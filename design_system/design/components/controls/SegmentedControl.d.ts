import * as React from "react";

export type SegmentOption = string | { value: string; label: React.ReactNode };

/** Chunky pill toggle for filters / view switches. Active thumb is a solid sticker. */
export interface SegmentedControlProps {
  options: SegmentOption[];
  value: string;
  onChange?: (value: string) => void;
  size?: "sm" | "md";
  style?: React.CSSProperties;
}

export function SegmentedControl(props: SegmentedControlProps): JSX.Element;
