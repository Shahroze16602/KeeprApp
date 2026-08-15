/* Keepr — SCR-09 Review · SCR-10 Confirm · SCR-11 Deletion Progress · SCR-12 Completion */
const Ir = (n, s) => <i className={"ph-fill ph-" + n} style={s} />;

/* ---------------- SCR-09 Pre-commit Review ---------------- */
function Review({ go, params }) {
  const K = window.KeeprDesignSystem_30a628;
  const { Button } = K;
  const month = (params && params.month) || { month: "March", year: "2024" };
  const seed = (params && params.decisions) || [];
  // fall back to demo decisions if session was skipped
  const base = seed.length ? seed : window.KEEPR_DECK.map((p, i) => ({ photo: p, decision: i % 3 === 0 ? "gone" : "keep" }));
  const [map, setMap] = React.useState(() => Object.fromEntries(base.map((d) => [d.photo.id, d.decision])));
  const [tab, setTab] = React.useState("gone");
  const photoById = Object.fromEntries(base.map((d) => [d.photo.id, d.photo]));

  const ids = Object.keys(map);
  const keepIds = ids.filter((id) => map[id] === "keep");
  const goneIds = ids.filter((id) => map[id] === "gone");
  const gb = goneIds.reduce((s, id) => s + (photoById[id].mb || 0), 0) / 1024;
  const shown = tab === "keep" ? keepIds : goneIds;
  const move = (id) => setMap((m) => ({ ...m, [id]: m[id] === "keep" ? "gone" : "keep" }));

  const result = { kept: keepIds.length, deleted: goneIds.length, gb, month };

  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div style={{ padding: "10px 18px 8px", display: "flex", alignItems: "center", gap: 12 }}>
        <button aria-label="Back to cleanup" onClick={() => go("session", { month })} style={window.KeeprBackBtn}>{Ir("caret-left")}</button>
        <div><Kicker color="var(--keep)">Almost done</Kicker><div style={h1r}>Review {month.month}</div></div>
      </div>

      {/* group tabs */}
      <div style={{ display: "flex", gap: 10, padding: "6px 18px 10px" }}>
        <GroupTab on={tab === "keep"} onClick={() => setTab("keep")} label="Keep" count={keepIds.length} tone="keep" Ir={Ir} />
        <GroupTab on={tab === "gone"} onClick={() => setTab("gone")} label="Delete" count={goneIds.length} tone="gone" Ir={Ir} />
      </div>

      {tab === "gone" && (
        <div style={{ padding: "0 18px 8px", display: "flex", alignItems: "center", gap: 8, color: "var(--text-muted)" }}>
          {Ir("info", { fontSize: 16 })}<span style={{ fontFamily: "var(--font-ui)", fontWeight: 600, fontSize: 12.5 }}>Est. <b style={{ color: "var(--reward)" }}>{gb.toFixed(2)} GB</b> freed · tap a photo to keep it instead</span>
        </div>
      )}

      <div style={{ flex: 1, overflowY: "auto", padding: "4px 18px 8px" }}>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 8 }}>
          {shown.map((id) => {
            const p = photoById[id];
            return (
              <button key={id} aria-label={`Move photo from ${p.date} to ${tab === "gone" ? "Keep" : "Delete"}`} onClick={() => move(id)} style={{ position: "relative", padding: 0, aspectRatio: "3/4", borderRadius: "var(--r-sm)", overflow: "hidden", border: `var(--bw) solid ${tab === "gone" ? "var(--gone)" : "var(--keep)"}`, cursor: "pointer", boxShadow: "var(--shadow-hard-sm)" }}>
                <img src={p.src} alt={`Photo from ${p.date}`} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
                <span style={{ position: "absolute", inset: 0, background: tab === "gone" ? "var(--gone-wash)" : "transparent" }} />
                <span style={{ position: "absolute", top: 5, left: 5, width: 24, height: 24, borderRadius: 99, background: tab === "gone" ? "var(--gone)" : "var(--keep)", color: tab === "gone" ? "var(--gone-ink)" : "var(--keep-ink)", border: "2px solid var(--border-hard)", display: "grid", placeItems: "center", fontSize: 13 }}>{tab === "gone" ? Ir("trash") : Ir("heart")}</span>
              </button>
            );
          })}
          {shown.length === 0 && <div style={{ gridColumn: "1/-1", textAlign: "center", padding: 30, color: "var(--text-muted)" }}><Kicker>Nothing in this group</Kicker></div>}
        </div>
      </div>

      <div style={{ ...window.KeeprFootBar, flexDirection: "column", gap: 8 }}>
        {goneIds.length > 0 ? (
          <Button variant="gone" size="lg" full icon={Ir("trash")} onClick={() => go("confirm", { result })}>Delete {goneIds.length} permanently</Button>
        ) : (
          <Button variant="keep" size="lg" full icon={Ir("flag-checkered")} onClick={() => go("progress", { result, skip: true })}>Finish month · 0 to delete</Button>
        )}
        <Button variant="ghost" full onClick={() => go("main")}>Save for later</Button>
      </div>
    </div>
  );
}
function GroupTab({ on, onClick, label, count, tone, Ir }) {
  return (
    <button aria-pressed={on} onClick={onClick} style={{ flex: 1, display: "flex", alignItems: "center", justifyContent: "center", gap: 8, height: 52, background: on ? (tone === "gone" ? "var(--gone-grad)" : "var(--keep-grad)") : "var(--surface-card)", color: on ? (tone === "gone" ? "var(--gone-ink)" : "var(--keep-ink)") : "var(--text-muted)", border: "var(--bw-thick) solid var(--border-hard)", borderRadius: "var(--r-pill)", cursor: "pointer", boxShadow: on ? "var(--shadow-hard-sm)" : "none", fontFamily: "var(--font-ui)", fontWeight: 900, fontSize: 14, letterSpacing: ".04em", textTransform: "uppercase", transition: "all 160ms var(--ease-spring)" }}>
      {Ir(tone === "gone" ? "trash" : "heart")}{label}<span style={{ fontFamily: "var(--font-num)", fontSize: 17 }}>{count}</span>
    </button>
  );
}

