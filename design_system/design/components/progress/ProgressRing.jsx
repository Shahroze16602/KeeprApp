import React from "react";

/**
 * Keepr ProgressRing — thick, rounded-cap ring that fills with a
 * satisfying overshoot. Optional center numeral. Gradient stroke on
 * reward surfaces. This is the level/month completion meter.
 */
export function ProgressRing({
  value = 0,            // 0..1
  size = 96,
  thickness = 12,
  color = "var(--keep)",
  track = "var(--surface-inset)",
  gradient = false,     // use reward gradient stroke
  showValue = false,
  label,
  children,
  style,
  ...rest
}) {
  const v = Math.max(0, Math.min(1, value));
  const r = (size - thickness) / 2;
  const c = 2 * Math.PI * r;
  const gid = React.useId ? React.useId().replace(/:/g, "") : "kr" + Math.round(Math.random() * 1e6);

  return (
    <div
      style={{ position: "relative", width: size, height: size, display: "inline-flex", alignItems: "center", justifyContent: "center", ...style }}
      {...rest}
    >
      <svg width={size} height={size} style={{ transform: "rotate(-90deg)", overflow: "visible" }}>
        {gradient && (
          <defs>
            <linearGradient id={gid} x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" stopColor="#FFE24D" />
              <stop offset="55%" stopColor="#FFB800" />
              <stop offset="100%" stopColor="#FF7A00" />
            </linearGradient>
          </defs>
        )}
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={track} strokeWidth={thickness} />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke={gradient ? `url(#${gid})` : color}
          strokeWidth={thickness}
          strokeLinecap="round"
          strokeDasharray={c}
          strokeDashoffset={c * (1 - v)}
          style={{ transition: "stroke-dashoffset var(--dur-settle) var(--ease-spring)" }}
        />
      </svg>
      <div style={{ position: "absolute", inset: 0, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 2 }}>
        {children}
        {showValue && !children && (
          <span style={{ fontFamily: "var(--font-num)", fontWeight: "var(--w-black)", fontSize: size * 0.26, color: "var(--text-strong)", letterSpacing: "var(--track-mega)" }}>
            {Math.round(v * 100)}
          </span>
        )}
        {label && (
          <span style={{ fontFamily: "var(--font-ui)", fontWeight: "var(--w-heavy)", fontSize: 10, letterSpacing: "var(--track-label)", textTransform: "uppercase", color: "var(--text-muted)" }}>{label}</span>
        )}
      </div>
    </div>
  );
}
