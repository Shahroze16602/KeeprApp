import * as React from "react";

/** Enormous numeral that counts up (never fades). GB reclaimed, counts, XP, streaks. */
export interface StatNumberProps extends React.HTMLAttributes<HTMLDivElement> {
  value: number;
  prefix?: string;
  /** e.g. " GB", "%". */
  suffix?: string;
  /** Small-caps label under the numeral. */
  label?: React.ReactNode;
  decimals?: number;
  /** CSS font-size (token). */
  size?: string;
  color?: string;
  /** Reward gold gradient text. */
  gradient?: boolean;
  duration?: number;
  /** Count-up on mount. Default true. */
  animate?: boolean;
}

export function StatNumber(props: StatNumberProps): JSX.Element;
