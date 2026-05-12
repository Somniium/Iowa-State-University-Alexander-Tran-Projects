import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function Signup() {
  const { signup } = useAuth();
  const nav = useNavigate();

  const [email, setEmail] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");

  async function onSubmit(e) {
    e.preventDefault();
    setErr("");
    try {
      await signup(email, username, password);
      nav("/login", { replace: true });
    } catch (ex) {
      setErr(ex.message);
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
        "radial-gradient(1400px 600px at 15% -10%, rgba(255, 0, 140, 0.2), transparent 60%)," +
        "radial-gradient(1200px 600px at 85% 0%, rgba(237, 102, 255, 0.18), transparent 60%)," +
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
      fontSize: 30,
      fontWeight: 950,
      textAlign: "center",
      background:
        "linear-gradient(90deg, #b700ff 0%, #ff7a7a 45%, #ff66c4 85%)",
      WebkitBackgroundClip: "text",
      color: "transparent",
      margin: "0 0 6px",
    },

    subtitle: {
      textAlign: "center",
      fontSize: 13,
      color: "rgba(255,255,255,.7)",
      marginBottom: 16,
    },

    form: {
      display: "grid",
      gap: 12,
    },

    input: {
      padding: "13px 14px",
      borderRadius: 14,
      border: "1px solid rgba(255,255,255,.14)",
      background: "rgba(18,18,24,.85)",   // dark when empty
      color: "#fff",
      fontWeight: 500,
      transition: "all .2s ease",
    },



    btn: {
      padding: "13px",
      borderRadius: 16,
      border: "none",
      fontWeight: 900,
      cursor: "pointer",
      background:
        "linear-gradient(90deg, #b700ff, #ff66c4)",
      color: "white",
      boxShadow: "0 18px 40px rgba(183,0,255,.35)",
    },

    err: {
      marginBottom: 10,
      padding: "10px 12px",
      borderRadius: 12,
      fontSize: 13,
      color: "#ff6b6b",
      background: "rgba(255, 107, 107, .10)",
      border: "1px solid rgba(255, 107, 107, .25)",
    },

    footer: {
      marginTop: 14,
      display: "flex",
      justifyContent: "space-between",
      fontSize: 13,
      color: "rgba(255,255,255,.65)",
    },

    link: {
      color: "#b700ff",
      fontWeight: 800,
      textDecoration: "none",
    },
  };

  return (
    <div style={S.page}>
      <div style={S.card}>
        <div style={S.brand}>
          Honest<span style={{ color: "#b700ff" }}>Critic</span>
        </div>

        <h2 style={S.title}>Create account</h2>
        <p style={S.subtitle}>
          Join the community. Rate, review, repeat.
        </p>

        {err && <div style={S.err}>{err}</div>}

        <form onSubmit={onSubmit} style={S.form}>
          <input
            style={S.input}
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />

          <input
            style={S.input}
            placeholder="Username"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />

          <input
            style={S.input}
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <button style={S.btn}>Create account</button>
        </form>

        <div style={S.footer}>
          <span>Already have an account?</span>
          <Link to="/login" style={S.link}>
            Log in →
          </Link>
        </div>
      </div>
    </div>
  );
}