/* ---------------- SCR-10 Deletion Confirmation ---------------- */
function Confirm({ go, params }) {
  const K = window.KeeprDesignSystem_30a628;
  const { Button } = K;
  const result = (params && params.result) || { deleted: 3, gb: 0.3 };
  const batches = Math.ceil(result.deleted / 2000);
  return (
    <div style={{ position: "absolute", inset: 0, display: "flex", flexDirection: "column", justifyContent: "flex-end", background: "rgba(8,8,7,.72)", backdropFilter: "blur(4px)", zIndex: 30 }}>
      <div style={{ animation: "k-sheet 320ms var(--ease-spring) both", background: "var(--bg-app-2)", borderTop: "var(--bw-slab) solid var(--border-hard)", borderRadius: "34px 34px 0 0", padding: "10px 22px 26px" }}>
        <div style={{ width: 46, height: 6, borderRadius: 99, background: "var(--border-soft)", margin: "0 auto 18px" }} />
        <div style={{ display: "grid", placeItems: "center", marginBottom: 14 }}>
          <div style={{ width: 76, height: 76, borderRadius: "var(--r-lg)", background: "var(--gone-grad)", border: "var(--bw-thick) solid var(--border-hard)", display: "grid", placeItems: "center", color: "var(--gone-ink)", fontSize: 36, boxShadow: "var(--shadow-hard)" }}>{Ir("trash")}</div>
        </div>
        <div style={{ textAlign: "center", fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 26, letterSpacing: "-.03em", color: "var(--text-strong)", lineHeight: 1.05 }}>Delete {result.deleted} photos<br />permanently?</div>
        <div style={{ textAlign: "center", fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 14.5, color: "var(--text-muted)", margin: "10px 0 16px", lineHeight: 1.45 }}>
          Android will ask you to confirm{batches > 1 ? ` — in ${batches} batches` : ""}. This frees an estimated <b style={{ color: "var(--reward)" }}>{result.gb.toFixed(2)} GB</b>. Kept photos are never touched.
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <Button variant="gone" size="lg" full icon={Ir("trash")} onClick={() => go("progress", { result })}>Delete permanently</Button>
          <Button variant="ghost" full onClick={() => go("review", { month: result.month })}>Cancel</Button>
        </div>
      </div>
    </div>
  );
}

