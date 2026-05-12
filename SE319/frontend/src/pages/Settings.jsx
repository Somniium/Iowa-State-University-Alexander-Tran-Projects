import { useEffect, useState } from "react";
import { api } from "../api/client";

export default function Settings() {
  const [theme, setTheme] = useState("light");

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const [msg, setMsg] = useState("");
  const [err, setErr] = useState("");

  function applyTheme(t) {
    document.documentElement.dataset.theme = t;
    localStorage.setItem("theme", t);
  }

  useEffect(() => {
    (async () => {
      try {
        const me = await api("/users/me");
        setTheme(me.theme || "light");
        applyTheme(me.theme || "light");
      } catch (e) {
        setErr(e.message || "Failed to load settings");
      }
    })();
  }, []);

  async function saveSettings() {
    setMsg("");
    setErr("");
    try {
      const updated = await api("/users/me/settings", {
        method: "PUT",
        body: { theme }
      });

      // keep navbar/user dropdown consistent if you read from localStorage
      localStorage.setItem("user", JSON.stringify(updated));

      applyTheme(updated.theme || theme);
      setMsg("Settings saved.");
    } catch (e) {
      setErr(e.message || "Failed to save settings");
    }
  }

  async function changePassword() {
    setMsg("");
    setErr("");

    const cur = currentPassword.trim();
    const next = newPassword.trim();

    if (!cur || !next) {
      setErr("Please fill in both password fields.");
      return;
    }
    if (next.length < 6) {
      setErr("New password must be at least 6 characters.");
      return;
    }
    if (cur === next) {
      setErr("New password must be different from current password.");
      return;
    }

    try {
      await api("/users/me/password", {
        method: "PUT",
        body: { currentPassword: cur, newPassword: next },
      });

      setCurrentPassword("");
      setNewPassword("");
      setMsg("Password updated. Please log in again.");

      // IMPORTANT: force fresh auth
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    } catch (e) {
      setErr(e.message || "Failed to change password");
    }
  }


  return (
    <div className="hc-page">
      <header className="hc-head">
        <h1 className="hc-title">Settings</h1>
        <p className="hc-sub">Privacy, theme, and security.</p>
      </header>

      {err ? <div className="hc-error">{err}</div> : null}
      {msg ? <div className="hc-success">{msg}</div> : null}

      <div className="hc-grid">
        <section className="hc-card">
          <div className="hc-cardHead">
            <h2>Theme</h2>
            <span className="hc-pill">Appearance</span>
          </div>

          <div className="hc-row">
            <div>
              <div className="hc-rowTitle">Dark mode</div>
              <div className="hc-muted">Toggle light/dark.</div>
            </div>

            <label className="hc-switch">
              <input
                type="checkbox"
                checked={theme === "dark"}
                onChange={(e) => {
                  const t = e.target.checked ? "dark" : "light";
                  setTheme(t);
                  applyTheme(t);
                }}
              />
              <span className="hc-slider" />
            </label>
          </div>
        </section>
      </div>

      <section className="hc-card hc-spaced">
        <div className="hc-cardHead">
          <h2>Change password</h2>
          <span className="hc-pill">Security</span>
        </div>

        <div className="hc-two">
          <input
            type="password"
            placeholder="Current password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
          />
          <input
            type="password"
            placeholder="New password (min 6 chars)"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
          />
        </div>

        <button className="hc-pill-btn hc-edit" type="button" onClick={changePassword}>
          Update Password
        </button>
      </section>

      <div style={{ marginTop: 16 }}>
        <button className="hc-pill-btn hc-post" type="button" onClick={saveSettings}>
          Save Settings
        </button>
      </div>
    </div>
  );
}
