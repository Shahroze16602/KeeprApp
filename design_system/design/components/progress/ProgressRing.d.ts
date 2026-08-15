import * as React from "react";

/** Thick rounded-cap progress ring that fills with overshoot. Level/month completion meter. */
export interface ProgressRingProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Completion 0..1. */
  value?: number;
  size?: number;
  thickness?: number;
  /** Stroke color (ignored when gradient). */
  color?: string;
  track?: string;
  /** Use the reward gold gradient stroke. */
  gradient?: boolean;
  /** Show integer percent in the center. */
  showValue?: boolean;
  /** Small-caps label under the value. */
  label?: React.ReactNode;
  /** Custom center content (overrides showValue). */
  children?: React.ReactNode;
}

export function ProgressRing(props: ProgressRingProps): JSX.Element;
