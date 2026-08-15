import React from "react";

/**
 * Keepr Badge — sticker label (counts, tags, NEW) and the large
 * circular milestone badge (100 / 1,000 / 10,000 photos).
 */
export function Badge({
  children,
  tone = "neutral",
  variant = "pill",     // "pill" | "milestone"
  icon,
  label,
  locked = false,
  style,
  ...rest
}) {
  const tones = {
    keep:    { bg: "var(--keep-grad)", fg: "var(--keep-ink)" },
    gone:    { bg: "var(--gone-grad)", fg: "var(--gone-ink)" },
    reward:  { bg: "var(--reward-grad)", fg: "#241800" },
    win:     { bg: "var(--win)", fg: "#04160C" },
    neutral: { bg: "var(--surface-card-2)", fg: "var(--text-strong)" },
    ink:     { bg: "var(--border-hard)", fg: "#fff" },
  }[tone];

  if (variant === "milestone") {
    return (
      <div
        style={{
          position: "relative", width: 116, height: 116, display: "grid", placeItems: "center",
          borderRadius: "var(--r-pill)",
          background: locked ? "var(--surface-card)" : tones.bg,
          border: "var(--bw-slab) solid var(--border-hard)",
          boxShadow: locked ? "var(--shadow-hard)" : "var(--shadow-hard-lg)",
          color: locked ? "var(--text-faint)" : tones.fg,
          filter: locked ? "grayscale(0.4)" : "none",
          ...style,
        }}
        {...rest}
      >
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", lineHeight: 0.92 }}>
          {icon && <span style={{ fontSize: 26, marginBottom: 2 }}>{locked ? "🔒" : icon}</span>}
          <span style={{ fontFamily: "var(--font-num)", fontWeight: "var(--w-black)", fontSize: 26, letterSpacing: "var(--track-mega)" }}>{children}</span>
          {label && <span style={{ fontFamily: "var(--font-ui)", fontWeight: "var(--w-heavy)", fontSize: 9, letterSpacing: "var(--track-label)", textTransform: "uppercase", marginTop: 3, opacity: 0.85 }}>{label}</span>}
        </div>
      </div>
    );
  }

  return (
    <span
      style={{
        display: "inline-flex", alignItems: "center", gap: 6,
        fontFamily: "var(--font-ui)", fontWeight: "var(--w-heavy)", fontSize: 12,
        letterSpacing: "var(--track-label)", textTransform: "uppercase",
        color: tones.fg, background: tones.bg,
        padding: "5px 11px", borderRadius: "var(--r-pill)",
        border: "2px solid var(--border-hard)", boxShadow: "var(--shadow-hard-sm)",
        ...style,
      }}
      {...rest}
    >
      {icon}{children}
    </span>
  );
}
