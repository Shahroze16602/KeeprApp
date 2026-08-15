import React, { useEffect, useRef, useState } from "react";

/**
 * Keepr StatNumber — enormous numeral that COUNTS UP (never fades).
 * The house style for GB reclaimed, photo counts, XP, streak totals.
 */
export function StatNumber({
  value = 0,
  prefix = "",
  suffix = "",
  label,
  decimals = 0,
  size = "var(--t-mega)",
  color = "var(--text-strong)",
  gradient = false,     // reward gold gradient text
  duration = 900,
  animate = true,
  style,
  ...rest
}) {
  const [display, setDisplay] = useState(animate ? 0 : value);
  const raf = useRef(0);

  useEffect(() => {
    const reducedMotion = typeof window !== "undefined" && (
      window.matchMedia("(prefers-reduced-motion: reduce)").matches ||
      document.querySelector('[data-motion="reduced"]')
    );
    if (!animate || reducedMotion) { setDisplay(value); return; }
    const start = performance.now();
    const from = 0;
    const ease = (t) => 1 - Math.pow(1 - t, 3);
    const tick = (now) => {
      const t = Math.max(0, Math.min(1, (now - start) / duration));
      setDisplay(from + (value - from) * ease(t));
      if (t < 1) raf.current = requestAnimationFrame(tick);
    };
    raf.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf.current);
  }, [value, duration, animate]);

  const num = display.toLocaleString(undefined, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });

  const gradStyle = gradient
    ? {
        background: "var(--reward-grad)",
        WebkitBackgroundClip: "text",
        backgroundClip: "text",
        WebkitTextFillColor: "transparent",
        color: "transparent",
      }
    : { color };

  return (
    <div aria-live="polite" aria-atomic="true" style={{ display: "inline-flex", flexDirection: "column", alignItems: "center", ...style }} {...rest}>
      <span
        style={{
          fontFamily: "var(--font-num)",
          fontWeight: "var(--w-black)",
          fontSize: size,
          letterSpacing: "var(--track-mega)",
          lineHeight: "var(--lh-tight)",
          fontVariantNumeric: "tabular-nums lining-nums",
          ...gradStyle,
        }}
      >
        {prefix}{num}{suffix}
      </span>
      {label && (
        <span style={{ marginTop: 4, fontFamily: "var(--font-ui)", fontWeight: "var(--w-heavy)", fontSize: "var(--t-label)", letterSpacing: "var(--track-label)", textTransform: "uppercase", color: "var(--text-muted)" }}>{label}</span>
      )}
    </div>
  );
}
