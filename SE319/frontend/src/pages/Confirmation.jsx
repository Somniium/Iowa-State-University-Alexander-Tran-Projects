import { useLocation, useNavigate } from "react-router-dom";

export default function Confirmation() {
  const nav = useNavigate();
  const loc = useLocation();

  const {
    message = "CONGRATS — YOU MADE A REVIEW",
    title = "",
    kind = "Developer Card",
    againPath = "/dashboard",
    seePath = "/",
  } = loc.state || {};
  const S = {
    page: {
      position: "fixed",
      inset: 0,
      width: "100%",
      height: "100vh",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      padding: "0 20px",
      background:
        "radial-gradient(1400px 600px at 15% -10%, rgba(183,0,255,.20), transparent 60%)," +
        "radial-gradient(1200px 600px at 85% 0%, rgba(255,102,196,.18), transparent 60%)," +
        "linear-gradient(180deg, #0a0a0f 0%, #0f0f18 50%, #0a0a0f 100%)",
      overflowX: "hidden",
      zIndex: 5,
    },

    card: {
      width: "min(720px, 92vw)",
      borderRadius: 22,
      padding: 22,
      background: "rgba(14,14,20,.78)",
      border: "1px solid rgba(255,255,255,.10)",
      boxShadow: "0 18px 48px rgba(0,0,0,.55)",
      color: "white",
    },
  };

  return (
    <div style={S.page}>
      <div style={S.card}>
        <div style={{ fontWeight: 900, letterSpacing: 0.3, fontSize: 14, opacity: 0.9 }}>
          Honest<span style={{ color: "#b700ff" }}>Critic</span>
        </div>

        <h1 style={{ margin: "10px 0 6px", fontSize: 34, lineHeight: 1.1, fontWeight: 950 }}>
          {message}
        </h1>

        <p style={{ margin: 0, opacity: 0.78, fontSize: 14, lineHeight: 1.5 }}>
          Clean submission. No fluff. Your {kind.toLowerCase()} is ready.
        </p>

        <div
          style={{
            marginTop: 14,
            padding: "12px 14px",
            borderRadius: 16,
            background: "rgba(255,255,255,.06)",
            border: "1px solid rgba(255,255,255,.10)",
            display: "flex",
            gap: 10,
            flexWrap: "wrap",
            alignItems: "center",
          }}
        >
          <span
            style={{
              display: "inline-flex",
              alignItems: "center",
              gap: 8,
              padding: "6px 10px",
              borderRadius: 999,
              fontWeight: 900,
              fontSize: 12,
              border: "1px solid rgba(183,0,255,.35)",
              background: "rgba(183,0,255,.18)",
            }}
          >
            ✓ Confirmed
          </span>

          {title ? (
            <span
              style={{
                display: "inline-flex",
                alignItems: "center",
                gap: 8,
                padding: "6px 10px",
                borderRadius: 999,
                fontWeight: 900,
                fontSize: 12,
                border: "1px solid rgba(183,0,255,.35)",
                background: "rgba(183,0,255,.18)",
              }}
            >
              🎮 {title}
            </span>
          ) : null}
        </div>

        <div style={{ display: "flex", gap: 12, marginTop: 16, flexWrap: "wrap" }}>
          <button
            type="button"
            onClick={() => nav(againPath)}
            style={{
              borderRadius: 14,
              padding: "12px 14px",
              fontWeight: 950,
              cursor: "pointer",
              color: "white",
              background: "transparent",
              border: "1px solid rgba(255,255,255,.18)",
            }}
          >
            Make another?
          </button>

          <button
            type="button"
            onClick={() => nav(seePath)}
            style={{
              border: "none",
              borderRadius: 14,
              padding: "12px 14px",
              fontWeight: 950,
              cursor: "pointer",
              color: "white",
              background:
                "linear-gradient(90deg, rgba(183,0,255,1) 0%, rgba(255,102,196,1) 100%)",
              boxShadow: "0 16px 32px rgba(183,0,255,.28)",
            }}
          >
            See review
          </button>
        </div>
      </div>
    </div>
  );
}
