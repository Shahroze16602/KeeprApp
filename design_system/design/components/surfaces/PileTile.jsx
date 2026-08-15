import React, { useState } from "react";

/**
 * Keepr PileTile — a collectible-looking tile for smart piles
 * (Screenshots / Blurry / Duplicates / Big Videos). Chunky sticker
 * with a stacked-card shadow so it reads as a "pack" to open.
 */
export function PileTile({
  label,
  count,
  detail,
  accent = "var(--keep-grad)",
  thumb,
  icon,
  onClick,
  style,
  ...rest
}) {
  const [pressed, setPressed] = useState(false);
  return (
    <button
      type="button"
      onClick={onClick}
      onPointerDown={() => setPressed(true)}
      onPointerUp={() => setPressed(false)}
      onPointerLeave={() => setPressed(false)}
      style={{
        position: "relative",
        display: "flex",
        flexDirection: "column",
        justifyContent: "flex-end",
        gap: 4,
        width: "100%",
        aspectRatio: "1 / 1",
        padding: "var(--pad-card)",
        textAlign: "left",
        color: "var(--text-strong)",
        background: "var(--surface-card)",
        border: "var(--bw-thick) solid var(--border-hard)",
        borderRadius: "var(--r-lg)",
        overflow: "hidden",
        cursor: "pointer",
        WebkitTapHighlightColor: "transparent",
        boxShadow: pressed ? "var(--shadow-pressed)" : "var(--shadow-hard-lg)",
        transform: pressed ? "translate(3px,4px)" : "translate(0,0)",
        transition: "transform var(--dur-snap) var(--ease-spring), box-shadow var(--dur-snap)",
        ...style,
      }}
      {...rest}
    >
      {/* thumb or accent field */}
      <div
        style={{
          position: "absolute", inset: 0,
          background: thumb ? "var(--surface-photo)" : accent,
          opacity: thumb ? 1 : 0.9,
        }}
      >
        {thumb && (
          <img src={thumb} alt="" style={{ width: "100%", height: "100%", objectFit: "cover", filter: "saturate(1.05)" }} />
        )}
      </div>
      {/* readability veil */}
      <div style={{ position: "absolute", inset: 0, background: "linear-gradient(0deg, rgba(8,8,7,0.82) 8%, rgba(8,8,7,0.15) 60%, transparent)" }} />

      {/* count sticker top-right */}
      <span
        style={{
          position: "absolute", top: 12, right: 12,
          fontFamily: "var(--font-num)", fontWeight: "var(--w-black)", fontSize: 15,
          color: "var(--text-on-accent)", background: "#fff",
          border: "2px solid var(--border-hard)", borderRadius: "var(--r-pill)",
          padding: "4px 10px", boxShadow: "var(--shadow-hard-sm)",
        }}
      >
        {count}
      </span>

      {/* icon chip */}
      {icon && (
        <span style={{
          position: "absolute", top: 12, left: 12, fontSize: 24, lineHeight: 1,
          width: 44, height: 44, display: "inline-flex", alignItems: "center", justifyContent: "center",
          color: "#fff", background: "rgba(20,17,15,0.5)", backdropFilter: "blur(8px)",
          border: "2px solid rgba(255,255,255,0.16)", borderRadius: "var(--r-md)",
        }}>{icon}</span>
      )}

      <span style={{ position: "relative", fontFamily: "var(--font-display)", fontWeight: "var(--w-black)", fontSize: 20, letterSpacing: "var(--track-snug)", color: "#fff", lineHeight: 1 }}>{label}</span>
      {detail && (
        <span style={{ position: "relative", fontFamily: "var(--font-ui)", fontWeight: "var(--w-heavy)", fontSize: 12, letterSpacing: "var(--track-label)", textTransform: "uppercase", color: "rgba(255,255,255,0.72)" }}>{detail}</span>
      )}
    </button>
  );
}
