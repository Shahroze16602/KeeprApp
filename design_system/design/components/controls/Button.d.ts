import * as React from "react";

/**
 * Chunky, pressable Keepr button with hard-offset depth.
 */
export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  /** Visual role. Default "keep". */
  variant?: "keep" | "gone" | "reward" | "neutral" | "ghost";
  /** Height/padding scale. Default "md". */
  size?: "sm" | "md" | "lg";
  /** Stretch to full container width. */
  full?: boolean;
  disabled?: boolean;
  /** Leading icon node. */
  icon?: React.ReactNode;
  /** Trailing icon node. */
  iconRight?: React.ReactNode;
  children?: React.ReactNode;
}

export function Button(props: ButtonProps): JSX.Element;
