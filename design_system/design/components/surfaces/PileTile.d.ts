import * as React from "react";

/** Collectible-looking tile for smart piles (Screenshots / Blurry / Duplicates / Big Videos). */
export interface PileTileProps extends Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, "type"> {
  label: React.ReactNode;
  /** Count sticker value. */
  count: React.ReactNode;
  /** Sub-label (e.g. "1.4 GB"). */
  detail?: React.ReactNode;
  /** CSS background used when no thumb — a token gradient. */
  accent?: string;
  /** Optional thumbnail image URL. */
  thumb?: string;
  /** Icon glyph node. */
  icon?: React.ReactNode;
}

export function PileTile(props: PileTileProps): JSX.Element;
