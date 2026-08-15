/* Keepr — Settings · Privacy · Analytics · Feedback · Rate · Reset */
const Is2 = (n, s) => <i className={"ph-fill ph-" + n} style={s} />;

function KSwitch({ on, onClick, label }) {
  return (
    <button role="switch" aria-checked={on} aria-label={label} onClick={onClick} style={{ width: 62, height: 48, flex: "0 0 auto", borderRadius: 99, border: "var(--bw) solid var(--border-hard)", background: on ? "var(--win-500)" : "var(--surface-inset)", position: "relative", cursor: "pointer", padding: 0, boxShadow: "var(--shadow-hard-sm)", transition: "background 180ms" }}>
      <span aria-hidden="true" style={{ position: "absolute", top: 7, left: on ? 30 : 4, width: 26, height: 26, borderRadius: 99, background: "#fff", border: "2px solid var(--border-hard)", transition: "left 200ms var(--ease-spring)" }} />
    </button>
  );
}
function Head({ go, back, kicker, title, to }) {
  return (
    <div style={{ padding: "10px 18px 10px", display: "flex", alignItems: "center", gap: 12 }}>
      <button aria-label={`Back from ${title}`} onClick={back || (() => go(to || "main"))} style={window.KeeprBackBtn}>{Is2("caret-left")}</button>
      <div><Kicker color="var(--keep)">{kicker}</Kicker><div style={hs}>{title}</div></div>
    </div>
  );
}
function Row({ icon, label, sub, right, onClick, danger }) {
  const Container = onClick ? "button" : "div";
  return (
    <Container onClick={onClick} style={{ display: "flex", alignItems: "center", gap: 13, width: "100%", textAlign: "left", padding: "13px 15px", background: "var(--surface-card)", border: "var(--bw) solid var(--border-hard)", borderRadius: "var(--r-md)", cursor: onClick ? "pointer" : "default", color: "var(--text-strong)" }}>
      <span style={{ width: 38, height: 38, flex: "0 0 auto", display: "grid", placeItems: "center", borderRadius: "var(--r-sm)", background: danger ? "var(--gone-wash)" : "var(--surface-inset)", color: danger ? "var(--gone)" : "var(--keep)", fontSize: 19 }}>{Is2(icon)}</span>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontFamily: "var(--font-ui)", fontWeight: 800, fontSize: 14.5, color: danger ? "var(--gone)" : "var(--text-strong)" }}>{label}</div>
        {sub && <div style={{ fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 12, color: "var(--text-muted)" }}>{sub}</div>}
      </div>
      {right || (onClick && <span style={{ color: "var(--text-faint)", fontSize: 18 }}>{Is2("caret-right")}</span>)}
    </Container>
  );
}
const sect = { fontFamily: "var(--font-ui)", fontWeight: 800, fontSize: 11, letterSpacing: ".14em", textTransform: "uppercase", color: "var(--text-muted)", margin: "18px 4px 8px" };
const hs = { fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 26, letterSpacing: "-.04em", color: "var(--text-strong)", lineHeight: 1 };

