import * as React from "react";

/** Round or squircle chunky icon tap target (deck actions, chrome). */
export interface IconButtonProps extends Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, "aria-label"> {
  /** Icon glyph node (e.g. a Phosphor <i> or SVG). */
  children?: React.ReactNode;
  variant?: "keep" | "gone" | "reward" | "neutral" | "glass";
  size?: "sm" | "md" | "lg" | "xl";
  shape?: "circle" | "squircle";
  disabled?: boolean;
  /** Accessible label — always provide one. */
  ariaLabel?: string;
}

export function IconButton(props: IconButtonProps): JSX.Element;
