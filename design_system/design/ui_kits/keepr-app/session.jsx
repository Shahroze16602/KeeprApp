/* Keepr — SCR-07 Cleanup Session (+ SCR-08 inline load recovery) */
function Session({ go, params }) {
  const K = window.KeeprDesignSystem_30a628;
  const { PhotoCard, IconButton, ComboCounter } = K;
  const Is = (n) => <i className={"ph-fill ph-" + n} />;
  const month = (params && params.month) || { month: "March", year: "2024", total: 412 };

  const deck = window.KEEPR_DECK;
  const [idx, setIdx] = React.useState(0);
  const [decisions, setDecisions] = React.useState([]); // {photo, decision}
  const [drag, setDrag] = React.useState({ x: 0, active: false });
  const [flyoff, setFlyoff] = React.useState(null);
  const [combo, setCombo] = React.useState(0);
  const [reclaimed, setReclaimed] = React.useState(0);
  const [recover, setRecover] = React.useState(false);
  const lastRef = React.useRef(0);
  const startRef = React.useRef(0);

  const THRESH = 110;
  const done = idx >= deck.length;
  const cur = deck[idx];
  const dir = drag.x > 0 ? "keep" : "gone";
  const progress = Math.min(1, Math.abs(drag.x) / THRESH);

  React.useEffect(() => {
    if (done) {
      const t = setTimeout(() => go("review", { month, decisions }), 420);
      return () => clearTimeout(t);
    }
    // simulate a load failure on a flagged item
    if (cur && cur.broken) setRecover(true); else setRecover(false);
  }, [idx, done]);

  const commit = (decision) => {
    if (!cur || flyoff) return;
    setFlyoff(decision);
    const now = Date.now();
    if (decision === "gone") setReclaimed((r) => r + cur.mb / 1024);
    if (now - lastRef.current < 1400) setCombo((c) => c + 1); else setCombo(1);
    lastRef.current = now;
    setTimeout(() => {
      setDecisions((d) => [...d, { photo: cur, decision }]);
      setIdx((i) => i + 1);
      setDrag({ x: 0, active: false });
      setFlyoff(null);
    }, 240);
  };
  const undo = () => {
    if (idx === 0) return;
    const prev = decisions[decisions.length - 1];
    if (prev && prev.decision === "gone") setReclaimed((r) => Math.max(0, r - prev.photo.mb / 1024));
    setDecisions((d) => d.slice(0, -1));
    setIdx((i) => i - 1);
    setCombo(0);
  };

  const onDown = (e) => { if (recover) return; startRef.current = e.clientX; setDrag({ x: 0, active: true }); e.currentTarget.setPointerCapture(e.pointerId); };
  const onMove = (e) => { if (drag.active) setDrag((d) => ({ ...d, x: e.clientX - startRef.current })); };
  const onUp = () => { if (!drag.active) return; if (Math.abs(drag.x) > THRESH) commit(drag.x > 0 ? "keep" : "gone"); else setDrag({ x: 0, active: false }); };

  const decidedCount = decisions.length;
  const pct = Math.round((decidedCount / deck.length) * 100);

  return (
    <div style={{ position: "relative", height: "100%", display: "flex", flexDirection: "column" }}>
      {/* top bar */}
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between", padding: "12px 16px 6px" }}>
        <IconButton variant="glass" size="sm" ariaLabel="Save and exit" onClick={() => go("main")}>{Is("caret-left")}</IconButton>
        <div style={{ textAlign: "center" }}>
          <div style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 18, letterSpacing: "-.03em", color: "var(--text-strong)" }}>{month.month} {month.year}</div>
          <Kicker style={{ fontSize: 9, marginTop: 1 }}>{month.partial ? "Selected · " : ""}Photo {Math.min(idx + 1, deck.length)} of {deck.length}</Kicker>
        </div>
        <IconButton variant="glass" size="sm" ariaLabel="Review decisions" onClick={() => go("review", { month, decisions })}>{Is("list-checks")}</IconButton>
      </div>

      {/* progress bar */}
      <div role="progressbar" aria-label={`${decidedCount} of ${deck.length} photos sorted`} aria-valuemin="0" aria-valuemax={deck.length} aria-valuenow={decidedCount} style={{ margin: "6px 18px 2px", height: 10, background: "var(--surface-inset)", border: "var(--bw) solid var(--border-hard)", borderRadius: 99, overflow: "hidden" }}>
        <div style={{ height: "100%", width: pct + "%", background: "var(--keep-grad)", borderRadius: 99, transition: "width 300ms var(--ease-spring)" }} />
      </div>
      {/* reclaimed ticker */}
      <div aria-live="polite" aria-atomic="true" style={{ display: "flex", justifyContent: "center", gap: 8, padding: "8px 0 6px" }}>
        <span style={{ fontFamily: "var(--font-num)", fontWeight: 900, fontSize: 15, color: "var(--reward)", letterSpacing: "-.03em" }}>{reclaimed.toFixed(2)} GB</span>
        <Kicker style={{ alignSelf: "center" }}>queued to free</Kicker>
      </div>

      {/* deck */}
      <div style={{ position: "relative", flex: 1, margin: "2px 20px 0" }}>
        {done ? (
          <div style={{ position: "absolute", inset: 0, display: "grid", placeItems: "center" }}>
            <div style={{ textAlign: "center", color: "var(--text-muted)" }}>{Is("check-circle")}<div style={{ marginTop: 8 }}><Kicker>Opening review…</Kicker></div></div>
          </div>
        ) : recover ? (
          <RecoveryCard Is={Is} onRetry={() => { deck[idx].broken = false; setRecover(false); }} onSkip={() => { setDecisions((d) => [...d, { photo: cur, decision: "unavailable" }]); setIdx((i) => i + 1); }} />
        ) : (
          <>
            {[1, 2].map((o) => { const p = deck[idx + o]; return p ? (
              <div key={p.id} aria-hidden="true" style={{ position: "absolute", inset: 0, transform: `translateY(${o * 10}px) scale(${1 - o * 0.045})`, zIndex: 1, opacity: 0.9 }}><PhotoCard image={p.src} /></div>
            ) : null; })}
            <div onPointerDown={onDown} onPointerMove={onMove} onPointerUp={onUp} onPointerCancel={() => setDrag({ x: 0, active: false })}
              style={{ position: "absolute", inset: 0, zIndex: 3, touchAction: "none", cursor: "grab",
                transform: flyoff ? `translateX(${flyoff === "keep" ? 140 : -140}%) rotate(${flyoff === "keep" ? 22 : -22}deg)` : `translateX(${drag.x}px) rotate(${drag.x * 0.05}deg)`,
                transition: drag.active ? "none" : "transform 320ms var(--ease-spring)" }}>
              <PhotoCard image={cur.src} imageAlt={`Photo from ${cur.date}, ${cur.size}`} date={cur.date} meta={cur.size} swipe={drag.x !== 0 || flyoff ? (flyoff || dir) : null} progress={flyoff ? 1 : progress} />
            </div>
          </>
        )}
        {combo >= 2 && !flyoff && !recover && !done && (
          <div style={{ position: "absolute", top: 12, right: 6, zIndex: 5, pointerEvents: "none" }}><ComboCounter combo={combo} /></div>
        )}
      </div>

      {/* action bar */}
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 20, padding: "16px 0 22px" }}>
        <IconButton variant="neutral" size="md" ariaLabel="Undo last decision" disabled={idx === 0} onClick={undo}>{Is("arrow-counter-clockwise")}</IconButton>
        <IconButton variant="gone" size="xl" ariaLabel="Delete photo" disabled={done || recover} onClick={() => commit("gone")}>{Is("trash")}</IconButton>
        <IconButton variant="keep" size="xl" ariaLabel="Keep photo" disabled={done || recover} onClick={() => commit("keep")}>{Is("heart")}</IconButton>
      </div>
    </div>
  );
}

