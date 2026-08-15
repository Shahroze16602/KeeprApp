/* @ds-bundle: {"format":4,"namespace":"KeeprDesignSystem_30a628","components":[{"name":"Button","sourcePath":"components/controls/Button.jsx"},{"name":"IconButton","sourcePath":"components/controls/IconButton.jsx"},{"name":"SegmentedControl","sourcePath":"components/controls/SegmentedControl.jsx"},{"name":"Badge","sourcePath":"components/feedback/Badge.jsx"},{"name":"ComboCounter","sourcePath":"components/feedback/ComboCounter.jsx"},{"name":"Stamp","sourcePath":"components/feedback/Stamp.jsx"},{"name":"ProgressRing","sourcePath":"components/progress/ProgressRing.jsx"},{"name":"StatNumber","sourcePath":"components/progress/StatNumber.jsx"},{"name":"StreakBadge","sourcePath":"components/progress/StreakBadge.jsx"},{"name":"LevelCard","sourcePath":"components/surfaces/LevelCard.jsx"},{"name":"PhotoCard","sourcePath":"components/surfaces/PhotoCard.jsx"},{"name":"PileTile","sourcePath":"components/surfaces/PileTile.jsx"}],"sourceHashes":{"components/controls/Button.jsx":"7e3bfdb55408","components/controls/IconButton.jsx":"4b616103f438","components/controls/SegmentedControl.jsx":"5f03fccfea4e","components/feedback/Badge.jsx":"0e265c43ff22","components/feedback/ComboCounter.jsx":"61ad14f24ad1","components/feedback/Stamp.jsx":"9d2ffb9c2a0e","components/progress/ProgressRing.jsx":"c5dde0c84f3c","components/progress/StatNumber.jsx":"c1e71388c0df","components/progress/StreakBadge.jsx":"a4f35ef6ca4f","components/surfaces/LevelCard.jsx":"00a4853723bf","components/surfaces/PhotoCard.jsx":"3994742f6642","components/surfaces/PileTile.jsx":"a4d88cc5f836"},"inlinedExternals":[],"unexposedExports":[]} */

(() => {

const __ds_ns = (window.KeeprDesignSystem_30a628 = window.KeeprDesignSystem_30a628 || {});

const __ds_scope = {};

(__ds_ns.__errors = __ds_ns.__errors || []);

// components/controls/Button.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const {
  useState
} = React;
/**
 * Keepr Button — chunky, pressable, hard-offset depth.
 * It physically depresses into its shadow on press.
 */
function Button({
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
    keep: {
      bg: "var(--keep-grad)",
      fg: "var(--keep-ink)"
    },
    gone: {
      bg: "var(--gone-grad)",
      fg: "var(--gone-ink)"
    },
    reward: {
      bg: "var(--reward-grad)",
      fg: "#241800"
    },
    neutral: {
      bg: "var(--surface-card-2)",
      fg: "var(--text-strong)"
    },
    ghost: {
      bg: "transparent",
      fg: "var(--text-strong)"
    }
  }[variant] || {};
  const sizes = {
    sm: {
      h: "var(--control-h-sm)",
      px: "18px",
      fs: "14px"
    },
    md: {
      h: "var(--control-h)",
      px: "26px",
      fs: "16px"
    },
    lg: {
      h: "64px",
      px: "34px",
      fs: "19px"
    }
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
    boxShadow: disabled ? "none" : pressed ? "var(--shadow-pressed)" : "var(--shadow-hard)",
    transform: pressed && !disabled ? "translate(3px, 4px)" : "translate(0,0)",
    transition: "transform var(--dur-instant) var(--ease-spring), box-shadow var(--dur-instant) var(--ease-out), filter var(--dur-snap)",
    ...style
  };
  const down = () => !disabled && setPressed(true);
  const up = () => setPressed(false);
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    style: base,
    disabled: disabled,
    onClick: onClick,
    onPointerDown: down,
    onPointerUp: up,
    onPointerLeave: up,
    onPointerCancel: up
  }, rest), icon, children, iconRight);
}
Object.assign(__ds_scope, { Button });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/controls/Button.jsx", error: String((e && e.message) || e) }); }

