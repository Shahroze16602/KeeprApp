/* Keepr — SCR-05 Main / Month Picker · SCR-06 Selected Photos Mode */
const Ip = (n, s) => <i className={"ph-fill ph-" + n} style={s} />;

function Main({ go }) {
  const K = window.KeeprDesignSystem_30a628;
  const { StreakBadge, StatNumber, ProgressRing, Badge } = K;
  const months = window.KEEPR_MONTHS;

  return (
    <div style={{ height: "100%", overflowY: "auto" }}>
      <div className="k-hide-scroll" style={{ padding: "6px 18px 130px" }}>
        {/* header */}
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "6px 0 14px" }}>
          <KeeprWord size={27} />
          <button onClick={() => go("settings")} style={gearBtn} aria-label="settings">{Ip("gear-six")}</button>
        </div>

        {/* reclaimed hero */}
        <div style={{ display: "flex", alignItems: "center", gap: 16, background: "var(--surface-card)", border: "var(--bw-slab) solid var(--border-hard)", borderRadius: "var(--r-2xl)", boxShadow: "var(--shadow-hard-lg)", padding: "18px 18px", marginBottom: 16, position: "relative", overflow: "hidden" }}>
          <div style={{ position: "absolute", inset: 0, background: "radial-gradient(90% 120% at 100% 0%, rgba(255,198,60,.14), transparent 60%)", pointerEvents: "none" }} />
          <ProgressRing value={0.64} size={82} gradient>
            <span style={{ fontFamily: "var(--font-num)", fontWeight: 900, fontSize: 24, letterSpacing: "-.04em", color: "var(--text-strong)" }}>7</span>
            <Kicker style={{ fontSize: 8 }}>Level</Kicker>
          </ProgressRing>
          <div style={{ flex: 1, minWidth: 0 }}>
            <Kicker>Reclaimed so far</Kicker>
            <StatNumber value={7.3} decimals={1} suffix=" GB" size="42px" gradient style={{ alignItems: "flex-start" }} />
            <div style={{ display: "flex", alignItems: "center", gap: 8, marginTop: 4 }}>
              <StreakBadge days={12} live />
              <span style={{ fontFamily: "var(--font-ui)", fontWeight: 700, fontSize: 12, color: "var(--text-muted)" }}>3 months cleared</span>
            </div>
          </div>
        </div>

        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
          <Kicker>Pick a month to clean</Kicker>
          <Badge tone="ink">4,180 to sort</Badge>
        </div>

        <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          {months.map((m) => (
            <MonthTile key={m.id} m={m} go={go} />
          ))}
        </div>

      </div>
    </div>
  );
}

function MonthTile({ m, go }) {
  const [pressed, setPressed] = React.useState(false);
  const done = m.state === "done";
  const value = m.total ? Math.min(1, m.done / m.total) : done ? 1 : 0;
  const size = 66, th = 9, r = (size - th) / 2, c = 2 * Math.PI * r;
  const onTap = () => go("session", { month: m });
  return (
    <button aria-label={`${m.month} ${m.year}, ${done ? "cleared" : m.done > 0 ? `${m.done} of ${m.total} sorted, resume` : `${m.total} photos, not started`}`} onClick={onTap} onPointerDown={() => setPressed(true)} onPointerUp={() => setPressed(false)} onPointerLeave={() => setPressed(false)} onPointerCancel={() => setPressed(false)}
      style={{ display: "flex", alignItems: "center", gap: 15, width: "100%", padding: "16px", textAlign: "left", background: done ? "var(--surface-card-2)" : "var(--surface-card)", color: "var(--text-strong)", border: "var(--bw-thick) solid var(--border-hard)", borderRadius: "var(--r-lg)", cursor: "pointer", boxShadow: pressed ? "var(--shadow-pressed)" : "var(--shadow-hard)", transform: pressed ? "translate(2px,3px)" : "none", transition: "transform 160ms var(--ease-spring), box-shadow 160ms" }}>
      <div style={{ position: "relative", width: size, height: size, flex: "0 0 auto", display: "grid", placeItems: "center" }}>
          <>
            <svg width={size} height={size} style={{ transform: "rotate(-90deg)" }}>
              <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="var(--surface-inset)" strokeWidth={th} />
              <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={done ? "var(--win)" : "var(--keep)"} strokeWidth={th} strokeLinecap="round" strokeDasharray={c} strokeDashoffset={c * (1 - value)} style={{ transition: "stroke-dashoffset var(--dur-settle) var(--ease-spring)" }} />
            </svg>
            <span style={{ position: "absolute", fontFamily: "var(--font-num)", fontWeight: 900, fontSize: 17, color: "var(--text-strong)" }}>{done ? "✓" : Math.round(value * 100)}</span>
          </>
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: "flex", alignItems: "baseline", gap: 8 }}>
          <span style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 23, letterSpacing: "-.03em", lineHeight: 1 }}>{m.month}</span>
          <span style={{ fontFamily: "var(--font-num)", fontWeight: 700, fontSize: 14, color: "var(--text-muted)" }}>{m.year}</span>
        </div>
        <div style={{ marginTop: 6 }}>
          <Kicker>{done ? "Cleared" : m.done > 0 ? `${m.done} / ${m.total} · tap to resume` : `${m.total} photos · not started`}</Kicker>
        </div>
      </div>
      {m.reclaim ? (
        <span style={rewardSticker}>{m.reclaim}</span>
      ) : (
        <span style={{ color: "var(--text-faint)", fontSize: 22 }}>{Ip("caret-right")}</span>
      )}
    </button>
  );
}

