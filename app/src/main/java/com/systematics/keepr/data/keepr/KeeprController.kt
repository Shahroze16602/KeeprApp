package com.systematics.keepr.data.keepr

import android.app.RecoverableSecurityException
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.firebase.analytics.FirebaseAnalytics
import com.systematics.keepr.domain.keepr.GamificationRules
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

enum class CatalogStatus { Idle, Loading, Ready, Empty, PermissionRequired, Error }
enum class MediaDecision { Keep, Delete, Unavailable }
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromString(value: String?): ThemeMode = when (value?.uppercase()) {
            "LIGHT" -> LIGHT
            "DARK" -> DARK
            else -> SYSTEM
        }
    }
}
data class KeeprMedia(
    val uri: String,
    val takenAt: Long,
    val sizeBytes: Long?,
    val width: Int,
    val height: Int,
)
data class MonthSummary(
    val key: String,
    val label: String,
    val year: Int,
    val total: Int,
    val decided: Int,
    val deletedBytes: Long,
    val complete: Boolean,
)
data class DecisionRecord(val media: KeeprMedia, val decision: MediaDecision, val sequence: Int)
data class CleanupSession(
    val id: Long,
    val scopeKey: String,
    val title: String,
    val partial: Boolean,
    val media: List<KeeprMedia>,
    val decisions: List<DecisionRecord>,
) {
    val current: KeeprMedia? get() {
        val done = decisions.mapTo(HashSet()) { it.media.uri }
        return media.firstOrNull { it.uri !in done }
    }
    val keep: List<DecisionRecord> get() = decisions.filter { it.decision == MediaDecision.Keep }
    val delete: List<DecisionRecord> get() = decisions.filter { it.decision == MediaDecision.Delete }
    val unavailable: List<DecisionRecord> get() = decisions.filter { it.decision == MediaDecision.Unavailable }
    val progress: Float get() = if (media.isEmpty()) 0f else decisions.size.toFloat() / media.size
}
data class DeletionResult(
    val requested: Int = 0,
    val confirmed: Int = 0,
    val unresolved: Int = 0,
    val failed: Int = 0,
    val confirmedBytes: Long = 0L,
    val finished: Boolean = false,
)
data class KeeprState(
    val catalogStatus: CatalogStatus = CatalogStatus.Idle,
    val months: List<MonthSummary> = emptyList(),
    val selectedMedia: List<KeeprMedia> = emptyList(),
    val session: CleanupSession? = null,
    val deletion: DeletionResult = DeletionResult(),
    val error: String? = null,
    val xp: Int = 0,
    val streak: Int = 0,
    val monthsCleared: Int = 0,
    val reclaimedBytes: Long = 0L,
    val combo: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val darkMode: Boolean = true,
    val fullMotion: Boolean = true,
    val haptics: Boolean = true,
    val analytics: Boolean = false,
)

sealed interface DeleteStep {
    data class Launch(val intentSender: android.content.IntentSender, val count: Int) : DeleteStep
    data object Finished : DeleteStep
    data class Failed(val message: String) : DeleteStep
}

class KeeprController(private val context: Context) {
    private val resolver = context.contentResolver
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val db = KeeprDb(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(loadInitial())
    val state: StateFlow<KeeprState> = _state.asStateFlow()
    private val monthMedia = mutableMapOf<String, List<KeeprMedia>>()
    private var lastDecisionAt = 0L
    private var deleteQueue = emptyList<DecisionRecord>()
    private var deleteCursor = 0
    private var activeBatch = emptyList<DecisionRecord>()
    private var confirmedUris = mutableSetOf<String>()
    private var unresolvedUris = mutableSetOf<String>()

    init {
        applyAnalytics(prefs.getBoolean(KEY_ANALYTICS, false), reset = false)
    }

    fun refreshCatalog() {
        _state.update { it.copy(catalogStatus = CatalogStatus.Loading, error = null) }
        scope.launch {
            try {
                val all = queryImages()
                monthMedia.clear()
                monthMedia.putAll(all.groupBy { monthKey(it.takenAt) })
                val progress = db.progressByScope()
                val months = monthMedia.entries.map { (key, media) ->
                    val ym = YearMonth.parse(key)
                    val p = progress[key]
                    MonthSummary(
                        key = key,
                        label = ym.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault()),
                        year = ym.year,
                        total = media.size,
                        decided = p?.first?.coerceAtMost(media.size) ?: 0,
                        deletedBytes = p?.second ?: 0,
                        complete = p?.third ?: false,
                    )
                }.sortedByDescending { it.key }
                val selected = if (hasPartialAccess() && !hasFullAccess()) all else emptyList()
                _state.update {
                    it.copy(
                        catalogStatus = if (months.isEmpty()) CatalogStatus.Empty else CatalogStatus.Ready,
                        months = months, selectedMedia = selected, error = null
                    )
                }
            } catch (_: SecurityException) {
                _state.update { it.copy(catalogStatus = CatalogStatus.PermissionRequired, error = null) }
            } catch (t: Throwable) {
                _state.update { it.copy(catalogStatus = CatalogStatus.Error, error = t.message ?: "Photo scan failed") }
            }
        }
    }