/* ---------------- SCR-14 Settings ---------------- */
function Settings({ go, state, set }) {
  const localeNames = { en: "English", es: "Español", fr: "Français", de: "Deutsch", pt: "Português", it: "Italiano", ja: "日本語", ko: "한국어", hi: "हिन्दी", ar: "العربية" };
  const [motion, setMotion] = React.useState(() => localStorage.getItem("keepr_motion") !== "reduced");
  const [haptics, setHaptics] = React.useState(() => localStorage.getItem("keepr_haptics") !== "off");
  const [dark, setDark] = React.useState(() => { const p = document.getElementById("phone"); return p ? p.getAttribute("data-theme") !== "light" : true; });
  const setTheme = (isDark) => {
    const p = document.getElementById("phone"); if (!p) return;
    if (isDark) { p.removeAttribute("data-theme"); localStorage.setItem("keepr_theme", "dark"); }
    else { p.setAttribute("data-theme", "light"); localStorage.setItem("keepr_theme", "light"); }
    setDark(isDark);
  };
  const setMotionSetting = (enabled) => {
    const p = document.getElementById("phone"); if (p) p.setAttribute("data-motion", enabled ? "full" : "reduced");
    localStorage.setItem("keepr_motion", enabled ? "full" : "reduced");
    setMotion(enabled);
  };
  const setHapticsSetting = (enabled) => {
    localStorage.setItem("keepr_haptics", enabled ? "on" : "off");
    setHaptics(enabled);
  };
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <Head go={go} kicker="Keepr" title="Settings" to="main" />
      <div className="k-hide-scroll" style={{ flex: 1, overflowY: "auto", padding: "2px 18px 26px" }}>
        <div style={sect}>General</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <Row icon="translate" label="Language" sub={localeNames[localStorage.getItem("keepr_lang") || "en"]} onClick={() => go("language-s")} />
          <Row icon="images" label="Photo access" sub="Full access" onClick={() => go("access")} />
        </div>

        <div style={sect}>Feel</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <Row icon={dark ? "moon" : "sun"} label="Dark mode" sub={dark ? "On" : "Off"} right={<KSwitch label="Dark mode" on={dark} onClick={() => setTheme(!dark)} />} />
          <Row icon="wind" label="Motion & animation" sub={motion ? "Springy card physics" : "Reduced"} right={<KSwitch label="Motion and animation" on={motion} onClick={() => setMotionSetting(!motion)} />} />
          <Row icon="vibrate" label="Haptics" sub={haptics ? "Feedback on every swipe" : "Off"} right={<KSwitch label="Haptics" on={haptics} onClick={() => setHapticsSetting(!haptics)} />} />
        </div>

        <div style={sect}>Privacy</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <Row icon="chart-bar" label="Usage analytics" sub={state.analytics ? "Sharing anonymous usage" : "Off"} onClick={() => go("analytics")} />
          <Row icon="shield-check" label="Privacy policy" onClick={() => go("privacy")} />
        </div>

        <div style={sect}>Support</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          <Row icon="chat-circle-text" label="Send feedback" onClick={() => go("feedback")} />
          <Row icon="star" label="Rate Keepr" onClick={() => go("rate")} />
          <Row icon="book-open" label="Replay intro" onClick={() => go("onboarding")} />
        </div>

        <div style={sect}>Danger zone</div>
        <Row icon="trash" label="Reset Keepr" sub="Clear local data · keeps your photos" danger onClick={() => go("reset")} />

        <div style={{ textAlign: "center", marginTop: 22 }}><KeeprWord size={20} /><div style={{ fontFamily: "var(--font-ui)", fontWeight: 600, fontSize: 11, color: "var(--text-faint)", marginTop: 4 }}>Version 1.0 · Made for your camera roll</div></div>
      </div>
    </div>
  );
}

