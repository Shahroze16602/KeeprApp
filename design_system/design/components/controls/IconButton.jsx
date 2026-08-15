import React, { useState } from "react";

/**
 * Keepr IconButton — round or squircle chunky tap target.
 * Used for the deck's big KEEP / GONE / UNDO / SUPER controls.
 */
export function IconButton({
  children,
  variant = "neutral",
  size = "md",
  shape = "circle",
  disabled = false,
  onClick,
  ariaLabel,
  style,
  ...rest
}) {
  const [pressed, setPressed] = useState(false);

  const palette = {
    keep:    { bg: "var(--keep-grad)", fg: "var(--keep-ink)" },
    gone:    { bg: "var(--gone-grad)", fg: "var(--gone-ink)" },
    reward:  { bg: "var(--reward-grad)", fg: "#241800" },
    neutral: { bg: "var(--surface-card-2)", fg: "var(--text-strong)" },
    glass:   { bg: "rgba(20,17,15,0.55)", fg: "#FFFFFF" },
  }[variant] || {};

  const dim = { sm: 48, md: 56, lg: 72, xl: 84 }[size];

  const base = {
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    width: dim,
    height: dim,
    fontSize: dim * 0.44,
    color: disabled ? "var(--text-faint)" : palette.fg,
    background: disabled ? "var(--surface-card)" : palette.bg,
    border: `var(--bw) solid var(--border-hard)`,
    borderRadius: shape === "circle" ? "var(--r-pill)" : "var(--r-md)",
    cursor: disabled ? "not-allowed" : "pointer",
    userSelect: "none",
    WebkitTapHighlightColor: "transparent",
    backdropFilter: variant === "glass" ? "blur(10px)" : "none",
    boxShadow: disabled ? "none" : pressed ? "var(--shadow-pressed)" : "var(--shadow-hard)",
    transform: pressed && !disabled ? "translate(3px, 4px)" : "translate(0,0)",
    transition:
      "transform var(--dur-instant) var(--ease-spring), box-shadow var(--dur-instant) var(--ease-out)",
    ...style,
  };

  const down = () => !disabled && setPressed(true);
  const up = () => setPressed(false);

  return (
    <button
      type="button"
      aria-label={ariaLabel}
      style={base}
      disabled={disabled}
      onClick={onClick}
      onPointerDown={down}
      onPointerUp={up}
      onPointerLeave={up}
      onPointerCancel={up}
      {...rest}
    >
      {children}
    </button>
  );
}
