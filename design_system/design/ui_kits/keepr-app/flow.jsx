/* Keepr — first-run flow: Splash · Language · Onboarding · Media Access */
const I = (n, s) => <i className={"ph-fill ph-" + n} style={s} />;

/* ---------------- SCR-01 Splash ---------------- */
function Splash({ go, next }) {
  const [fill, setFill] = React.useState(0);
  React.useEffect(() => {
    const t1 = setTimeout(() => setFill(1), 120);
    const t2 = setTimeout(() => go(next || "main"), 2000);
    return () => { clearTimeout(t1); clearTimeout(t2); };
  }, []);
  return (
    <div role="button" tabIndex={0} aria-label="Continue to language selection" onClick={() => go(next || "main")} onKeyDown={(e) => { if (e.key === "Enter" || e.key === " ") go(next || "main"); }} style={{ position: "absolute", inset: 0, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: 30, cursor: "pointer", background: "radial-gradient(130% 80% at 50% 30%, rgba(255,120,40,.20), var(--bg-app) 60%)" }}>
      <div style={{ animation: "k-pop-in 620ms var(--ease-boing) both" }}>
        <div style={{ animation: "k-bob 3s var(--ease-out) 700ms infinite" }}><KeeprMark size={128} glow /></div>
      </div>
      <div style={{ marginTop: 26, animation: "k-pop-in 500ms var(--ease-boing) 160ms both" }}><KeeprWord size={46} /></div>
      <div style={{ marginTop: 12, opacity: fill, transition: "opacity 500ms 300ms" }}><Kicker>Finish your camera roll</Kicker></div>
      <div style={{ position: "absolute", left: 40, right: 40, bottom: 54 }}>
        <div style={{ height: 10, background: "var(--surface-inset)", border: "var(--bw) solid var(--border-hard)", borderRadius: 99, overflow: "hidden" }}>
          <div style={{ height: "100%", width: fill ? "100%" : "8%", background: "var(--keep-grad)", borderRadius: 99, transition: "width 1800ms var(--ease-out)" }} />
        </div>
      </div>
    </div>
  );
}

/* ---------------- SCR-02 Language ---------------- */
function Language({ go, back, standalone }) {
  const K = window.KeeprDesignSystem_30a628;
  const { Button } = K;
  const [sel, setSel] = React.useState(() => localStorage.getItem("keepr_lang") || "en");
  const langs = [
    ["en", "English", "English"], ["es", "Español", "Spanish"], ["fr", "Français", "French"],
    ["de", "Deutsch", "German"], ["pt", "Português", "Portuguese"], ["it", "Italiano", "Italian"],
    ["ja", "日本語", "Japanese"], ["ko", "한국어", "Korean"], ["hi", "हिन्दी", "Hindi"], ["ar", "العربية", "Arabic"],
  ];
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div style={{ padding: "8px 18px 12px", display: "flex", alignItems: "center", gap: 12 }}>
        {standalone && <button aria-label="Back to settings" onClick={back} style={backBtn}>{I("caret-left")}</button>}
        <div>
          <Kicker>{standalone ? "Settings" : "Step 1 of 3"}</Kicker>
          <div style={h1Style}>Your language</div>
        </div>
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "4px 18px 12px" }}>
        <div style={{ display: "flex", alignItems: "center", gap: 8, color: "var(--text-muted)", fontFamily: "var(--font-ui)", fontWeight: 600, fontSize: 13, marginBottom: 14 }}>
          {I("translate", { fontSize: 18 })} Keepr follows your device language when supported.
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {langs.map(([id, native, en]) => {
            const on = sel === id;
            return (
              <button key={id} lang={id} aria-pressed={on} onClick={() => { setSel(id); localStorage.setItem("keepr_lang", id); const phone = document.getElementById("phone"); if (phone) { phone.dir = id === "ar" ? "rtl" : "ltr"; phone.lang = id; } }} style={{ display: "flex", alignItems: "center", gap: 14, textAlign: id === "ar" ? "right" : "left", padding: "14px 16px", minHeight: 64, background: on ? "var(--surface-card-2)" : "var(--surface-card)", border: `var(--bw-thick) solid ${on ? "var(--keep)" : "var(--border-hard)"}`, borderRadius: "var(--r-lg)", boxShadow: on ? "var(--shadow-hard-sm)" : "none", cursor: "pointer", transition: "all 140ms var(--ease-spring)" }}>
                <div style={{ flex: 1 }}>
                  <div style={{ fontFamily: "var(--font-display)", fontWeight: 800, fontSize: 18, color: "var(--text-strong)" }}>{native}</div>
                  <div style={{ fontFamily: "var(--font-ui)", fontWeight: 600, fontSize: 12, color: "var(--text-muted)" }}>{en}</div>
                </div>
                <span style={{ width: 26, height: 26, borderRadius: 99, display: "grid", placeItems: "center", background: on ? "var(--keep-grad)" : "transparent", border: `2px solid ${on ? "var(--border-hard)" : "var(--border-soft)"}`, color: "var(--keep-ink)", fontSize: 15 }}>{on && <i className="ph-bold ph-check" />}</span>
              </button>
            );
          })}
        </div>
      </div>
      <div style={footBar}><Button variant="keep" size="lg" full iconRight={I("arrow-right")} onClick={() => standalone ? back() : go("onboarding")}>{standalone ? "Save" : "Continue"}</Button></div>
    </div>
  );
}