// components/controls/IconButton.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const {
  useState
} = React;
/**
 * Keepr IconButton — round or squircle chunky tap target.
 * Used for the deck's big KEEP / GONE / UNDO / SUPER controls.
 */
function IconButton({
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
    keep: {
      bg: "var(--keep-grad)",
      fg: "var(--keep-ink)"
    },
    gone: {
      bg: "var(--gone-grad)",
      fg: "var(--gone-ink)"
    },
    reward: {
      bg: "var(--reward-grad)",
      fg: "#241800"
    },
    neutral: {
      bg: "var(--surface-card-2)",
      fg: "var(--text-strong)"
    },
    glass: {
      bg: "rgba(20,17,15,0.55)",
      fg: "#FFFFFF"
    }
  }[variant] || {};
  const dim = {
    sm: 48,
    md: 56,
    lg: 72,
    xl: 84
  }[size];
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
    transition: "transform var(--dur-instant) var(--ease-spring), box-shadow var(--dur-instant) var(--ease-out)",
    ...style
  };
  const down = () => !disabled && setPressed(true);
  const up = () => setPressed(false);
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    "aria-label": ariaLabel,
    style: base,
    disabled: disabled,
    onClick: onClick,
    onPointerDown: down,
    onPointerUp: up,
    onPointerLeave: up,
    onPointerCancel: up
  }, rest), children);
}
Object.assign(__ds_scope, { IconButton });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/controls/IconButton.jsx", error: String((e && e.message) || e) }); }

// components/controls/SegmentedControl.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * Keepr SegmentedControl — chunky pill toggle. Used for filters
 * (All / Screenshots / Videos), light/dark, or view switches.
 * The active thumb is a solid sticker that sits inside the track.
 */
function SegmentedControl({
  options = [],
  value,
  onChange,
  size = "md",
  style,
  ...rest
}) {
  const h = {
    sm: 40,
    md: 48
  }[size];
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
    ...style
  };
  return /*#__PURE__*/React.createElement("div", _extends({
    role: "tablist",
    style: track
  }, rest), options.map(opt => {
    const val = typeof opt === "string" ? opt : opt.value;
    const label = typeof opt === "string" ? opt : opt.label;
    const active = val === value;
    return /*#__PURE__*/React.createElement("button", {
      key: val,
      role: "tab",
      "aria-selected": active,
      onClick: () => onChange && onChange(val),
      style: {
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
        transition: "background var(--dur-snap) var(--ease-out), color var(--dur-snap), transform var(--dur-snap) var(--ease-spring)",
        transform: active ? "translateY(0) scale(1)" : "scale(0.98)"
      }
    }, label);
  }));
}
Object.assign(__ds_scope, { SegmentedControl });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/controls/SegmentedControl.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Badge.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * Keepr Badge — sticker label (counts, tags, NEW) and the large
 * circular milestone badge (100 / 1,000 / 10,000 photos).
 */