    fun startMonth(key: String, title: String, partial: Boolean = false) {
        scope.launch {
            try {
                val media = if (partial) queryImages() else monthMedia[key] ?: queryImages().filter { monthKey(it.takenAt) == key }
                val sessionId = db.sessionForScope(key) ?: db.createSession(key, title, partial)
                val decisions = db.decisions(sessionId)
                _state.update { it.copy(session = CleanupSession(sessionId, key, title, partial, media, decisions), combo = 0, error = null) }
            } catch (t: Throwable) {
                _state.update { it.copy(error = t.message ?: "Unable to open this cleanup") }
            }
        }
    }

    fun decide(decision: MediaDecision) {
        val session = _state.value.session ?: return
        val item = session.current ?: return
        val sequence = (session.decisions.maxOfOrNull { it.sequence } ?: 0) + 1
        val now = System.currentTimeMillis()
        val combo = if (now - lastDecisionAt <= GamificationRules.COMBO_WINDOW_MS) _state.value.combo + 1 else 1
        val xp = _state.value.xp + GamificationRules.XP_PER_DECISION
        val record = DecisionRecord(item, decision, sequence)

        lastDecisionAt = now
        prefs.edit().putInt(KEY_XP, xp).apply()
        _state.update { state ->
            val active = state.session
            if (active?.id != session.id || active.decisions.any { it.media.uri == item.uri }) state
            else state.copy(
                session = active.copy(decisions = active.decisions + record),
                combo = combo,
                xp = xp,
                error = null,
            )
        }

        scope.launch {
            runCatching { db.addDecision(session.id, item, decision, sequence) }
                .onFailure { failure ->
                    _state.update { state ->
                        val active = state.session
                        if (active?.id != session.id) state
                        else state.copy(
                            session = active.copy(decisions = active.decisions.filterNot {
                                it.media.uri == item.uri && it.sequence == sequence
                            }),
                            combo = 0,
                            xp = max(0, state.xp - GamificationRules.XP_PER_DECISION),
                            error = failure.message ?: "Unable to save this decision",
                        )
                    }
                    prefs.edit().putInt(KEY_XP, _state.value.xp).apply()
                }
        }
    }

    fun undo() {
        val session = _state.value.session ?: return
        val last = session.decisions.maxByOrNull { it.sequence } ?: return
        scope.launch {
            db.removeDecision(session.id, last.media.uri)
            val xp = max(0, _state.value.xp - GamificationRules.XP_PER_DECISION)
            prefs.edit().putInt(KEY_XP, xp).apply()
            _state.update { it.copy(session = session.copy(decisions = session.decisions - last), combo = 0, xp = xp) }
        }
    }

    fun moveDecision(uri: String) {
        val session = _state.value.session ?: return
        val row = session.decisions.firstOrNull { it.media.uri == uri } ?: return
        if (row.decision == MediaDecision.Unavailable) return
        val next = if (row.decision == MediaDecision.Delete) MediaDecision.Keep else MediaDecision.Delete
        scope.launch {
            db.updateDecision(session.id, uri, next)
            _state.update { it.copy(session = session.copy(decisions = session.decisions.map { d -> if (d.media.uri == uri) d.copy(decision = next) else d })) }
        }
    }

    fun hasSeenReviewMoveHint(): Boolean = prefs.getBoolean(KEY_REVIEW_MOVE_HINT, false)

    fun markReviewMoveHintSeen() {
        prefs.edit().putBoolean(KEY_REVIEW_MOVE_HINT, true).apply()
    }

    fun skipUnavailable() = decide(MediaDecision.Unavailable)
    fun clearCombo() = _state.update { it.copy(combo = 0) }

    fun restartSession(onComplete: () -> Unit = {}) {
        val session = _state.value.session ?: return
        scope.launch {
            db.clearDecisions(session.id)
            _state.update { it.copy(session = session.copy(decisions = emptyList()), combo = 0) }
            withContext(Dispatchers.Main.immediate) { onComplete() }
        }
    }

