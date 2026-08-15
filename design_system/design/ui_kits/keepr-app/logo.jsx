/* Keepr — brand mark, wordmark, shared UI atoms. Exported to window. */

/* The Keepr mark: two chunky fanned decision cards — cool GONE behind,
   warm KEEP in front with a confident check. Encodes the whole ritual. */
function KeeprMark({ size = 96, glow = false, style }) {
  const uid = React.useRef("km" + Math.random().toString(36).slice(2, 7)).current;
  return (
    <svg width={size} height={size} viewBox="0 0 100 100" fill="none"
      style={{ overflow: "visible", filter: glow ? "drop-shadow(0 12px 30px rgba(255,120,40,.5))" : "none", ...style }}>
      <defs>
        <linearGradient id={uid + "k"} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#FFB13C" /><stop offset=".55" stopColor="#FF6B2C" /><stop offset="1" stopColor="#F04E12" />
        </linearGradient>
        <linearGradient id={uid + "g"} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#3ED8FF" /><stop offset=".5" stopColor="#00B8F0" /><stop offset="1" stopColor="#2E6BFF" />
        </linearGradient>
      </defs>
      {/* back card — GONE */}
      <g transform="rotate(-13 50 54)">
        <rect x="20" y="26" width="52" height="62" rx="15" fill={`url(#${uid}g)`} stroke="#000" strokeWidth="6" />
        <rect x="30" y="52" width="24" height="7" rx="3.5" fill="#fff" opacity=".85" />
        <rect x="30" y="64" width="15" height="7" rx="3.5" fill="#fff" opacity=".55" />
      </g>
      {/* front card — KEEP */}
      <g transform="rotate(9 52 50)">
        <rect x="30" y="14" width="54" height="64" rx="16" fill={`url(#${uid}k)`} stroke="#000" strokeWidth="6" />
        <path d="M43 47 L53 57 L72 34" stroke="#fff" strokeWidth="9" strokeLinecap="round" strokeLinejoin="round" fill="none" />
      </g>
    </svg>
  );
}

/* Wordmark — heavy Archivo with the signature gradient dot accent. */
function KeeprWord({ size = 34, light = false, style }) {
  return (
    <span style={{ fontFamily: "var(--font-display)", fontWeight: 900, fontSize: size, letterSpacing: "-.05em", lineHeight: 1, color: light ? "#fff" : "var(--text-strong)", display: "inline-flex", alignItems: "flex-end", ...style }}>
      Keepr
      <span style={{ display: "inline-block", width: size * 0.16, height: size * 0.16, borderRadius: 99, background: "var(--keep-grad)", border: "2px solid var(--border-hard)", marginLeft: size * 0.05, marginBottom: size * 0.12 }} />
    </span>
  );
}

/* Full lockup — mark over wordmark, centered. */
function KeeprLogo({ mark = 84, word = 40, glow = true, tagline, style }) {
  return (
    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 16, ...style }}>
      <KeeprMark size={mark} glow={glow} />
      <KeeprWord size={word} />
      {tagline && <div style={{ fontFamily: "var(--font-ui)", fontWeight: 700, fontSize: 13, letterSpacing: ".14em", textTransform: "uppercase", color: "var(--text-muted)" }}>{tagline}</div>}
    </div>
  );
}

/* Small-caps label used everywhere. */
function Kicker({ children, color = "var(--text-muted)", style }) {
  return <div style={{ fontFamily: "var(--font-ui)", fontWeight: 800, fontSize: 11, letterSpacing: ".14em", textTransform: "uppercase", color, ...style }}>{children}</div>;
}

Object.assign(window, { KeeprMark, KeeprWord, KeeprLogo, Kicker });
