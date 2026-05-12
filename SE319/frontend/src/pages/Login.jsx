import { useState } from "react";
import { useNavigate, Link, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function Login() {
  const { login } = useAuth();
  const nav = useNavigate();
  const [emailOrUsername, setEU] = useState("");
  const [password, setPW] = useState("");
  const [err, setErr] = useState("");
  const loc = useLocation();
  const from = loc.state?.from || "/";

  async function onSubmit(e) {
    e.preventDefault();
    try {
      await login(emailOrUsername, password);
      nav(from, { replace: true });
    } catch (err) {
      setErr(err?.message || "Login failed");
    }
  }

  const S = {
    page: {
      height: "100vh",
      width: "100%",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      padding: "0 20px",
      background:
        "radial-gradient(1400px 600px at 15% -10%, rgba(183,0,255,.20), transparent 60%)," +
        "radial-gradient(1200px 600px at 85% 0%, rgba(255,102,196,.18), transparent 60%)," +
        "linear-gradient(180deg, #0a0a0f 0%, #0f0f18 50%, #0a0a0f 100%)",
      overflowX: "hidden",
    },

    card: {
      width: "min(460px, 92vw)",
      borderRadius: 28,
      padding: 26,
      background:
        "linear-gradient(180deg, rgba(26,26,34,.95), rgba(10,10,15,.98))",
      border: "1px solid rgba(255,255,255,.14)",
      boxShadow:
        "0 30px 80px rgba(0,0,0,.65), inset 0 1px 0 rgba(255,255,255,.08)",
      color: "white",
    },


    brand: {
      textAlign: "center",
      fontWeight: 900,
      marginBottom: 8,
    },

    title: {
      margin: "0 0 4px",
      fontSize: 30,
      lineHeight: 1.05,
      fontWeight: 950,
      background:
        "linear-gradient(90deg, #b700ff 0%, #ff7a7a 45%, #ff66c4 85%)",
      WebkitBackgroundClip: "text",
      backgroundClip: "text",
      color: "transparent",
      textAlign: "center",
    },

    subtitle: {
      margin: "0 0 12px",
      textAlign: "center",
      color: "rgba(255,255,255,.7)",
      fontSize: 13,
    },
    form: { display: "grid", gap: 12, marginTop: 10 },
    input: {
      width: "100%",
      padding: "13px 14px",
      borderRadius: 14,
      border: "1px solid rgba(255,255,255,.16)",
      background: "rgba(17,19,24,.7)",
      color: "white",
      outline: "none",
    },

    btn: {
      width: "100%",
      padding: "13px 14px",
      borderRadius: 16,
      border: "0",
      fontWeight: 900,
      cursor: "pointer",
      background:
        "linear-gradient(90deg, rgba(183,0,255,1) 0%, rgba(255,102,196,1) 100%)",
      color: "white",
      boxShadow: "0 16px 32px rgba(183,0,255,.28)",
    },

    err: {
      margin: "10px 0 0",
      color: "#ff6b6b",
      background: "rgba(255, 107, 107, .10)",
      border: "1px solid rgba(255, 107, 107, .25)",
      padding: "10px 12px",
      borderRadius: 12,
      fontSize: 13,
    },
    footer: {
      marginTop: 14,
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      gap: 10,
      flexWrap: "wrap",
      fontSize: 13,
      color: "rgba(255,255,255,.72)",
    },
    link: { color: "#b700ff", textDecoration: "none", fontWeight: 800 },
    hint: { opacity: 0.8 },
  };

  return (
    <div style={S.page}>
      <div style={S.card}>
        <div style={S.brand}>
          Honest<span style={{ color: "#b700ff" }}>Critic</span>
        </div>

        <h2 style={S.title}>Login</h2>
        <p style={S.subtitle}>Welcome back. Let’s find your next obsession.</p>

        {err && <p style={S.err}>{err}</p>}

        <form onSubmit={onSubmit} style={S.form}>
          <input
            style={S.input}
            value={emailOrUsername}
            onChange={(e) => setEU(e.target.value)}
            placeholder="Email or username"
            autoComplete="username"
          />
          <input
            style={S.input}
            type="password"
            value={password}
            onChange={(e) => setPW(e.target.value)}
            placeholder="Password"
            autoComplete="current-password"
          />
          <button style={S.btn} type="submit">
            Login
          </button>
        </form>

        <div style={S.footer}>
          <span style={S.hint}>No account?</span>
          <Link to="/signup" style={S.link}>
            Create one →
          </Link>
        </div>
      </div>
    </div>
  );
}