    fun beginDeletion() {
        val session = _state.value.session ?: return
        deleteQueue = session.delete.filter { it.media.uri !in confirmedUris }
        deleteCursor = 0
        activeBatch = emptyList()
        confirmedUris.clear(); unresolvedUris.clear()
        _state.update { it.copy(deletion = DeletionResult(requested = deleteQueue.size)) }
    }

    suspend fun nextDeletionStep(): DeleteStep = withContext(Dispatchers.IO) {
        while (deleteCursor < deleteQueue.size) {
            activeBatch = if (Build.VERSION.SDK_INT >= 30) {
                deleteQueue.drop(deleteCursor).take(PLATFORM_DELETE_LIMIT)
            } else {
                deleteQueue.drop(deleteCursor).take(1)
            }
            try {
                val sender = if (Build.VERSION.SDK_INT >= 30) {
                    MediaStore.createDeleteRequest(
                        resolver,
                        activeBatch.map { Uri.parse(it.media.uri) }
                    ).intentSender
                } else {
                    val uri = Uri.parse(activeBatch.first().media.uri)
                    try {
                        resolver.delete(uri, null, null)
                        null
                    } catch (e: RecoverableSecurityException) {
                        e.userAction.actionIntent.intentSender
                    }
                }
                if (sender != null) return@withContext DeleteStep.Launch(sender, activeBatch.size)
                reconcileActiveBatch()
            } catch (_: Throwable) {
                unresolvedUris.addAll(activeBatch.map { it.media.uri })
                deleteCursor += activeBatch.size
                _state.update {
                    it.copy(deletion = it.deletion.copy(
                        unresolved = unresolvedUris.size,
                        failed = it.deletion.failed + activeBatch.size
                    ))
                }
            }
        }
        finishDeletion()
        DeleteStep.Finished
    }

    suspend fun onDeletionResult() = withContext(Dispatchers.IO) { reconcileActiveBatch() }

    private fun reconcileActiveBatch() {
        activeBatch.forEach { row ->
            if (exists(Uri.parse(row.media.uri))) unresolvedUris += row.media.uri else confirmedUris += row.media.uri
        }
        deleteCursor += activeBatch.size
        val bytes = deleteQueue.filter { it.media.uri in confirmedUris }.sumOf { it.media.sizeBytes ?: 0L }
        _state.update {
            it.copy(deletion = it.deletion.copy(
                confirmed = confirmedUris.size, unresolved = unresolvedUris.size, confirmedBytes = bytes
            ))
        }
    }

    private fun finishDeletion() {
        val session = _state.value.session
        val result = _state.value.deletion.copy(finished = true)
        if (session != null) {
            db.completeSession(session.id, result.confirmedBytes)
            val isFullMonth = !session.partial
            val alreadyAwarded = prefs.getStringSet(KEY_COMPLETED, emptySet())?.contains(session.scopeKey) == true
            val completed = if (isFullMonth && !alreadyAwarded) _state.value.monthsCleared + 1 else _state.value.monthsCleared
            val xp = _state.value.xp + GamificationRules.XP_PER_COMPLETION
            val streak = updateStreak()
            val reclaimed = _state.value.reclaimedBytes + result.confirmedBytes
            if (isFullMonth) {
                val set = prefs.getStringSet(KEY_COMPLETED, emptySet())!!.toMutableSet().apply { add(session.scopeKey) }
                prefs.edit().putStringSet(KEY_COMPLETED, set).apply()
            }
            prefs.edit().putInt(KEY_MONTHS, completed).putInt(KEY_XP, xp).putLong(KEY_RECLAIMED, reclaimed).apply()
            _state.update { it.copy(deletion = result, monthsCleared = completed, xp = xp, streak = streak, reclaimedBytes = reclaimed, combo = 0) }
        } else _state.update { it.copy(deletion = result) }
    }

