import React from "react";

/**
 * Keepr ComboCounter — the "x3 COMBO!" readout for fast consecutive
 * swipes. Scales with the multiplier and pops on each increment.
 */
export function ComboCounter({ combo = 0, style, ...rest }) {
  if (combo < 2) return null;
  const heat = Math.min(1, (combo - 2) / 8);
  return (
    <div
      key={combo}
      style={{
        display: "inline-flex", flexDirection: "column", alignItems: "center", lineHeight: 0.9,
        animation: "k-pop-in var(--dur-base) var(--ease-boing) both",
        ...style,
      }}
      {...rest}
    >
      <span style={{
        fontFamily: "var(--font-num)", fontWeight: "var(--w-black)",
        fontSize: 40 + heat * 26, letterSpacing: "var(--track-mega)",
        color: "#241800",
        background: "var(--reward-grad)",
        WebkitBackgroundClip: "text", backgroundClip: "text", WebkitTextFillColor: "transparent",
        WebkitTextStroke: "2px var(--border-hard)",
        transform: `rotate(${-3 - heat * 3}deg)`,
      }}>×{combo}</span>
      <span style={{
        fontFamily: "var(--font-display)", fontWeight: "var(--w-black)", fontSize: 14 + heat * 6,
        letterSpacing: "var(--track-label)", textTransform: "uppercase", color: "var(--reward)",
        transform: `rotate(${-3 - heat * 3}deg)`,
      }}>combo</span>
    </div>
  );
}