function RecoveryCard({ Is, onRetry, onSkip }) {
  const K = window.KeeprDesignSystem_30a628;
  const { Button } = K;
  return (
    <div style={{ position: "absolute", inset: 0, borderRadius: "var(--r-xl)", border: "var(--bw-slab) solid var(--border-hard)", background: "var(--surface-card)", boxShadow: "var(--shadow-hard-xl)", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: 28, textAlign: "center", gap: 6 }}>
      <div style={{ width: 84, height: 84, borderRadius: "var(--r-lg)", background: "var(--surface-inset)", border: "var(--bw-thick) solid var(--border-hard)", display: "grid", placeItems: "center", color: "var(--gone)", fontSize: 40, marginBottom: 8 }}>{Is("image-broken")}</div>
      <div style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 21, letterSpacing: "-.03em", color: "var(--text-strong)" }}>Couldn't load this photo</div>
      <div style={{ fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 14, color: "var(--text-muted)", lineHeight: 1.4, maxWidth: 230 }}>No keep or delete was recorded. You can retry or skip it for now.</div>
      <div style={{ display: "flex", flexDirection: "column", gap: 10, width: "100%", maxWidth: 240, marginTop: 16 }}>
        <Button variant="keep" full icon={Is("arrow-clockwise")} onClick={onRetry}>Retry</Button>
        <Button variant="neutral" full onClick={onSkip}>Skip for now</Button>
      </div>
    </div>
  );
}

Object.assign(window, { Session });