function Badge({
  children,
  tone = "neutral",
  variant = "pill",
  // "pill" | "milestone"
  icon,
  label,
  locked = false,
  style,
  ...rest
}) {
  const tones = {
    keep: {
      bg: "var(--keep-grad)",
      fg: "var(--keep-ink)"
    },
    gone: {
      bg: "var(--gone-grad)",
      fg: "var(--gone-ink)"
    },
    reward: {
      bg: "var(--reward-grad)",
      fg: "#241800"
    },
    win: {
      bg: "var(--win)",
      fg: "#04160C"
    },
    neutral: {
      bg: "var(--surface-card-2)",
      fg: "var(--text-strong)"
    },
    ink: {
      bg: "var(--border-hard)",
      fg: "#fff"
    }
  }[tone];
  if (variant === "milestone") {
    return /*#__PURE__*/React.createElement("div", _extends({
      style: {
        position: "relative",
        width: 116,
        height: 116,
        display: "grid",
        placeItems: "center",
        borderRadius: "var(--r-pill)",
        background: locked ? "var(--surface-card)" : tones.bg,
        border: "var(--bw-slab) solid var(--border-hard)",
        boxShadow: locked ? "var(--shadow-hard)" : "var(--shadow-hard-lg)",
        color: locked ? "var(--text-faint)" : tones.fg,
        filter: locked ? "grayscale(0.4)" : "none",
        ...style
      }
    }, rest), /*#__PURE__*/React.createElement("div", {
      style: {
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        lineHeight: 0.92
      }
    }, icon && /*#__PURE__*/React.createElement("span", {
      style: {
        fontSize: 26,
        marginBottom: 2
      }
    }, locked ? "🔒" : icon), /*#__PURE__*/React.createElement("span", {
      style: {
        fontFamily: "var(--font-num)",
        fontWeight: "var(--w-black)",
        fontSize: 26,
        letterSpacing: "var(--track-mega)"
      }
    }, children), label && /*#__PURE__*/React.createElement("span", {
      style: {
        fontFamily: "var(--font-ui)",
        fontWeight: "var(--w-heavy)",
        fontSize: 9,
        letterSpacing: "var(--track-label)",
        textTransform: "uppercase",
        marginTop: 3,
        opacity: 0.85
      }
    }, label)));
  }
  return /*#__PURE__*/React.createElement("span", _extends({
    style: {
      display: "inline-flex",
      alignItems: "center",
      gap: 6,
      fontFamily: "var(--font-ui)",
      fontWeight: "var(--w-heavy)",
      fontSize: 12,
      letterSpacing: "var(--track-label)",
      textTransform: "uppercase",
      color: tones.fg,
      background: tones.bg,
      padding: "5px 11px",
      borderRadius: "var(--r-pill)",
      border: "2px solid var(--border-hard)",
      boxShadow: "var(--shadow-hard-sm)",
      ...style
    }
  }, rest), icon, children);
}
Object.assign(__ds_scope, { Badge });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Badge.jsx", error: String((e && e.message) || e) }); }

// components/feedback/ComboCounter.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * Keepr ComboCounter — the "x3 COMBO!" readout for fast consecutive
 * swipes. Scales with the multiplier and pops on each increment.
 */
function ComboCounter({
  combo = 0,
  style,
  ...rest
}) {
  if (combo < 2) return null;
  const heat = Math.min(1, (combo - 2) / 8);
  return /*#__PURE__*/React.createElement("div", _extends({
    key: combo,
    style: {
      display: "inline-flex",
      flexDirection: "column",
      alignItems: "center",
      lineHeight: 0.9,
      animation: "k-pop-in var(--dur-base) var(--ease-boing) both",
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-num)",
      fontWeight: "var(--w-black)",
      fontSize: 40 + heat * 26,
      letterSpacing: "var(--track-mega)",
      color: "#241800",
      background: "var(--reward-grad)",
      WebkitBackgroundClip: "text",
      backgroundClip: "text",
      WebkitTextFillColor: "transparent",
      WebkitTextStroke: "2px var(--border-hard)",
      transform: `rotate(${-3 - heat * 3}deg)`
    }
  }, "\xD7", combo), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-display)",
      fontWeight: "var(--w-black)",
      fontSize: 14 + heat * 6,
      letterSpacing: "var(--track-label)",
      textTransform: "uppercase",
      color: "var(--reward)",
      transform: `rotate(${-3 - heat * 3}deg)`
    }
  }, "combo"));
}
Object.assign(__ds_scope, { ComboCounter });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/ComboCounter.jsx", error: String((e && e.message) || e) }); }

// components/feedback/Stamp.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * Keepr Stamp — the KEEP / GONE sticker that slams onto a card.
 * Standalone so it can be reused in reviews, toasts, and summaries.
 */
function Stamp({
  kind = "keep",
  size = "md",
  slam = false,
  style,
  ...rest
}) {
  const cfg = {
    keep: {
      text: "KEEP",
      bg: "var(--keep)",
      fg: "var(--keep-ink)",
      rot: -12
    },
    gone: {
      text: "GONE",
      bg: "var(--gone)",
      fg: "var(--gone-ink)",
      rot: 12
    },
    super: {
      text: "SUPER",
      bg: "var(--reward-grad)",
      fg: "#241800",
      rot: -8
    }
  }[kind];
  const fs = {
    sm: 22,
    md: 40,
    lg: 64
  }[size];
  return /*#__PURE__*/React.createElement("span", _extends({
    style: {
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      fontFamily: "var(--font-display)",
      fontWeight: "var(--w-black)",
      fontSize: fs,
      letterSpacing: "var(--track-tight)",
      textTransform: "uppercase",
      color: cfg.fg,
      background: cfg.bg,
      padding: `${fs * 0.14}px ${fs * 0.5}px`,
      borderRadius: "var(--r-sticker)",
      border: `var(--bw-thick) solid var(--border-hard)`,
      boxShadow: "var(--shadow-hard)",
      transform: `rotate(${cfg.rot}deg)`,
      animation: slam ? "k-slam var(--dur-settle) var(--ease-boing) both" : "none",
      ...style
    }
  }, rest), cfg.text);
}
Object.assign(__ds_scope, { Stamp });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/feedback/Stamp.jsx", error: String((e && e.message) || e) }); }