/* ---------------- SCR-15 Analytics ---------------- */
function Analytics({ go, back, state, set }) {
  const on = !!state.analytics;
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <Head go={go} back={back} kicker="Privacy" title="Usage analytics" to="settings" />
      <div style={{ flex: 1, overflowY: "auto", padding: "6px 18px" }}>
        <div style={{ display: "grid", placeItems: "center", margin: "10px 0 18px" }}>
          <div style={{ width: 96, height: 96, borderRadius: "var(--r-2xl)", background: "var(--surface-card)", border: "var(--bw-slab) solid var(--border-hard)", display: "grid", placeItems: "center", color: "var(--keep)", fontSize: 44, boxShadow: "var(--shadow-hard)" }}>{Is2("chart-donut")}</div>
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 12, background: "var(--surface-card)", border: "var(--bw-thick) solid var(--border-hard)", borderRadius: "var(--r-lg)", padding: "15px 16px", boxShadow: "var(--shadow-hard-sm)" }}>
          <div style={{ flex: 1 }}><div style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 17, color: "var(--text-strong)" }}>Share anonymous usage</div><div style={{ fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 12.5, color: "var(--text-muted)" }}>Off by default · you're in control</div></div>
          <KSwitch label="Share anonymous usage" on={on} onClick={() => set({ analytics: !on })} />
        </div>
        <div style={{ marginTop: 16, display: "flex", flexDirection: "column", gap: 10 }}>
          {[["check-circle", "Only counts — screens seen, months finished", "var(--win)"], ["prohibit", "Never your photos, filenames, dates, or IDs", "var(--gone)"], ["arrow-u-up-left", "Turn off any time — data is reset", "var(--keep)"]].map(([ic, t, c], i) => (
            <div key={i} style={{ display: "flex", gap: 11, alignItems: "center" }}>
              <span style={{ color: c, fontSize: 22, flex: "0 0 auto" }}>{Is2(ic)}</span>
              <span style={{ fontFamily: "var(--font-ui)", fontWeight: 600, fontSize: 13.5, color: "var(--text-body)" }}>{t}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

/* ---------------- SCR-16 Feedback ---------------- */
function Feedback({ go, back }) {
  const K = window.KeeprDesignSystem_30a628;
  const { Button } = K;
  const [cat, setCat] = React.useState("Idea");
  const [message, setMessage] = React.useState("");
  const [sent, setSent] = React.useState(false);
  const cats = [["Idea", "lightbulb"], ["Bug", "bug"], ["Praise", "heart"], ["Other", "dots-three"]];
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <Head go={go} back={back} kicker="Support" title="Send feedback" to="settings" />
      <div style={{ flex: 1, overflowY: "auto", padding: "6px 18px" }}>
        <Kicker style={{ marginBottom: 8 }}>What's this about?</Kicker>
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 16 }}>
          {cats.map(([c, ic]) => { const on = cat === c; return (
            <button key={c} aria-pressed={on} onClick={() => setCat(c)} style={{ display: "inline-flex", alignItems: "center", gap: 7, minHeight: 48, padding: "0 15px", background: on ? "var(--keep-grad)" : "var(--surface-card)", color: on ? "var(--keep-ink)" : "var(--text-muted)", border: "var(--bw) solid var(--border-hard)", borderRadius: 99, cursor: "pointer", boxShadow: on ? "var(--shadow-hard-sm)" : "none", fontFamily: "var(--font-ui)", fontWeight: 800, fontSize: 13.5 }}>{Is2(ic)}{c}</button>
          ); })}
        </div>
        <label htmlFor="feedback-message" style={{ position: "absolute", width: 1, height: 1, padding: 0, margin: -1, overflow: "hidden", clip: "rect(0,0,0,0)", whiteSpace: "nowrap", border: 0 }}>Feedback message</label>
        <textarea id="feedback-message" value={message} onChange={(e) => { setMessage(e.target.value); setSent(false); }} placeholder="Tell us what you think…" style={{ width: "100%", boxSizing: "border-box", minHeight: 120, resize: "vertical", background: "var(--surface-card)", border: "var(--bw-thick) solid var(--border-hard)", borderRadius: "var(--r-md)", padding: 14, color: "var(--text-strong)", fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 15, outline: "none" }} />
        {sent && <div role="status" style={{ marginTop: 10, color: "var(--win)", fontFamily: "var(--font-ui)", fontWeight: 800, fontSize: 13 }}>Feedback is ready to share with your chosen app.</div>}
        <div style={{ marginTop: 16, background: "var(--surface-inset)", border: "var(--bw) solid var(--border-hard)", borderRadius: "var(--r-md)", padding: 14 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 8 }}>{Is2("shield-check", { color: "var(--win)", fontSize: 17 })}<Kicker>Attached diagnostics (safe)</Kicker></div>
          <div style={{ fontFamily: "var(--font-num)", fontWeight: 600, fontSize: 12, color: "var(--text-muted)", lineHeight: 1.7 }}>App 1.0 · Android 16 · Full access · 3 months · 0 errors<br /><span style={{ color: "var(--text-faint)" }}>No photos, names, dates or IDs included.</span></div>
        </div>
      </div>
      <div style={window.KeeprFootBar}><Button variant="keep" size="lg" full icon={Is2("share-network")} disabled={!message.trim()} onClick={() => setSent(true)}>Prepare feedback</Button></div>
    </div>
  );
}

/* ---------------- SCR-17 Rate-Us ---------------- */
function Rate({ go, back }) {
  const K = window.KeeprDesignSystem_30a628;
  const { Button } = K;
  const [stars, setStars] = React.useState(5);
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: 30, textAlign: "center" }}>
        <div style={{ animation: "k-pop-in 500ms var(--ease-boing) both" }}><KeeprMark size={92} glow /></div>
        <div style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 26, letterSpacing: "-.035em", color: "var(--text-strong)", marginTop: 22 }}>Enjoying Keepr?</div>
        <div style={{ fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 14.5, color: "var(--text-muted)", marginTop: 8, maxWidth: 250, lineHeight: 1.45 }}>A quick rating helps other people find a cleaner that respects them.</div>
        <div style={{ display: "flex", gap: 8, margin: "24px 0" }}>
          {[1, 2, 3, 4, 5].map((s) => (
            <button key={s} aria-label={`${s} star${s === 1 ? "" : "s"}`} aria-pressed={s === stars} onClick={() => setStars(s)} style={{ width: 48, height: 48, padding: 0, background: "none", border: "none", cursor: "pointer", fontSize: 38, color: s <= stars ? "var(--reward)" : "var(--surface-card-2)", transition: "transform 140ms var(--ease-spring)", transform: s <= stars ? "scale(1.05)" : "scale(1)" }}>{Is2("star")}</button>
          ))}
        </div>
        <div style={{ width: "100%", maxWidth: 300, display: "flex", flexDirection: "column", gap: 10 }}>
          <Button variant="reward" size="lg" full icon={Is2("google-play-logo")} onClick={() => (back ? back() : go("main"))}>Rate on Google Play</Button>
          <Button variant="ghost" full onClick={() => go("feedback")}>Send private feedback</Button>
          <Button variant="ghost" full onClick={() => (back ? back() : go("main"))}>Maybe later</Button>
        </div>
      </div>
    </div>
  );
}

