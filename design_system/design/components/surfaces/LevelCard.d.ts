import * as React from "react";

/**
 * Month "bucket" rendered as a game level — ring, count, reclaim size, active/done state.
 */
export interface LevelCardProps extends Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, "type"> {
  month: React.ReactNode;
  year?: React.ReactNode;
  /** Total photos in the bucket. */
  total?: number;
  /** Photos already sorted. */
  done?: number;
  /** Reclaim sticker text (e.g. "2.4 GB"). */
  reclaim?: React.ReactNode;
  state?: "active" | "done";
  /** Optional level number badge. */
  level?: number;
}

export function LevelCard(props: LevelCardProps): JSX.Element;
