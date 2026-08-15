import * as React from "react";

/** Sticker label (counts, tags, NEW) and the large circular milestone badge. */
export interface BadgeProps extends React.HTMLAttributes<HTMLElement> {
  children?: React.ReactNode;
  tone?: "keep" | "gone" | "reward" | "win" | "neutral" | "ink";
  variant?: "pill" | "milestone";
  /** Leading glyph. */
  icon?: React.ReactNode;
  /** Small-caps caption (milestone only). */
  label?: React.ReactNode;
  /** Locked milestone — dimmed + lock glyph. */
  locked?: boolean;
}

export function Badge(props: BadgeProps): JSX.Element;
