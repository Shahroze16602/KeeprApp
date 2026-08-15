import React from "react";

/**
 * Keepr Stamp — the KEEP / GONE sticker that slams onto a card.
 * Standalone so it can be reused in reviews, toasts, and summaries.
 */
export function Stamp({ kind = "keep", size = "md", slam = false, style, ...rest }) {
  const cfg = {
    keep: { text: "KEEP", bg: "var(--keep)", fg: "var(--keep-ink)", rot: -12 },
    gone: { text: "GONE", bg: "var(--gone)", fg: "var(--gone-ink)", rot: 12 },
    super:{ text: "SUPER", bg: "var(--reward-grad)", fg: "#241800", rot: -8 },
  }[kind];

  const fs = { sm: 22, md: 40, lg: 64 }[size];

  return (
    <span
      style={{
        display: "inline-flex", alignItems: "center", justifyContent: "center",
        fontFamily: "var(--font-display)", fontWeight: "var(--w-black)", fontSize: fs,
        letterSpacing: "var(--track-tight)", textTransform: "uppercase",
        color: cfg.fg, background: cfg.bg,
        padding: `${fs * 0.14}px ${fs * 0.5}px`,
        borderRadius: "var(--r-sticker)",
        border: `var(--bw-thick) solid var(--border-hard)`,
        boxShadow: "var(--shadow-hard)",
        transform: `rotate(${cfg.rot}deg)`,
        animation: slam ? "k-slam var(--dur-settle) var(--ease-boing) both" : "none",
        ...style,
      }}
      {...rest}
    >
      {cfg.text}
    </span>
  );
}