/* ---------------- Privacy Policy ---------------- */
function Privacy({ go, back }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", height: "100%" }}>
      <Head go={go} back={back} kicker="Your data" title="Privacy policy" to="settings" />
      <div className="k-hide-scroll" style={{ flex: 1, overflowY: "auto", padding: "6px 18px 30px" }}>
        <div style={{ background: "var(--surface-card)", border: "var(--bw-thick) solid var(--border-hard)", borderRadius: "var(--r-lg)", boxShadow: "var(--shadow-hard-sm)", padding: 18 }}>
          <div style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 19, color: "var(--text-strong)" }}>Your photos stay on this device.</div>
          <p style={policyText}>Keepr reads the photos you allow so you can review and organize them. Photo content, filenames, dates, and media identifiers are never uploaded.</p>
          <p style={policyText}>Deletion is requested through Android system controls. Nothing is deleted until you confirm, and Keepr checks the result before reporting success.</p>
          <p style={policyText}>Anonymous usage analytics are optional and off by default. You can change that choice at any time in Settings.</p>
          <p style={policyText}>Keepr stores cleanup progress and preferences locally. Reset Keepr removes that app data without deleting your photos.</p>
        </div>
        <div style={{ marginTop: 16 }}><Kicker>Last updated · August 2, 2026</Kicker></div>
      </div>
    </div>
  );
}

/* ---------------- SCR-18 Reset Confirmation ---------------- */
function Reset({ go, back, set }) {
  const K = window.KeeprDesignSystem_30a628;
  const { Button } = K;
  return (
    <div style={{ position: "absolute", inset: 0, display: "flex", flexDirection: "column", justifyContent: "flex-end", background: "rgba(8,8,7,.72)", backdropFilter: "blur(4px)", zIndex: 30 }}>
      <div style={{ animation: "k-sheet 320ms var(--ease-spring) both", background: "var(--bg-app-2)", borderTop: "var(--bw-slab) solid var(--border-hard)", borderRadius: "34px 34px 0 0", padding: "10px 22px 26px" }}>
        <div style={{ width: 46, height: 6, borderRadius: 99, background: "var(--border-soft)", margin: "0 auto 18px" }} />
        <div style={{ display: "grid", placeItems: "center", marginBottom: 14 }}>
          <div style={{ width: 76, height: 76, borderRadius: "var(--r-lg)", background: "var(--gone-wash)", border: "var(--bw-thick) solid var(--gone)", display: "grid", placeItems: "center", color: "var(--gone)", fontSize: 36 }}>{Is2("warning")}</div>
        </div>
        <div style={{ textAlign: "center", fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 24, letterSpacing: "-.03em", color: "var(--text-strong)" }}>Reset Keepr?</div>
        <div style={{ textAlign: "center", fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 14, color: "var(--text-muted)", margin: "10px 0 16px", lineHeight: 1.45 }}>This clears your sessions, decisions, and preferences on this device. <b style={{ color: "var(--text-body)" }}>Your photos are never deleted.</b></div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <Button variant="gone" size="lg" full onClick={() => { set && set({ analytics: false }); go("splash"); }}>Reset Keepr</Button>
          <Button variant="ghost" full onClick={back || (() => go("settings"))}>Cancel</Button>
        </div>
      </div>
    </div>
  );
}

const policyText = { fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 14, lineHeight: 1.55, color: "var(--text-body)", margin: "14px 0 0" };
Object.assign(window, { Settings, Privacy, Analytics, Feedback, Rate, Reset });