/* ---------------- SCR-11 Deletion Progress & Recovery ---------------- */
function Progress({ go, params }) {
  const result = (params && params.result) || { deleted: 3, kept: 6, gb: 0.3, month: { month: "March", year: "2024" } };
  const total = result.deleted;
  const [stage, setStage] = React.useState(params && params.skip ? "reconciling" : "system");
  const [n, setN] = React.useState(0);
  React.useEffect(() => {
    let t;
    if (stage === "system") t = setTimeout(() => setStage("reconciling"), 1100);
    else if (stage === "reconciling") {
      if (n < total) t = setTimeout(() => setN((x) => x + Math.max(1, Math.ceil(total / 12))), 90);
      else t = setTimeout(() => go("complete", { result }), 700);
    }
    return () => clearTimeout(t);
  }, [stage, n]);
  const shownN = Math.min(n, total);
  const pct = total ? Math.round((shownN / total) * 100) : 100;

  return (
    <div aria-live="polite" aria-busy={stage !== "done"} style={{ position: "absolute", inset: 0, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: 34, textAlign: "center", background: "var(--bg-app)" }}>
      {stage === "system" ? (
        <>
          <div style={{ width: 96, height: 96, borderRadius: "var(--r-2xl)", background: "var(--surface-card)", border: "var(--bw-slab) solid var(--border-hard)", display: "grid", placeItems: "center", color: "var(--gone)", fontSize: 46, boxShadow: "var(--shadow-hard-lg)", animation: "k-bob 1.4s var(--ease-out) infinite" }}>{Ir("device-mobile")}</div>
          <div style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 24, letterSpacing: "-.03em", color: "var(--text-strong)", marginTop: 22 }}>Confirm with Android</div>
          <div style={{ fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 14.5, color: "var(--text-muted)", marginTop: 8, maxWidth: 260, lineHeight: 1.45 }}>The system is asking permission to delete {total} photos. Approve it to continue.</div>
        </>
      ) : (
        <>
          <div style={{ position: "relative", width: 150, height: 150, display: "grid", placeItems: "center" }}>
            <svg width="150" height="150" style={{ transform: "rotate(-90deg)" }}>
              <circle cx="75" cy="75" r="66" fill="none" stroke="var(--surface-inset)" strokeWidth="14" />
              <circle cx="75" cy="75" r="66" fill="none" stroke="var(--gone)" strokeWidth="14" strokeLinecap="round" strokeDasharray={2 * Math.PI * 66} strokeDashoffset={2 * Math.PI * 66 * (1 - pct / 100)} style={{ transition: "stroke-dashoffset 120ms linear" }} />
            </svg>
            <div style={{ position: "absolute", fontFamily: "var(--font-num)", fontWeight: 900, fontSize: 42, letterSpacing: "-.04em", color: "var(--text-strong)" }}>{shownN}</div>
          </div>
          <div style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 22, letterSpacing: "-.03em", color: "var(--text-strong)", marginTop: 22 }}>{pct < 100 ? "Reconciling…" : "Done"}</div>
          <div style={{ fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 14, color: "var(--text-muted)", marginTop: 6 }}>Re-checking each photo — no result is assumed.</div>
        </>
      )}
    </div>
  );
}