// components/progress/ProgressRing.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * Keepr ProgressRing — thick, rounded-cap ring that fills with a
 * satisfying overshoot. Optional center numeral. Gradient stroke on
 * reward surfaces. This is the level/month completion meter.
 */
function ProgressRing({
  value = 0,
  // 0..1
  size = 96,
  thickness = 12,
  color = "var(--keep)",
  track = "var(--surface-inset)",
  gradient = false,
  // use reward gradient stroke
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
  return /*#__PURE__*/React.createElement("div", _extends({
    style: {
      position: "relative",
      width: size,
      height: size,
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("svg", {
    width: size,
    height: size,
    style: {
      transform: "rotate(-90deg)",
      overflow: "visible"
    }
  }, gradient && /*#__PURE__*/React.createElement("defs", null, /*#__PURE__*/React.createElement("linearGradient", {
    id: gid,
    x1: "0",
    y1: "0",
    x2: "1",
    y2: "1"
  }, /*#__PURE__*/React.createElement("stop", {
    offset: "0%",
    stopColor: "#FFE24D"
  }), /*#__PURE__*/React.createElement("stop", {
    offset: "55%",
    stopColor: "#FFB800"
  }), /*#__PURE__*/React.createElement("stop", {
    offset: "100%",
    stopColor: "#FF7A00"
  }))), /*#__PURE__*/React.createElement("circle", {
    cx: size / 2,
    cy: size / 2,
    r: r,
    fill: "none",
    stroke: track,
    strokeWidth: thickness
  }), /*#__PURE__*/React.createElement("circle", {
    cx: size / 2,
    cy: size / 2,
    r: r,
    fill: "none",
    stroke: gradient ? `url(#${gid})` : color,
    strokeWidth: thickness,
    strokeLinecap: "round",
    strokeDasharray: c,
    strokeDashoffset: c * (1 - v),
    style: {
      transition: "stroke-dashoffset var(--dur-settle) var(--ease-spring)"
    }
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      inset: 0,
      display: "flex",
      flexDirection: "column",
      alignItems: "center",
      justifyContent: "center",
      gap: 2
    }
  }, children, showValue && !children && /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-num)",
      fontWeight: "var(--w-black)",
      fontSize: size * 0.26,
      color: "var(--text-strong)",
      letterSpacing: "var(--track-mega)"
    }
  }, Math.round(v * 100)), label && /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-ui)",
      fontWeight: "var(--w-heavy)",
      fontSize: 10,
      letterSpacing: "var(--track-label)",
      textTransform: "uppercase",
      color: "var(--text-muted)"
    }
  }, label)));
}
Object.assign(__ds_scope, { ProgressRing });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/progress/ProgressRing.jsx", error: String((e && e.message) || e) }); }

// components/progress/StatNumber.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const {
  useEffect,
  useRef,
  useState
} = React;
/**
 * Keepr StatNumber — enormous numeral that COUNTS UP (never fades).
 * The house style for GB reclaimed, photo counts, XP, streak totals.
 */
