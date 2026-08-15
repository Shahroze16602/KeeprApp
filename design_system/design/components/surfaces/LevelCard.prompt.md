Month bucket rendered as a game level — the home screen's core row. Progress ring, sorted count, reclaim sticker, lock/done states.

```jsx
<LevelCard month="March" year="2024" total={412} done={296}
  reclaim="2.4 GB" state="active" level={7} />
```

`state`: `active` (keep ring) or `done` (green ring + ✓). Every month remains interactive.
