package com.systematics.monetization.ui.utils

import android.util.Log
import com.systematics.monetization.ui.state.DialogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn


object MonetizationSharedState {

    private const val TAG = "MonetizationSharedStateTAG"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _blockAppOpenAdTemporary = MutableStateFlow(false)
    private val _blockAppOpenAdManually = MutableStateFlow(true)

    val blockAppOpenAd = combine(_blockAppOpenAdTemporary, _blockAppOpenAdManually) { t, m ->
        t || m
    }.stateIn(
        scope,
        SharingStarted.Eagerly,
        _blockAppOpenAdTemporary.value || _blockAppOpenAdManually.value
    )
    private val _dialogState: MutableStateFlow<DialogState> = MutableStateFlow(DialogState.None)
    val dialogState = _dialogState.asStateFlow()

    var unblockAppOpenAdOnAppResume = false
        private set

    fun blockAppOpen(unblockOnAppResume: Boolean = false) {
        _blockAppOpenAdTemporary.value = true
        if (unblockOnAppResume) {
            unblockAppOpenAdOnAppResume = true
        }
        Log.d(TAG, "blockAppOpen: blocked")
    }

    fun unblockAppOpen() {
        _blockAppOpenAdTemporary.value = false
        Log.d(TAG, "unBlockAppOpen: unBlocked")
    }

    fun blockAppOpenManually() {
        _blockAppOpenAdManually.value = true
        Log.d(TAG, "blockAppOpenManually: blocked")
    }

    fun unblockAppOpenManually() {
        _blockAppOpenAdManually.value = false
        Log.d(TAG, "unBlockAppOpenManually: unBlocked")
    }

    fun unblockAppOpenOnResume() {
        if (unblockAppOpenAdOnAppResume) {
            unblockAppOpen()
            unblockAppOpenAdOnAppResume = false
            Log.d(TAG, "unBlockAppOpenOnResume: unBlocked")
        }
    }

    suspend fun showLoadingForTimeMillis(
        timeMillis: Long,
        dialogType: String = "default",
    ) {
        Log.d(TAG, "showLoadingForTimeMillis: time $timeMillis dialogType $dialogType")
        if (timeMillis > 0) {
            updateLoadingState(DialogState.Show(dialogType))
            delay(timeMillis)
            updateLoadingState(DialogState.None)
        }
    }

    fun showDialog(dialogType: String = "default") {
        updateLoadingState(DialogState.Show(dialogType))
    }

    fun hideDialog() {
        updateLoadingState(dialogState = DialogState.None)
    }

    private fun updateLoadingState(dialogState: DialogState) {
        Log.d(TAG, "updateLoadingState: $dialogState")
        _dialogState.value = dialogState
    }
}