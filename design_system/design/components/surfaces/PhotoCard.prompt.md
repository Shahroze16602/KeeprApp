The full-bleed deck card — the app's core surface. Photo fills it; chrome floats over protection gradients; tilting toward keep/gone bleeds a color wash and lands a stamp.

```jsx
<PhotoCard image={url} date="MAR 2024" meta="4.2 MB"
  swipe="keep" progress={0.8} tilt={-6} />
```

Set `swipe` + `progress` together to drive the reaction. `tilt` rotates the whole card. Children render on top of everything (extra chrome).