function StatNumber({
  value = 0,
  prefix = "",
  suffix = "",
  label,
  decimals = 0,
  size = "var(--t-mega)",
  color = "var(--text-strong)",
  gradient = false,
  // reward gold gradient text
  duration = 900,
  animate = true,
  style,
  ...rest
}) {
  const [display, setDisplay] = useState(animate ? 0 : value);
  const raf = useRef(0);
  useEffect(() => {
    const reducedMotion = typeof window !== "undefined" && (window.matchMedia("(prefers-reduced-motion: reduce)").matches || document.querySelector('[data-motion="reduced"]'));
    if (!animate || reducedMotion) {
      setDisplay(value);
      return;
    }
    const start = performance.now();
    const from = 0;
    const ease = t => 1 - Math.pow(1 - t, 3);
    const tick = now => {
      const t = Math.max(0, Math.min(1, (now - start) / duration));
      setDisplay(from + (value - from) * ease(t));
      if (t < 1) raf.current = requestAnimationFrame(tick);
    };
    raf.current = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf.current);
  }, [value, duration, animate]);
  const num = display.toLocaleString(undefined, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  });
  const gradStyle = gradient ? {
    background: "var(--reward-grad)",
    WebkitBackgroundClip: "text",
    backgroundClip: "text",
    WebkitTextFillColor: "transparent",
    color: "transparent"
  } : {
    color
  };
  return /*#__PURE__*/React.createElement("div", _extends({
    "aria-live": "polite",
    "aria-atomic": "true",
    style: {
      display: "inline-flex",
      flexDirection: "column",
      alignItems: "center",
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-num)",
      fontWeight: "var(--w-black)",
      fontSize: size,
      letterSpacing: "var(--track-mega)",
      lineHeight: "var(--lh-tight)",
      fontVariantNumeric: "tabular-nums lining-nums",
      ...gradStyle
    }
  }, prefix, num, suffix), label && /*#__PURE__*/React.createElement("span", {
    style: {
      marginTop: 4,
      fontFamily: "var(--font-ui)",
      fontWeight: "var(--w-heavy)",
      fontSize: "var(--t-label)",
      letterSpacing: "var(--track-label)",
      textTransform: "uppercase",
      color: "var(--text-muted)"
    }
  }, label));
}
Object.assign(__ds_scope, { StatNumber });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/progress/StatNumber.jsx", error: String((e && e.message) || e) }); }

// components/progress/StreakBadge.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * Keepr StreakBadge — flame sticker with the running streak count.
 * Lives in the home header. Bobs gently when the streak is live.
 */
function StreakBadge({
  days = 0,
  live = true,
  style,
  ...rest
}) {
  return /*#__PURE__*/React.createElement("div", _extends({
    style: {
      display: "inline-flex",
      alignItems: "center",
      gap: 8,
      padding: "8px 14px 8px 10px",
      background: live ? "var(--reward-grad)" : "var(--surface-card-2)",
      color: live ? "#241800" : "var(--text-muted)",
      border: "var(--bw) solid var(--border-hard)",
      borderRadius: "var(--r-pill)",
      boxShadow: "var(--shadow-hard-sm)",
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("span", {
    style: {
      fontSize: 22,
      lineHeight: 1,
      animation: live ? "k-bob 1.6s var(--ease-spring) infinite" : "none",
      filter: live ? "none" : "grayscale(1)"
    }
  }, "\uD83D\uDD25"), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-num)",
      fontWeight: "var(--w-black)",
      fontSize: 20,
      letterSpacing: "var(--track-mega)",
      lineHeight: 1
    }
  }, days), /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-ui)",
      fontWeight: "var(--w-heavy)",
      fontSize: 11,
      letterSpacing: "var(--track-label)",
      textTransform: "uppercase",
      opacity: 0.8
    }
  }, "day", days === 1 ? "" : "s"));
}
Object.assign(__ds_scope, { StreakBadge });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/progress/StreakBadge.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/LevelCard.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const {
  useState
} = React;
/**
 * Keepr LevelCard — a month "bucket" rendered as a game level.
 * Progress ring, count, size to reclaim, and active/done state.
 * Self-contained ring so it drops in anywhere.
 */
