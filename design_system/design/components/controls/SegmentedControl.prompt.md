Chunky pill toggle — filters, view switches, light/dark. The active segment is a solid gradient sticker inside an inset track.

```jsx
<SegmentedControl
  options={["All", "Screenshots", "Videos"]}
  value={tab}
  onChange={setTab}
/>
```

Options may be strings or `{ value, label }`. Sizes `sm` / `md`.
