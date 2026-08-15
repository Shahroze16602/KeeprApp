import * as React from "react";

/**
 * Full-bleed swipe-deck card. Photo owns the surface; chrome floats
 * over protection gradients; tilt bleeds in a wash and lands a stamp.
 */
export interface PhotoCardProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Photo URL. */
  image?: string;
  /** Accessible description for meaningful photos; use an empty string for decorative stacked cards. */
  imageAlt?: string;
  /** Floating date pill (e.g. "MAR 2024"). */
  date?: React.ReactNode;
  /** Floating meta pill (e.g. "4.2 MB"). */
  meta?: React.ReactNode;
  /** Active swipe direction — drives wash + stamp. */
  swipe?: "keep" | "gone" | null;
  /** 0..1 intensity of the wash and stamp. */
  progress?: number;
  /** Card tilt in degrees. */
  tilt?: number;
  children?: React.ReactNode;
}

export function PhotoCard(props: PhotoCardProps): JSX.Element;
