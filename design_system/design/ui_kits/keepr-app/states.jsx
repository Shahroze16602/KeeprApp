/* Keepr — permission, empty, resume, and deletion-recovery states */
const Ist = (n, s) => <i className={"ph-fill ph-" + n} style={s} />;

function StateShell({ tone = "keep", icon, kicker, title, body, children }) {
  const color = tone === "gone" ? "var(--gone)" : tone === "reward" ? "var(--reward)" : "var(--keep)";
  return (
    <div style={{ height: "100%", boxSizing: "border-box", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "28px 22px", textAlign: "center" }}>
      <div aria-hidden="true" style={{ width: 92, height: 92, display: "grid", placeItems: "center", borderRadius: "var(--r-2xl)", border: "var(--bw-slab) solid var(--border-hard)", background: "var(--surface-card)", color, fontSize: 43, boxShadow: "var(--shadow-hard-lg)" }}>{Ist(icon)}</div>
      <Kicker color={color} style={{ marginTop: 24 }}>{kicker}</Kicker>
      <h1 style={{ margin: "7px 0 0", maxWidth: 330, fontFamily: "var(--font-display)", fontWeight: 900, fontSize: 28, letterSpacing: "-.04em", lineHeight: 1.05, color: "var(--text-strong)" }}>{title}</h1>
      <p style={{ margin: "12px 0 24px", maxWidth: 310, fontFamily: "var(--font-ui)", fontWeight: 500, fontSize: 14.5, lineHeight: 1.5, color: "var(--text-muted)" }}>{body}</p>
      <div style={{ width: "100%", maxWidth: 310, display: "flex", flexDirection: "column", gap: 10 }}>{children}</div>
    </div>
  );
}

function PermissionDenied({ go }) {
  const { Button } = window.KeeprDesignSystem_30a628;
  return (
    <StateShell tone="gone" icon="lock-key" kicker="Photo access needed" title="Keepr can’t see your photos yet" body="Allow access in Android Settings, or choose only the photos you want to clean. Nothing is scanned until you decide.">
      <Button variant="keep" size="lg" full icon={Ist("gear-six")} onClick={() => go("access")}>Open access options</Button>
      <Button variant="neutral" full icon={Ist("images-square")} onClick={() => go("selected")}>Choose specific photos</Button>
      <Button variant="ghost" full onClick={() => go("empty")}>Continue without photos</Button>
    </StateShell>
  );
}

function EmptyLibrary({ go, params }) {
  const { Button } = window.KeeprDesignSystem_30a628;
  const month = params && params.month;
  return (
    <StateShell icon="image-square" kicker={month ? "Month is empty" : "Nothing to sort"} title={month ? `${month} has no photos` : "Your library is all clear"} body={month ? "There are no accessible photos in this month. Pick another month or refresh after changing photo access." : "Keepr couldn’t find any accessible photos. You can refresh or change which photos Android allows Keepr to see."}>
      <Button variant="keep" size="lg" full icon={Ist("arrow-clockwise")} onClick={() => go("main")}>Scan again</Button>
      <Button variant="neutral" full icon={Ist("gear-six")} onClick={() => go("settings")}>Open Settings</Button>
    </StateShell>
  );
}

function ResumeRecovery({ go }) {
  const { Button } = window.KeeprDesignSystem_30a628;
  return (
    <StateShell tone="reward" icon="clock-counter-clockwise" kicker="Progress restored" title="Continue where you left off" body="Keepr saved 128 decisions from March on this device. Your keep and delete choices are intact; no deletion has started.">
      <Button variant="keep" size="lg" full icon={Ist("play")} onClick={() => go("session", { month: { month: "March", year: "2024", total: 412, done: 128 } })}>Resume March</Button>
      <Button variant="ghost" full onClick={() => go("main")}>Back to months</Button>
    </StateShell>
  );
}

function DeletePartial({ go, params }) {
  const { Button } = window.KeeprDesignSystem_30a628;
  const result = (params && params.result) || { deleted: 7, unresolved: 3, month: { month: "March", year: "2024" } };
  return (
    <StateShell tone="gone" icon="warning-circle" kicker="Action needed" title={`${result.deleted} deleted · ${result.unresolved} unresolved`} body="Android confirmed some deletions, but a few photos could not be reconciled. Keepr will never count unresolved items as deleted.">
      <Button variant="gone" size="lg" full icon={Ist("list-magnifying-glass")} onClick={() => go("review", { month: result.month })}>Review unresolved photos</Button>
      <Button variant="neutral" full onClick={() => go("main")}>Back to months</Button>
    </StateShell>
  );
}

Object.assign(window, { PermissionDenied, EmptyLibrary, ResumeRecovery, DeletePartial });
