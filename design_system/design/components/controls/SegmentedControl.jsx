import React from "react";

/**
 * Keepr SegmentedControl — chunky pill toggle. Used for filters
 * (All / Screenshots / Videos), light/dark, or view switches.
 * The active thumb is a solid sticker that sits inside the track.
 */
export function SegmentedControl({
  options = [],
  value,
  onChange,
  size = "md",
  style,
  ...rest
}) {
  const h = { sm: 40, md: 48 }[size];

  const track = {
    display: "inline-flex",
    alignItems: "center",
    gap: "4px",
    height: h,
    padding: "4px",
    background: "var(--surface-inset)",
    border: `var(--bw) solid var(--border-hard)`,
    borderRadius: "var(--r-pill)",
    boxShadow: "var(--shadow-hard-sm)",
    ...style,
  };

  return (
    <div role="tablist" style={track} {...rest}>
      {options.map((opt) => {
        const val = typeof opt === "string" ? opt : opt.value;
        const label = typeof opt === "string" ? opt : opt.label;
        const active = val === value;
        return (
          <button
            key={val}
            role="tab"
            aria-selected={active}
            onClick={() => onChange && onChange(val)}
            style={{
              height: h - 12,
              padding: "0 16px",
              display: "inline-flex",
              alignItems: "center",
              gap: "6px",
              fontFamily: "var(--font-ui)",
              fontWeight: "var(--w-heavy)",
              fontSize: "13px",
              letterSpacing: "var(--track-label)",
              textTransform: "uppercase",
              color: active ? "var(--keep-ink)" : "var(--text-muted)",
              background: active ? "var(--keep-grad)" : "transparent",
              border: active ? `2px solid var(--border-hard)` : "2px solid transparent",
              borderRadius: "var(--r-pill)",
              cursor: "pointer",
              whiteSpace: "nowrap",
              WebkitTapHighlightColor: "transparent",
              transition:
                "background var(--dur-snap) var(--ease-out), color var(--dur-snap), transform var(--dur-snap) var(--ease-spring)",
              transform: active ? "translateY(0) scale(1)" : "scale(0.98)",
            }}
          >
            {label}
          </button>
        );
      })}
    </div>
  );
}