/* ---------------- SCR-06 Selected Photos Mode ---------------- */
function Selected({ go }) {
  const K = window.KeeprDesignSystem_30a628;
  const { Button } = K;
  const shots = window.KEEPR_PHOTOS.slice(0, 6);
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div style={{ padding: "8px 18px 10px", display: "flex", alignItems: "center", gap: 12 }}>
        <button aria-label="Back to months" onClick={() => go("main")} style={window.KeeprBackBtn}>{Ip("caret-left")}</button>
        <div><Kicker color="var(--gone)">Selected photos</Kicker><div style={window.KeeprH1 || h1p}>Partial access</div></div>
      </div>
      <div style={{ padding: "0 18px 10px" }}>
        <div style={{ display: "flex", gap: 10, alignItems: "center", background: "var(--gone-wash)", border: "var(--bw) solid var(--gone)", borderRadius: "var(--r-md)", padding: "12px 14px" }}>
          {Ip("info", { color: "var(--gone)", fontSize: 20 })}
          <span style={{ fontFamily: "var(--font-ui)", fontWeight: 600, fontSize: 13, color: "var(--text-body)" }}>These selected photos are a separate cleanup from your calendar months.</span>
        </div>
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "6px 18px" }}>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 8 }}>
          {shots.map((p) => <div key={p.id} style={{ aspectRatio: "1", borderRadius: "var(--r-sm)", overflow: "hidden", border: "var(--bw) solid var(--border-hard)" }}><img src={p.src} alt={`Selected photo from ${p.date}`} style={{ width: "100%", height: "100%", objectFit: "cover" }} /></div>)}
        </div>
        <div style={{ textAlign: "center", marginTop: 14 }}><Kicker>6 photos selected</Kicker></div>
      </div>
      <div style={{ ...window.KeeprFootBar, flexDirection: "column", gap: 8 }}>
        <Button variant="keep" size="lg" full onClick={() => go("session", { month: { month: "Selected", year: "", total: 6, done: 0, partial: true } })}>Start selected cleanup</Button>
        <div style={{ display: "flex", gap: 8 }}>
          <Button variant="neutral" full onClick={() => go("selected")}>Reselect</Button>
          <Button variant="ghost" full onClick={() => go("main")}>Allow all photos</Button>
        </div>
      </div>
    </div>
  );
}

const gearBtn = { width: 48, height: 48, display: "grid", placeItems: "center", background: "var(--surface-card)", border: "var(--bw) solid var(--border-hard)", borderRadius: 99, color: "var(--text-strong)", fontSize: 21, cursor: "pointer", boxShadow: "var(--shadow-hard-sm)" };
const rewardSticker = { flex: "0 0 auto", whiteSpace: "nowrap", fontFamily: "var(--font-num)", fontWeight: 900, fontSize: 13, color: "#241800", background: "var(--reward-grad)", border: "2px solid var(--border-hard)", borderRadius: 99, padding: "6px 11px", boxShadow: "var(--shadow-hard-sm)" };
const h1p = { fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 26, letterSpacing: "-.04em", color: "var(--text-strong)", lineHeight: 1 };

Object.assign(window, { Main, Selected });
