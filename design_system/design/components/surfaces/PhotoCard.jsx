import React from "react";

/**
 * Keepr PhotoCard — the full-bleed deck card. The photo owns the
 * surface; chrome floats over it. As it tilts toward keep/gone a
 * color wash bleeds in and a stamp lands.
 */
export function PhotoCard({
  image,
  imageAlt = "",
  date,
  meta,
  swipe = null,          // "keep" | "gone" | null
  progress = 0,          // 0..1 intensity of the wash/stamp
  tilt = 0,              // degrees
  children,
  style,
  ...rest
}) {
  const washColor =
    swipe === "keep" ? "var(--keep-wash)" : swipe === "gone" ? "var(--gone-wash)" : "transparent";
  const stampColor = swipe === "keep" ? "var(--keep)" : "var(--gone)";
  const stampInk = swipe === "keep" ? "var(--keep-ink)" : "var(--gone-ink)";
  const p = Math.max(0, Math.min(1, progress));

  return (
    <div
      style={{
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
        ...style,
      }}
      {...rest}
    >
      {/* photo */}
      {image ? (
        <img
          src={image}
          alt={imageAlt}
          style={{ width: "100%", height: "100%", objectFit: "cover", display: "block" }}
        />
      ) : (
        <div style={{ width: "100%", height: "100%", background: "var(--surface-card-2)" }} />
      )}

      {/* top protection gradient for floating chrome */}
      <div
        style={{
          position: "absolute", inset: "0 0 auto 0", height: "34%",
          background: "linear-gradient(180deg, rgba(0,0,0,0.55), transparent)",
          pointerEvents: "none",
        }}
      />
      {/* bottom protection gradient */}
      <div
        style={{
          position: "absolute", inset: "auto 0 0 0", height: "36%",
          background: "linear-gradient(0deg, rgba(0,0,0,0.62), transparent)",
          pointerEvents: "none",
        }}
      />

      {/* color wash on tilt */}
      <div
        style={{
          position: "absolute", inset: 0, background: washColor, opacity: p,
          transition: "opacity var(--dur-snap) var(--ease-out)", pointerEvents: "none",
        }}
      />

      {/* floating meta chrome */}
      {(date || meta) && (
        <div style={{ position: "absolute", left: 16, top: 16, right: 16, display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: 8 }}>
          {date && (
            <span style={{
              fontFamily: "var(--font-ui)", fontWeight: "var(--w-heavy)", fontSize: 13,
              letterSpacing: "var(--track-label)", textTransform: "uppercase", color: "#fff",
              background: "rgba(20,17,15,0.5)", backdropFilter: "blur(8px)",
              padding: "7px 12px", borderRadius: "var(--r-pill)", border: "2px solid rgba(255,255,255,0.14)",
            }}>{date}</span>
          )}
          {meta && (
            <span style={{
              fontFamily: "var(--font-num)", fontWeight: "var(--w-black)", fontSize: 13,
              color: "#fff", background: "rgba(20,17,15,0.5)", backdropFilter: "blur(8px)",
              padding: "7px 12px", borderRadius: "var(--r-pill)", border: "2px solid rgba(255,255,255,0.14)",
            }}>{meta}</span>
          )}
        </div>
      )}

      {/* landing stamp */}
      {swipe && (
        <div
          style={{
            position: "absolute", top: 26,
            [swipe === "keep" ? "left" : "right"]: 22,
            transform: `rotate(${swipe === "keep" ? -14 : 14}deg) scale(${0.6 + p * 0.4})`,
            opacity: p,
            fontFamily: "var(--font-display)", fontWeight: "var(--w-black)", fontSize: 44,
            letterSpacing: "var(--track-tight)", textTransform: "uppercase",
            color: stampInk, background: stampColor,
            padding: "6px 20px", borderRadius: "var(--r-sticker)",
            border: `var(--bw-thick) solid var(--border-hard)`, boxShadow: "var(--shadow-hard)",
            transition: "opacity var(--dur-snap), transform var(--dur-snap) var(--ease-boing)",
            pointerEvents: "none",
          }}
        >
          {swipe === "keep" ? "KEEP" : "GONE"}
        </div>
      )}

      {children}
    </div>
  );
}
