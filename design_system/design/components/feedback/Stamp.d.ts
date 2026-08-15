import * as React from "react";

/** The KEEP / GONE / SUPER sticker that slams onto a card. Reusable in reviews & summaries. */
export interface StampProps extends React.HTMLAttributes<HTMLSpanElement> {
  kind?: "keep" | "gone" | "super";
  size?: "sm" | "md" | "lg";
  /** Play the slam-in animation on mount. */
  slam?: boolean;
}

export function Stamp(props: StampProps): JSX.Element;
