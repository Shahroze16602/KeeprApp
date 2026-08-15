import React, { useState } from "react";

/**
 * Keepr LevelCard — a month "bucket" rendered as a game level.
 * Progress ring, count, size to reclaim, and active/done state.
 * Self-contained ring so it drops in anywhere.
 */
export function LevelCard({
  month,
  year,
  total = 0,
  done = 0,
  reclaim,
  state = "active",     // "active" | "done"
  level,
  onClick,
  style,
  ...rest
}) {
  const [pressed, setPressed] = useState(false);
  const value = total > 0 ? Math.min(1, done / total) : state === "done" ? 1 : 0;
  const size = 68, thickness = 9, r = (size - thickness) / 2, c = 2 * Math.PI * r;
  const ringColor = state === "done" ? "var(--win)" : "var(--keep)";

  return (
    <button
      type="button"
      onClick={onClick}
      onPointerDown={() => setPressed(true)}
      onPointerUp={() => setPressed(false)}
      onPointerLeave={() => setPressed(false)}
      onPointerCancel={() => setPressed(false)}
      style={{
        display: "flex", alignItems: "center", gap: "var(--gap)", width: "100%",
        padding: "var(--pad-card)", textAlign: "left",
        background: state === "done" ? "var(--surface-card-2)" : "var(--surface-card)",
        color: "var(--text-strong)",
        border: "var(--bw-thick) solid var(--border-hard)",
        borderRadius: "var(--r-lg)",
        cursor: "pointer",
        WebkitTapHighlightColor: "transparent",
        boxShadow: pressed ? "var(--shadow-pressed)" : "var(--shadow-hard)",
        transform: pressed ? "translate(2px,3px)" : "translate(0,0)",
        transition: "transform var(--dur-snap) var(--ease-spring), box-shadow var(--dur-snap)",
        ...style,
      }}
      {...rest}
    >
      {/* ring */}
      <div style={{ position: "relative", width: size, height: size, flex: "0 0 auto", display: "grid", placeItems: "center" }}>
        <svg width={size} height={size} style={{ transform: "rotate(-90deg)" }}>
          <circle cx={size/2} cy={size/2} r={r} fill="none" stroke="var(--surface-inset)" strokeWidth={thickness} />
          <circle cx={size/2} cy={size/2} r={r} fill="none" stroke={ringColor} strokeWidth={thickness}
            strokeLinecap="round" strokeDasharray={c} strokeDashoffset={c * (1 - value)}
            style={{ transition: "stroke-dashoffset var(--dur-settle) var(--ease-spring)" }} />
        </svg>
        <span style={{ position: "absolute", fontFamily: "var(--font-num)", fontWeight: "var(--w-black)", fontSize: 16, letterSpacing: "var(--track-mega)", color: "var(--text-strong)" }}>
          {state === "done" ? "✓" : Math.round(value * 100)}
        </span>
      </div>

      {/* text block */}
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: "flex", alignItems: "baseline", gap: 8 }}>
          <span style={{ fontFamily: "var(--font-display)", fontWeight: "var(--w-black)", fontSize: 24, letterSpacing: "var(--track-tight)", lineHeight: 1 }}>{month}</span>
          {year && <span style={{ fontFamily: "var(--font-num)", fontWeight: "var(--w-bold)", fontSize: 15, color: "var(--text-muted)" }}>{year}</span>}
        </div>
        <div style={{ marginTop: 6, fontFamily: "var(--font-ui)", fontWeight: "var(--w-heavy)", fontSize: 12, letterSpacing: "var(--track-label)", textTransform: "uppercase", color: "var(--text-muted)" }}>
          {state === "done" ? "Cleared" : `${done} / ${total} sorted`}
        </div>
      </div>

      {/* reclaim sticker */}
      {reclaim && (
        <span style={{
          flex: "0 0 auto", fontFamily: "var(--font-num)", fontWeight: "var(--w-black)", fontSize: 14,
          color: "#241800", background: "var(--reward-grad)",
          border: "2px solid var(--border-hard)", borderRadius: "var(--r-pill)",
          padding: "6px 12px", boxShadow: "var(--shadow-hard-sm)",
        }}>{reclaim}</span>
      )}
      {level != null && (
        <span style={{
          flex: "0 0 auto", fontFamily: "var(--font-num)", fontWeight: "var(--w-black)", fontSize: 13,
          color: "var(--text-muted)",
        }}>LV{level}</span>
      )}
    </button>
  );
}
