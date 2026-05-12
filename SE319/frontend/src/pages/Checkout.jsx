import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

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

export default function Checkout() {
  const nav = useNavigate();
  const loc = useLocation();

  const payload = useMemo(() => {
    const s = loc.state || {};
    return {
      message: s.message || "CONGRATS — YOU MADE A REVIEW",
      title: s.title || "",
      kind: s.kind || "Developer Card",
      againPath: s.againPath || "/dashboard",
      seePath: s.seePath || "/",
    };
  }, [loc.state]);

  const [step, setStep] = useState(0);

  useEffect(() => {
    // “transaction” simulation
    const timers = [];
    timers.push(setTimeout(() => setStep(1), 1200));
    timers.push(setTimeout(() => setStep(2), 1200));
    timers.push(
      setTimeout(() => {
        nav("/confirmation", { state: payload });
      }, 1700)
    );

    return () => timers.forEach(clearTimeout);
  }, [nav, payload]);

  const steps = [
    "Initializing checkout…",
    "Verifying submission…",
    "Finalizing receipt…",
  ];

  return (
    <div style={S.page}>
      <div style={S.card}>
        <div style={{ fontWeight: 900, letterSpacing: 0.3, fontSize: 14, opacity: 0.9 }}>
          Honest<span style={{ color: "#b700ff" }}>Critic</span>
        </div>

        <h2 style={{ margin: "10px 0 6px", fontSize: 28, lineHeight: 1.1, fontWeight: 950 }}>
          Processing…
        </h2>

        <p style={{ margin: 0, opacity: 0.78, fontSize: 14, lineHeight: 1.5 }}>
          This simulates a required workflow step. Don’t refresh — you’re almost done.
        </p>

        <div
          style={{
            marginTop: 14,
            padding: "14px 14px",
            borderRadius: 16,
            background: "rgba(255,255,255,.06)",
            border: "1px solid rgba(255,255,255,.10)",
            display: "flex",
            gap: 12,
            alignItems: "center",
          }}
        >
          <div
            aria-label="loading"
            style={{
              width: 18,
              height: 18,
              borderRadius: "50%",
              border: "2px solid rgba(255,255,255,.25)",
              borderTopColor: "white",
              animation: "hcSpin 900ms linear infinite",
            }}
          />
          <div style={{ fontWeight: 800 }}>{steps[Math.min(step, steps.length - 1)]}</div>
        </div>

        <div style={{ marginTop: 14, opacity: 0.8, fontSize: 13 }}>
          {payload.title ? (
            <div style={{ marginBottom: 6 }}>
              <span style={{ opacity: 0.7 }}>Item:</span> <strong>{payload.title}</strong>
            </div>
          ) : null}
          <div>
            <span style={{ opacity: 0.7 }}>Type:</span> <strong>{payload.kind}</strong>
          </div>
        </div>

        <style>{`
          @keyframes hcSpin { to { transform: rotate(360deg); } }
        `}</style>
      </div>
    </div>
  );
}