/* ---------------- SCR-03 Onboarding ---------------- */
function Onboarding({ go }) {
  const K = window.KeeprDesignSystem_30a628;
  const { Button } = K;
  const [i, setI] = React.useState(0);
  const panels = [
    { art: <MonthArt />, kick: "The ritual", title: "One month at a time", body: "Pick a calendar month and finish it. No endless feed — a bounded, beatable pile you can actually clear." },
    { art: <SwipeArt />, kick: "The controls", title: "Right keeps. Left deletes.", body: "Flick each photo — or tap the big buttons. Undo any decision instantly. Speed never means losing something." },
    { art: <SafeArt />, kick: "The promise", title: "Review before anything goes", body: "You confirm every deletion before it happens. Every month is free — no ads, purchases, or swipe limits. Photos stay on your device." },
  ];
  const p = panels[i];
  const last = i === panels.length - 1;
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "10px 18px" }}>
        <KeeprWord size={24} />
        {!last && <button onClick={() => go("access")} style={{ background: "none", border: "none", color: "var(--text-muted)", fontFamily: "var(--font-ui)", fontWeight: 800, fontSize: 13, letterSpacing: ".1em", textTransform: "uppercase", cursor: "pointer" }}>Skip</button>}
      </div>
      <div key={i} style={{ flex: 1, display: "flex", flexDirection: "column", justifyContent: "center", padding: "0 26px", animation: "k-pop-in 420ms var(--ease-out) both" }}>
        <div style={{ height: 250, display: "grid", placeItems: "center", marginBottom: 30 }}>{p.art}</div>
        <Kicker color="var(--keep)">{p.kick}</Kicker>
        <div style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 32, letterSpacing: "-.04em", lineHeight: 1.02, color: "var(--text-strong)", margin: "10px 0 12px" }}>{p.title}</div>
        <div style={{ fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 15.5, lineHeight: 1.5, color: "var(--text-body)", textWrap: "pretty" }}>{p.body}</div>
      </div>
      <div style={{ padding: "0 26px 6px", display: "flex", gap: 8 }}>
        {panels.map((_, k) => <span key={k} style={{ height: 8, flex: k === i ? "0 0 34px" : "0 0 8px", borderRadius: 99, background: k === i ? "var(--keep-grad)" : "var(--surface-card-2)", border: "2px solid var(--border-hard)", transition: "flex 260ms var(--ease-spring)" }} />)}
      </div>
      <div style={footBar}>
        <Button variant="keep" size="lg" full iconRight={I(last ? "arrow-right" : "arrow-right")} onClick={() => last ? go("access") : setI(i + 1)}>{last ? "Get started" : "Next"}</Button>
      </div>
    </div>
  );
}
function MonthArt() {
  const cells = Array.from({ length: 12 });
  return (
    <div style={{ position: "relative" }}>
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4,34px)", gap: 8, padding: 18, background: "var(--surface-card)", border: "var(--bw-slab) solid var(--border-hard)", borderRadius: "var(--r-lg)", boxShadow: "var(--shadow-hard-lg)" }}>
        {cells.map((_, k) => <div key={k} style={{ height: 34, borderRadius: 9, background: k === 2 ? "var(--keep-grad)" : "var(--surface-inset)", border: "2px solid var(--border-hard)", display: "grid", placeItems: "center", color: "var(--keep-ink)", fontSize: 16 }}>{k === 2 && <i className="ph-bold ph-check" />}</div>)}
      </div>
      <span style={{ position: "absolute", top: -14, right: -14, transform: "rotate(8deg)", background: "var(--reward-grad)", color: "#241800", fontFamily: "var(--font-num)", fontWeight: 900, fontSize: 13, padding: "6px 12px", borderRadius: 99, border: "var(--bw) solid var(--border-hard)", boxShadow: "var(--shadow-hard-sm)" }}>MARCH</span>
    </div>
  );
}
function SwipeArt() {
  return (
    <div style={{ position: "relative", width: 200, height: 230 }}>
      <div style={{ position: "absolute", inset: "18px 30px", background: "var(--surface-card-2)", border: "var(--bw-thick) solid var(--border-hard)", borderRadius: "var(--r-xl)", transform: "rotate(-6deg)" }} />
      <div style={{ position: "absolute", inset: "6px 14px", borderRadius: "var(--r-xl)", overflow: "hidden", border: "var(--bw-slab) solid var(--border-hard)", boxShadow: "var(--shadow-hard-lg)", transform: "rotate(7deg)" }}>
        <img src="https://picsum.photos/seed/keepr-swipe/400/500" alt="Example camera-roll photo" style={{ width: "100%", height: "100%", objectFit: "cover" }} />
        <div style={{ position: "absolute", inset: 0, background: "var(--keep-wash)" }} />
        <span style={{ position: "absolute", top: 18, left: 14, transform: "rotate(-12deg)", background: "var(--keep)", color: "var(--keep-ink)", fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 26, padding: "4px 14px", borderRadius: "var(--r-sticker)", border: "var(--bw-thick) solid var(--border-hard)", boxShadow: "var(--shadow-hard)" }}>KEEP</span>
      </div>
      <span style={{ position: "absolute", left: -8, bottom: 30, width: 46, height: 46, borderRadius: 99, background: "var(--gone-grad)", border: "var(--bw) solid var(--border-hard)", display: "grid", placeItems: "center", color: "var(--gone-ink)", fontSize: 22, boxShadow: "var(--shadow-hard-sm)" }}>{I("trash")}</span>
      <span style={{ position: "absolute", right: -8, top: 40, width: 46, height: 46, borderRadius: 99, background: "var(--keep-grad)", border: "var(--bw) solid var(--border-hard)", display: "grid", placeItems: "center", color: "var(--keep-ink)", fontSize: 22, boxShadow: "var(--shadow-hard-sm)" }}>{I("heart")}</span>
    </div>
  );
}
function SafeArt() {
  return (
    <div style={{ position: "relative", display: "grid", placeItems: "center" }}>
      <div style={{ width: 150, height: 150, borderRadius: "var(--r-2xl)", background: "var(--surface-card)", border: "var(--bw-slab) solid var(--border-hard)", boxShadow: "var(--shadow-hard-lg)", display: "grid", placeItems: "center", color: "var(--win)", fontSize: 74 }}>{I("shield-check")}</div>
      <span style={{ position: "absolute", bottom: -12, background: "var(--win-500)", color: "#04140A", fontFamily: "var(--font-ui)", fontWeight: 900, fontSize: 12, letterSpacing: ".1em", padding: "7px 14px", borderRadius: 99, border: "var(--bw) solid var(--border-hard)", boxShadow: "var(--shadow-hard-sm)", textTransform: "uppercase" }}>Nothing deleted yet</span>
    </div>
  );
}