function LevelCard({
  month,
  year,
  total = 0,
  done = 0,
  reclaim,
  state = "active",
  // "active" | "done"
  level,
  onClick,
  style,
  ...rest
}) {
  const [pressed, setPressed] = useState(false);
  const value = total > 0 ? Math.min(1, done / total) : state === "done" ? 1 : 0;
  const size = 68,
    thickness = 9,
    r = (size - thickness) / 2,
    c = 2 * Math.PI * r;
  const ringColor = state === "done" ? "var(--win)" : "var(--keep)";
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    onClick: onClick,
    onPointerDown: () => setPressed(true),
    onPointerUp: () => setPressed(false),
    onPointerLeave: () => setPressed(false),
    onPointerCancel: () => setPressed(false),
    style: {
      display: "flex",
      alignItems: "center",
      gap: "var(--gap)",
      width: "100%",
      padding: "var(--pad-card)",
      textAlign: "left",
      background: state === "done" ? "var(--surface-card-2)" : "var(--surface-card)",
      color: "var(--text-strong)",
      border: "var(--bw-thick) solid var(--border-hard)",
      borderRadius: "var(--r-lg)",
      cursor: "pointer",
      WebkitTapHighlightColor: "transparent",
      boxShadow: pressed ? "var(--shadow-pressed)" : "var(--shadow-hard)",
      transform: pressed ? "translate(2px,3px)" : "translate(0,0)",
      transition: "transform var(--dur-snap) var(--ease-spring), box-shadow var(--dur-snap)",
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "relative",
      width: size,
      height: size,
      flex: "0 0 auto",
      display: "grid",
      placeItems: "center"
    }
  }, /*#__PURE__*/React.createElement("svg", {
    width: size,
    height: size,
    style: {
      transform: "rotate(-90deg)"
    }
  }, /*#__PURE__*/React.createElement("circle", {
    cx: size / 2,
    cy: size / 2,
    r: r,
    fill: "none",
    stroke: "var(--surface-inset)",
    strokeWidth: thickness
  }), /*#__PURE__*/React.createElement("circle", {
    cx: size / 2,
    cy: size / 2,
    r: r,
    fill: "none",
    stroke: ringColor,
    strokeWidth: thickness,
    strokeLinecap: "round",
    strokeDasharray: c,
    strokeDashoffset: c * (1 - value),
    style: {
      transition: "stroke-dashoffset var(--dur-settle) var(--ease-spring)"
    }
  })), /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      fontFamily: "var(--font-num)",
      fontWeight: "var(--w-black)",
      fontSize: 16,
      letterSpacing: "var(--track-mega)",
      color: "var(--text-strong)"
    }
  }, state === "done" ? "✓" : Math.round(value * 100))), /*#__PURE__*/React.createElement("div", {
    style: {
      flex: 1,
      minWidth: 0
    }
  }, /*#__PURE__*/React.createElement("div", {
    style: {
      display: "flex",
      alignItems: "baseline",
      gap: 8
    }
  }, /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-display)",
      fontWeight: "var(--w-black)",
      fontSize: 24,
      letterSpacing: "var(--track-tight)",
      lineHeight: 1
    }
  }, month), year && /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-num)",
      fontWeight: "var(--w-bold)",
      fontSize: 15,
      color: "var(--text-muted)"
    }
  }, year)), /*#__PURE__*/React.createElement("div", {
    style: {
      marginTop: 6,
      fontFamily: "var(--font-ui)",
      fontWeight: "var(--w-heavy)",
      fontSize: 12,
      letterSpacing: "var(--track-label)",
      textTransform: "uppercase",
      color: "var(--text-muted)"
    }
  }, state === "done" ? "Cleared" : `${done} / ${total} sorted`)), reclaim && /*#__PURE__*/React.createElement("span", {
    style: {
      flex: "0 0 auto",
      fontFamily: "var(--font-num)",
      fontWeight: "var(--w-black)",
      fontSize: 14,
      color: "#241800",
      background: "var(--reward-grad)",
      border: "2px solid var(--border-hard)",
      borderRadius: "var(--r-pill)",
      padding: "6px 12px",
      boxShadow: "var(--shadow-hard-sm)"
    }
  }, reclaim), level != null && /*#__PURE__*/React.createElement("span", {
    style: {
      flex: "0 0 auto",
      fontFamily: "var(--font-num)",
      fontWeight: "var(--w-black)",
      fontSize: 13,
      color: "var(--text-muted)"
    }
  }, "LV", level));
}
Object.assign(__ds_scope, { LevelCard });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/LevelCard.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/PhotoCard.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
/**
 * Keepr PhotoCard — the full-bleed deck card. The photo owns the
 * surface; chrome floats over it. As it tilts toward keep/gone a
 * color wash bleeds in and a stamp lands.
 */
