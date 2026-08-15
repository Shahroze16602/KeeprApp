import * as React from "react";

/** Flame sticker + running streak count for the home header. */
export interface StreakBadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  days: number;
  /** Live streak — gold gradient + gentle bob. False = dimmed. */
  live?: boolean;
}

export function StreakBadge(props: StreakBadgeProps): JSX.Element;