    fun finishWithoutDeletion() {
        beginDeletion()
        finishDeletion()
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).putBoolean(KEY_DARK, mode == ThemeMode.DARK).apply()
        _state.update { it.copy(themeMode = mode, darkMode = mode == ThemeMode.DARK) }
    }
    fun setDarkMode(value: Boolean) = setThemeMode(if (value) ThemeMode.DARK else ThemeMode.LIGHT)
    fun setFullMotion(value: Boolean) { prefs.edit().putBoolean(KEY_MOTION, value).apply(); _state.update { it.copy(fullMotion = value) } }
    fun setHaptics(value: Boolean) { prefs.edit().putBoolean(KEY_HAPTICS, value).apply(); _state.update { it.copy(haptics = value) } }
    fun setAnalytics(value: Boolean) {
        prefs.edit().putBoolean(KEY_ANALYTICS, value).apply()
        applyAnalytics(value, reset = !value)
        _state.update { it.copy(analytics = value) }
    }
    fun resetKeepr() {
        db.reset()
        prefs.edit().clear().commit()
        applyAnalytics(false, reset = true)
        _state.value = loadInitial()
        monthMedia.clear()
    }

    fun hasFullAccess(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= 33) android.Manifest.permission.READ_MEDIA_IMAGES else android.Manifest.permission.READ_EXTERNAL_STORAGE
        return context.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    fun hasPartialAccess(): Boolean = Build.VERSION.SDK_INT >= 34 &&
        context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == android.content.pm.PackageManager.PERMISSION_GRANTED

    private fun applyAnalytics(enabled: Boolean, reset: Boolean) {
        runCatching {
            val analytics = FirebaseAnalytics.getInstance(context)
            analytics.setAnalyticsCollectionEnabled(enabled)
            analytics.setConsent(mapOf(
                FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to if (enabled) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED,
                FirebaseAnalytics.ConsentType.AD_STORAGE to FirebaseAnalytics.ConsentStatus.DENIED,
                FirebaseAnalytics.ConsentType.AD_USER_DATA to FirebaseAnalytics.ConsentStatus.DENIED,
                FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to FirebaseAnalytics.ConsentStatus.DENIED,
            ))
            if (reset) analytics.resetAnalyticsData()
        }
    }

    private fun loadInitial(): KeeprState {
        val savedTheme = prefs.getString(KEY_THEME_MODE, null)
        val themeMode = if (savedTheme != null) {
            ThemeMode.fromString(savedTheme)
        } else if (prefs.contains(KEY_DARK)) {
            if (prefs.getBoolean(KEY_DARK, true)) ThemeMode.DARK else ThemeMode.LIGHT
        } else {
            ThemeMode.SYSTEM
        }
        return KeeprState(
            xp = prefs.getInt(KEY_XP, 0), streak = prefs.getInt(KEY_STREAK, 0),
            monthsCleared = prefs.getInt(KEY_MONTHS, 0), reclaimedBytes = prefs.getLong(KEY_RECLAIMED, 0),
            themeMode = themeMode, darkMode = themeMode == ThemeMode.DARK,
            fullMotion = prefs.getBoolean(KEY_MOTION, true),
            haptics = prefs.getBoolean(KEY_HAPTICS, true), analytics = prefs.getBoolean(KEY_ANALYTICS, false),
        )
    }

    private fun updateStreak(): Int {
        val today = LocalDate.now().toEpochDay()
        val last = prefs.getLong(KEY_LAST_DAY, Long.MIN_VALUE)
        val next = GamificationRules.streak(prefs.getInt(KEY_STREAK, 0), last.takeUnless { it == Long.MIN_VALUE }, today)
        prefs.edit().putLong(KEY_LAST_DAY, today).putInt(KEY_STREAK, next).apply()
        return next
    }

    private fun queryImages(): List<KeeprMedia> {
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE, MediaStore.Images.Media.WIDTH, MediaStore.Images.Media.HEIGHT)
        val items = mutableListOf<KeeprMedia>()
        resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, "${MediaStore.Images.Media.DATE_TAKEN} DESC")?.use { c ->
            val id = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val taken = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val added = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val size = c.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val width = c.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val height = c.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            while (c.moveToNext()) {
                val at = c.getLong(taken).takeIf { it > 0 } ?: c.getLong(added) * 1000
                items += KeeprMedia(
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, c.getLong(id)).toString(),
                    at, if (c.isNull(size)) null else c.getLong(size), c.getInt(width), c.getInt(height)
                )
            }
        }
        return items
    }
    private fun exists(uri: Uri): Boolean = try { resolver.query(uri, arrayOf(MediaStore.MediaColumns._ID), null, null, null)?.use { it.moveToFirst() } == true } catch (_: Throwable) { false }
    private fun monthKey(at: Long): String = YearMonth.from(Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault())).toString()

    companion object {
        const val PLATFORM_DELETE_LIMIT = 2000
        private const val COMBO_WINDOW_MS = 2500L
        private const val PREFS = "keepr_state"
        private const val KEY_XP = "xp"; private const val KEY_STREAK = "streak"; private const val KEY_LAST_DAY = "last_day"
        private const val KEY_MONTHS = "months"; private const val KEY_RECLAIMED = "reclaimed"; private const val KEY_COMPLETED = "completed"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DARK = "dark"; private const val KEY_MOTION = "motion"; private const val KEY_HAPTICS = "haptics"; private const val KEY_ANALYTICS = "analytics"
        private const val KEY_REVIEW_MOVE_HINT = "review_move_hint_seen"
    }
}