function PhotoCard({
  image,
  imageAlt = "",
  date,
  meta,
  swipe = null,
  // "keep" | "gone" | null
  progress = 0,
  // 0..1 intensity of the wash/stamp
  tilt = 0,
  // degrees
  children,
  style,
  ...rest
}) {
  const washColor = swipe === "keep" ? "var(--keep-wash)" : swipe === "gone" ? "var(--gone-wash)" : "transparent";
  const stampColor = swipe === "keep" ? "var(--keep)" : "var(--gone)";
  const stampInk = swipe === "keep" ? "var(--keep-ink)" : "var(--gone-ink)";
  const p = Math.max(0, Math.min(1, progress));
  return /*#__PURE__*/React.createElement("div", _extends({
    style: {
      position: "relative",
      width: "100%",
      aspectRatio: "3 / 4",
      borderRadius: "var(--r-xl)",
      border: `var(--bw-slab) solid var(--border-card)`,
      overflow: "hidden",
      background: "var(--surface-photo)",
      boxShadow: "var(--shadow-hard-xl)",
      transform: `rotate(${tilt}deg)`,
      transition: "transform var(--dur-snap) var(--ease-snap-back)",
      ...style
    }
  }, rest), image ? /*#__PURE__*/React.createElement("img", {
    src: image,
    alt: imageAlt,
    style: {
      width: "100%",
      height: "100%",
      objectFit: "cover",
      display: "block"
    }
  }) : /*#__PURE__*/React.createElement("div", {
    style: {
      width: "100%",
      height: "100%",
      background: "var(--surface-card-2)"
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      inset: "0 0 auto 0",
      height: "34%",
      background: "linear-gradient(180deg, rgba(0,0,0,0.55), transparent)",
      pointerEvents: "none"
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      inset: "auto 0 0 0",
      height: "36%",
      background: "linear-gradient(0deg, rgba(0,0,0,0.62), transparent)",
      pointerEvents: "none"
    }
  }), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      inset: 0,
      background: washColor,
      opacity: p,
      transition: "opacity var(--dur-snap) var(--ease-out)",
      pointerEvents: "none"
    }
  }), (date || meta) && /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      left: 16,
      top: 16,
      right: 16,
      display: "flex",
      justifyContent: "space-between",
      alignItems: "flex-start",
      gap: 8
    }
  }, date && /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-ui)",
      fontWeight: "var(--w-heavy)",
      fontSize: 13,
      letterSpacing: "var(--track-label)",
      textTransform: "uppercase",
      color: "#fff",
      background: "rgba(20,17,15,0.5)",
      backdropFilter: "blur(8px)",
      padding: "7px 12px",
      borderRadius: "var(--r-pill)",
      border: "2px solid rgba(255,255,255,0.14)"
    }
  }, date), meta && /*#__PURE__*/React.createElement("span", {
    style: {
      fontFamily: "var(--font-num)",
      fontWeight: "var(--w-black)",
      fontSize: 13,
      color: "#fff",
      background: "rgba(20,17,15,0.5)",
      backdropFilter: "blur(8px)",
      padding: "7px 12px",
      borderRadius: "var(--r-pill)",
      border: "2px solid rgba(255,255,255,0.14)"
    }
  }, meta)), swipe && /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      top: 26,
      [swipe === "keep" ? "left" : "right"]: 22,
      transform: `rotate(${swipe === "keep" ? -14 : 14}deg) scale(${0.6 + p * 0.4})`,
      opacity: p,
      fontFamily: "var(--font-display)",
      fontWeight: "var(--w-black)",
      fontSize: 44,
      letterSpacing: "var(--track-tight)",
      textTransform: "uppercase",
      color: stampInk,
      background: stampColor,
      padding: "6px 20px",
      borderRadius: "var(--r-sticker)",
      border: `var(--bw-thick) solid var(--border-hard)`,
      boxShadow: "var(--shadow-hard)",
      transition: "opacity var(--dur-snap), transform var(--dur-snap) var(--ease-boing)",
      pointerEvents: "none"
    }
  }, swipe === "keep" ? "KEEP" : "GONE"), children);
}
Object.assign(__ds_scope, { PhotoCard });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/PhotoCard.jsx", error: String((e && e.message) || e) }); }