/* ---------------- SCR-12 Completion ---------------- */
function Complete({ go, params }) {
  const K = window.KeeprDesignSystem_30a628;
  const { StatNumber, Button, Badge } = K;
  const result = (params && params.result) || { kept: 6, deleted: 3, gb: 0.3, month: { month: "March", year: "2024" } };
  const cols = ["var(--keep-500)", "var(--gone-500)", "var(--reward-500)", "var(--win-500)", "#FF6FB5"];
  const conf = Array.from({ length: 46 }, (_, i) => ({ left: Math.random() * 100, delay: Math.random() * 0.5, dur: 1.7 + Math.random() * 1.3, color: cols[i % cols.length], size: 8 + Math.random() * 10, rot: Math.random() * 360 }));
  return (
    <div style={{ position: "absolute", inset: 0, overflowY: "auto", background: "radial-gradient(120% 80% at 50% 12%, rgba(255,150,40,.22), var(--bg-app) 58%)" }}>
      <div aria-hidden="true" style={{ position: "absolute", inset: 0, overflow: "hidden", pointerEvents: "none" }}>
        {conf.map((c, i) => <span key={i} style={{ position: "absolute", top: -20, left: c.left + "%", width: c.size, height: c.size * 1.4, background: c.color, border: "2px solid var(--border-hard)", borderRadius: 3, transform: `rotate(${c.rot}deg)`, animation: `k-confetti ${c.dur}s var(--ease-in) ${c.delay}s infinite` }} />)}
      </div>
      <div style={{ position: "relative", padding: "40px 22px 26px", minHeight: "100%", display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center" }}>
        <div style={{ animation: "k-pop-in 520ms var(--ease-boing) both" }}><Badge variant="milestone" tone="reward" icon="🏆" label="month cleared">✓</Badge></div>
        <div style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 30, letterSpacing: "-.04em", color: "var(--text-strong)", marginTop: 14 }}>{result.month.month} cleared!</div>
        <div style={{ margin: "8px 0 2px", animation: "k-slam 620ms var(--ease-boing) 180ms both" }}><StatNumber value={result.gb} decimals={2} prefix="+" suffix=" GB" size="70px" gradient duration={1100} /></div>
        <Kicker>Estimated space freed</Kicker>

        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 10, width: "100%", maxWidth: 340, margin: "22px 0 8px" }}>
          {[["heart", result.kept, "Kept", "var(--keep)"], ["trash", result.deleted, "Deleted", "var(--gone)"], ["clock-countdown", 0, "Unresolved", "var(--text-muted)"]].map(([ic, v, l, col], k) => (
            <div key={k} style={{ background: "var(--surface-card)", border: "var(--bw-thick) solid var(--border-hard)", borderRadius: "var(--r-lg)", boxShadow: "var(--shadow-hard-sm)", padding: "14px 6px", display: "flex", flexDirection: "column", alignItems: "center", gap: 2 }}>
              <span style={{ color: col, fontSize: 20 }}>{Ir(ic)}</span>
              <span style={{ fontFamily: "var(--font-num)", fontWeight: 900, fontSize: 26, color: "var(--text-strong)" }}>{v}</span>
              <Kicker style={{ fontSize: 9 }}>{l}</Kicker>
            </div>
          ))}
        </div>
        <div style={{ display: "flex", gap: 8, margin: "8px 0 2px" }}><Badge tone="win" icon="🔥">+1 day streak</Badge><Badge tone="ink">+1,200 XP</Badge></div>

        <div style={{ flex: 1 }} />
        <div style={{ width: "100%", maxWidth: 340, display: "flex", flexDirection: "column", gap: 10, marginTop: 26 }}>
          <div style={{ display: "flex", gap: 8 }}>
            <Button variant="neutral" full onClick={() => go("main")}>Back to months</Button>
            <Button variant="ghost" full icon={Ir("star")} onClick={() => go("rate")}>Rate Keepr</Button>
          </div>
        </div>
      </div>
    </div>
  );
}

const h1r = { fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 26, letterSpacing: "-.04em", color: "var(--text-strong)", lineHeight: 1 };
Object.assign(window, { Review, Confirm, Progress, Complete });
