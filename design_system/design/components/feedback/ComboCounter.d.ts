import * as React from "react";

/** "×3 COMBO!" readout for fast consecutive swipes. Scales with the multiplier, pops on increment. */
export interface ComboCounterProps extends React.HTMLAttributes<HTMLDivElement> {
  /** Current combo multiplier. Renders nothing below 2. */
  combo: number;
}

export function ComboCounter(props: ComboCounterProps): JSX.Element | null;