// components/surfaces/PileTile.jsx
try { (() => {
function _extends() { return _extends = Object.assign ? Object.assign.bind() : function (n) { for (var e = 1; e < arguments.length; e++) { var t = arguments[e]; for (var r in t) ({}).hasOwnProperty.call(t, r) && (n[r] = t[r]); } return n; }, _extends.apply(null, arguments); }
const {
  useState
} = React;
/**
 * Keepr PileTile — a collectible-looking tile for smart piles
 * (Screenshots / Blurry / Duplicates / Big Videos). Chunky sticker
 * with a stacked-card shadow so it reads as a "pack" to open.
 */
function PileTile({
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
  return /*#__PURE__*/React.createElement("button", _extends({
    type: "button",
    onClick: onClick,
    onPointerDown: () => setPressed(true),
    onPointerUp: () => setPressed(false),
    onPointerLeave: () => setPressed(false),
    style: {
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
      ...style
    }
  }, rest), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      inset: 0,
      background: thumb ? "var(--surface-photo)" : accent,
      opacity: thumb ? 1 : 0.9
    }
  }, thumb && /*#__PURE__*/React.createElement("img", {
    src: thumb,
    alt: "",
    style: {
      width: "100%",
      height: "100%",
      objectFit: "cover",
      filter: "saturate(1.05)"
    }
  })), /*#__PURE__*/React.createElement("div", {
    style: {
      position: "absolute",
      inset: 0,
      background: "linear-gradient(0deg, rgba(8,8,7,0.82) 8%, rgba(8,8,7,0.15) 60%, transparent)"
    }
  }), /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      top: 12,
      right: 12,
      fontFamily: "var(--font-num)",
      fontWeight: "var(--w-black)",
      fontSize: 15,
      color: "var(--text-on-accent)",
      background: "#fff",
      border: "2px solid var(--border-hard)",
      borderRadius: "var(--r-pill)",
      padding: "4px 10px",
      boxShadow: "var(--shadow-hard-sm)"
    }
  }, count), icon && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "absolute",
      top: 12,
      left: 12,
      fontSize: 24,
      lineHeight: 1,
      width: 44,
      height: 44,
      display: "inline-flex",
      alignItems: "center",
      justifyContent: "center",
      color: "#fff",
      background: "rgba(20,17,15,0.5)",
      backdropFilter: "blur(8px)",
      border: "2px solid rgba(255,255,255,0.16)",
      borderRadius: "var(--r-md)"
    }
  }, icon), /*#__PURE__*/React.createElement("span", {
    style: {
      position: "relative",
      fontFamily: "var(--font-display)",
      fontWeight: "var(--w-black)",
      fontSize: 20,
      letterSpacing: "var(--track-snug)",
      color: "#fff",
      lineHeight: 1
    }
  }, label), detail && /*#__PURE__*/React.createElement("span", {
    style: {
      position: "relative",
      fontFamily: "var(--font-ui)",
      fontWeight: "var(--w-heavy)",
      fontSize: 12,
      letterSpacing: "var(--track-label)",
      textTransform: "uppercase",
      color: "rgba(255,255,255,0.72)"
    }
  }, detail));
}
Object.assign(__ds_scope, { PileTile });
})(); } catch (e) { __ds_ns.__errors.push({ path: "components/surfaces/PileTile.jsx", error: String((e && e.message) || e) }); }


__ds_ns.Button = __ds_scope.Button;

__ds_ns.IconButton = __ds_scope.IconButton;

__ds_ns.SegmentedControl = __ds_scope.SegmentedControl;

__ds_ns.Badge = __ds_scope.Badge;

__ds_ns.ComboCounter = __ds_scope.ComboCounter;

__ds_ns.Stamp = __ds_scope.Stamp;

__ds_ns.ProgressRing = __ds_scope.ProgressRing;

__ds_ns.StatNumber = __ds_scope.StatNumber;

__ds_ns.StreakBadge = __ds_scope.StreakBadge;

__ds_ns.LevelCard = __ds_scope.LevelCard;

__ds_ns.PhotoCard = __ds_scope.PhotoCard;

__ds_ns.PileTile = __ds_scope.PileTile;

})();
