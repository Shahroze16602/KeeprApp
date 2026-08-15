import React from "react";

/**
 * Keepr StreakBadge — flame sticker with the running streak count.
 * Lives in the home header. Bobs gently when the streak is live.
 */
export function StreakBadge({ days = 0, live = true, style, ...rest }) {
  return (
    <div
      style={{
        display: "inline-flex", alignItems: "center", gap: 8,
        padding: "8px 14px 8px 10px",
        background: live ? "var(--reward-grad)" : "var(--surface-card-2)",
        color: live ? "#241800" : "var(--text-muted)",
        border: "var(--bw) solid var(--border-hard)",
        borderRadius: "var(--r-pill)",
        boxShadow: "var(--shadow-hard-sm)",
        ...style,
      }}
      {...rest}
    >
      <span style={{
        fontSize: 22, lineHeight: 1,
        animation: live ? "k-bob 1.6s var(--ease-spring) infinite" : "none",
        filter: live ? "none" : "grayscale(1)",
      }}>🔥</span>
      <span style={{ fontFamily: "var(--font-num)", fontWeight: "var(--w-black)", fontSize: 20, letterSpacing: "var(--track-mega)", lineHeight: 1 }}>{days}</span>
      <span style={{ fontFamily: "var(--font-ui)", fontWeight: "var(--w-heavy)", fontSize: 11, letterSpacing: "var(--track-label)", textTransform: "uppercase", opacity: 0.8 }}>day{days === 1 ? "" : "s"}</span>
    </div>
  );
}