private class KeeprDb(context: Context) : SQLiteOpenHelper(context, "keepr.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE session(id INTEGER PRIMARY KEY AUTOINCREMENT, scope TEXT UNIQUE NOT NULL, title TEXT NOT NULL, partial INTEGER NOT NULL, complete INTEGER NOT NULL DEFAULT 0, reclaimed INTEGER NOT NULL DEFAULT 0, updated INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE decision(session_id INTEGER NOT NULL, uri TEXT NOT NULL, kind TEXT NOT NULL, taken INTEGER NOT NULL, bytes INTEGER, width INTEGER NOT NULL, height INTEGER NOT NULL, seq INTEGER NOT NULL, PRIMARY KEY(session_id, uri))")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    fun sessionForScope(scope: String): Long? = readableDatabase.rawQuery("SELECT id FROM session WHERE scope=?", arrayOf(scope)).use { if (it.moveToFirst()) it.getLong(0) else null }
    fun createSession(scope: String, title: String, partial: Boolean): Long = writableDatabase.insertOrThrow("session", null, ContentValues().apply {
        put("scope", scope); put("title", title); put("partial", if (partial) 1 else 0); put("updated", System.currentTimeMillis())
    })
    fun isComplete(id: Long): Boolean = readableDatabase.rawQuery("SELECT complete FROM session WHERE id=?", arrayOf(id.toString())).use { it.moveToFirst() && it.getInt(0) == 1 }
    fun decisions(id: Long): List<DecisionRecord> = readableDatabase.rawQuery(
        "SELECT uri,kind,taken,bytes,width,height,seq FROM decision WHERE session_id=? ORDER BY seq", arrayOf(id.toString())
    ).use { c ->
        buildList {
            while (c.moveToNext()) add(DecisionRecord(
                KeeprMedia(c.getString(0), c.getLong(2), if (c.isNull(3)) null else c.getLong(3), c.getInt(4), c.getInt(5)),
                MediaDecision.valueOf(c.getString(1)), c.getInt(6)
            ))
        }
    }
    fun addDecision(id: Long, media: KeeprMedia, kind: MediaDecision, sequence: Int): Int {
        val rowId = writableDatabase.insertWithOnConflict("decision", null, ContentValues().apply {
            put("session_id", id); put("uri", media.uri); put("kind", kind.name); put("taken", media.takenAt)
            if (media.sizeBytes == null) putNull("bytes") else put("bytes", media.sizeBytes)
            put("width", media.width); put("height", media.height); put("seq", sequence)
        }, SQLiteDatabase.CONFLICT_REPLACE)
        check(rowId != -1L) { "Unable to persist photo decision" }
        touch(id); return sequence
    }
    fun removeDecision(id: Long, uri: String) { writableDatabase.delete("decision", "session_id=? AND uri=?", arrayOf(id.toString(), uri)); touch(id) }
    fun updateDecision(id: Long, uri: String, kind: MediaDecision) { writableDatabase.update("decision", ContentValues().apply { put("kind", kind.name) }, "session_id=? AND uri=?", arrayOf(id.toString(), uri)); touch(id) }
    fun clearDecisions(id: Long) { writableDatabase.delete("decision", "session_id=?", arrayOf(id.toString())); touch(id) }
    fun completeSession(id: Long, bytes: Long) { writableDatabase.update("session", ContentValues().apply { put("complete",1); put("reclaimed",bytes); put("updated",System.currentTimeMillis()) }, "id=?", arrayOf(id.toString())) }
    fun progressByScope(): Map<String, Triple<Int,Long,Boolean>> = readableDatabase.rawQuery(
        "SELECT s.scope,COUNT(d.uri),s.reclaimed,s.complete FROM session s LEFT JOIN decision d ON d.session_id=s.id GROUP BY s.id", null
    ).use { c -> buildMap { while(c.moveToNext()) put(c.getString(0), Triple(c.getInt(1),c.getLong(2),c.getInt(3)==1)) } }
    fun reset() { writableDatabase.delete("decision", null, null); writableDatabase.delete("session", null, null) }
    private fun touch(id: Long) { writableDatabase.update("session", ContentValues().apply { put("updated",System.currentTimeMillis()); put("complete",0) }, "id=?", arrayOf(id.toString())) }
}
