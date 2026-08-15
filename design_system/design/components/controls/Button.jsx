import React, { useState } from "react";

/**
 * Keepr Button — chunky, pressable, hard-offset depth.
 * It physically depresses into its shadow on press.
 */
export function Button({
  children,
  variant = "keep",
  size = "md",
  full = false,
  disabled = false,
  icon = null,
  iconRight = null,
  onClick,
  style,
  ...rest
}) {
  const [pressed, setPressed] = useState(false);

  const palette = {
    keep:    { bg: "var(--keep-grad)", fg: "var(--keep-ink)" },
    gone:    { bg: "var(--gone-grad)", fg: "var(--gone-ink)" },
    reward:  { bg: "var(--reward-grad)", fg: "#241800" },
    neutral: { bg: "var(--surface-card-2)", fg: "var(--text-strong)" },
    ghost:   { bg: "transparent", fg: "var(--text-strong)" },
  }[variant] || {};

  const sizes = {
    sm: { h: "var(--control-h-sm)", px: "18px", fs: "14px" },
    md: { h: "var(--control-h)", px: "26px", fs: "16px" },
    lg: { h: "64px", px: "34px", fs: "19px" },
  }[size];

  const isGhost = variant === "ghost";

  const base = {
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "10px",
    width: full ? "100%" : "auto",
    height: sizes.h,
    padding: `0 ${sizes.px}`,
    fontFamily: "var(--font-ui)",
    fontWeight: "var(--w-heavy)",
    fontSize: sizes.fs,
    letterSpacing: "var(--track-snug)",
    textTransform: "uppercase",
    color: disabled ? "var(--text-faint)" : palette.fg,
    background: disabled ? "var(--surface-card)" : palette.bg,
    border: `var(--bw) solid ${isGhost ? "var(--border-hard)" : "var(--border-hard)"}`,
    borderRadius: "var(--r-pill)",
    cursor: disabled ? "not-allowed" : "pointer",
    userSelect: "none",
    WebkitTapHighlightColor: "transparent",
    boxShadow: disabled
      ? "none"
      : pressed
      ? "var(--shadow-pressed)"
      : "var(--shadow-hard)",
    transform: pressed && !disabled ? "translate(3px, 4px)" : "translate(0,0)",
    transition:
      "transform var(--dur-instant) var(--ease-spring), box-shadow var(--dur-instant) var(--ease-out), filter var(--dur-snap)",
    ...style,
  };

  const down = () => !disabled && setPressed(true);
  const up = () => setPressed(false);

  return (
    <button
      type="button"
      style={base}
      disabled={disabled}
      onClick={onClick}
      onPointerDown={down}
      onPointerUp={up}
      onPointerLeave={up}
      onPointerCancel={up}
      {...rest}
    >
      {icon}
      {children}
      {iconRight}
    </button>
  );
}