/* ---------------- SCR-04 Media Access ---------------- */
function MediaAccess({ go }) {
  const K = window.KeeprDesignSystem_30a628;
  const { Button } = K;
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div style={{ flex: 1, overflowY: "auto", padding: "20px 24px 8px" }}>
        <div style={{ display: "grid", placeItems: "center", margin: "12px 0 22px" }}>
          <div style={{ position: "relative", width: 130, height: 130, borderRadius: "var(--r-2xl)", background: "var(--keep-grad)", border: "var(--bw-slab) solid var(--border-hard)", boxShadow: "var(--shadow-hard-lg)", display: "grid", placeItems: "center", color: "var(--keep-ink)", fontSize: 62 }}>
            {I("images-square")}
            <span style={{ position: "absolute", bottom: -10, right: -10, width: 48, height: 48, borderRadius: 99, background: "var(--surface-card)", border: "var(--bw) solid var(--border-hard)", display: "grid", placeItems: "center", color: "var(--win)", fontSize: 24 }}>{I("lock-simple")}</span>
          </div>
        </div>
        <Kicker color="var(--keep)">Photo access</Kicker>
        <div style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 27, letterSpacing: "-.035em", lineHeight: 1.05, color: "var(--text-strong)", margin: "8px 0 12px" }}>Let Keepr see your camera roll</div>
        <div style={{ fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 15, lineHeight: 1.5, color: "var(--text-body)", textWrap: "pretty" }}>Keepr shows your photos one month at a time and deletes only the ones you choose.</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10, margin: "18px 0 4px" }}>
          {[["device-mobile-camera", "Your photos stay on this device", "Nothing is uploaded or backed up."], ["hand-tap", "You approve every deletion", "Android confirms before anything is removed."], ["prohibit", "No account, no cloud", "Keepr never asks you to sign in."]].map(([ic, t, s], k) => (
            <div key={k} style={{ display: "flex", gap: 12, alignItems: "center", background: "var(--surface-card)", border: "var(--bw) solid var(--border-hard)", borderRadius: "var(--r-md)", padding: "12px 14px" }}>
              <span style={{ width: 40, height: 40, flex: "0 0 auto", display: "grid", placeItems: "center", borderRadius: "var(--r-sm)", background: "var(--surface-inset)", color: "var(--win)", fontSize: 21 }}>{I(ic)}</span>
              <div><div style={{ fontFamily: "var(--font-ui)", fontWeight: 800, fontSize: 14, color: "var(--text-strong)", lineHeight: 1.2 }}>{t}</div><div style={{ fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 12.5, color: "var(--text-muted)", lineHeight: 1.25, marginTop: 2 }}>{s}</div></div>
            </div>
          ))}
        </div>
      </div>
      <div style={{ ...footBar, gap: 8, flexDirection: "column" }}>
        <Button variant="keep" size="lg" full icon={I("images")} onClick={() => go("main")}>Allow photo access</Button>
        <div style={{ display: "flex", gap: 8 }}>
          <Button variant="neutral" full onClick={() => go("selected")}>Choose photos</Button>
          <Button variant="ghost" full onClick={() => go("permission-denied")}>Not now</Button>
        </div>
      </div>
    </div>
  );
}

const backBtn = { width: 48, height: 48, flex: "0 0 auto", display: "grid", placeItems: "center", background: "var(--surface-card)", border: "var(--bw) solid var(--border-hard)", borderRadius: 99, color: "var(--text-strong)", fontSize: 20, cursor: "pointer", boxShadow: "var(--shadow-hard-sm)" };
const h1Style = { fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 26, letterSpacing: "-.04em", color: "var(--text-strong)", lineHeight: 1 };
const footBar = { padding: "12px 18px 20px", display: "flex", borderTop: "var(--bw-thin) solid var(--border-soft)", background: "var(--bg-app)" };

Object.assign(window, { Splash, Language, Onboarding, MediaAccess, KeeprBackBtn: backBtn, KeeprFootBar: footBar });
