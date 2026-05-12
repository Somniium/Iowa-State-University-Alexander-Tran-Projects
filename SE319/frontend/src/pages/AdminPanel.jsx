import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { api } from "../api/client";

export default function AdminPanel() {
  const { user } = useAuth();

  const [users, setUsers] = useState([]);
  const [requests, setRequests] = useState([]);
  const [filterRole, setFilterRole] = useState("all");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  // ──  carousel state  ──
  const [carouselSearch, setCarouselSearch] = useState("");
  const [carouselOptions, setCarouselOptions] = useState([]);
  const [carouselPicks, setCarouselPicks] = useState([]);   // 3-5 games

  // confirm modal
  const [confirm, setConfirm] = useState({
    open: false,
    title: "",
    message: "",
    confirmText: "Confirm",
    danger: false,
    action: null,
  });

  function openConfirm({ title, message, confirmText = "Confirm", danger = false, action }) {
    setConfirm({ open: true, title, message, confirmText, danger, action });
  }
  function closeConfirm() {
    setConfirm((c) => ({ ...c, open: false, action: null }));
  }
  async function runConfirm() {
    if (!confirm.action) return;
    setLoading(true);
    setError("");
    try {
      await confirm.action();
      closeConfirm();
      await refresh();
    } catch (e) {
      setError(e?.message || "Action failed.");
      closeConfirm();
    } finally {
      setLoading(false);
    }
  }

  async function refresh() {
    setLoading(true);
    setError("");
    try {
      const [u, r] = await Promise.all([api("/users"), api("/reviewer-requests")]);
      setUsers(u || []);
      setRequests(r || []);
    } catch (e) {
      setError(e?.message || "Failed to load admin data.");
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    refresh();
  }, []);

  // ──  carousel effects  ──
  useEffect(() => {
    api("/admin-carousel")
      .then(setCarouselPicks)
      .catch(() => setCarouselPicks([]));
  }, []);

  useEffect(() => {
    if (!carouselSearch.trim()) return setCarouselOptions([]);
    const t = setTimeout(async () => {
      const data = await api(`/rawg/games?search=${encodeURIComponent(carouselSearch)}&page_size=6`);
      setCarouselOptions(data.games || []);
    }, 300);
    return () => clearTimeout(t);
  }, [carouselSearch]);

  // close carousel drop-down on outside click
  useEffect(() => {
    const close = (e) => {
      if (!e.target.closest(".admin-carousel-picker")) {
        setCarouselOptions([]);
      }
    };
    document.addEventListener("click", close);

    return () => document.removeEventListener("click", close);
  }, []);

  async function addCarouselGame(game) {
    if (carouselPicks.length >= 5) return alert("Max 5 games");
    const updated = [...carouselPicks, {
      rawgId: game.rawgId,
      title: game.title,
      cover: game.cover,
      releaseDate: game.releaseDate,
    }];

    const saved = await api("/admin-carousel", { method: "PUT", body: { games: updated } });
    setCarouselPicks(saved);
    setCarouselSearch("");
    setCarouselOptions([]);
  }
  async function removeCarouselGame(rawgId) {
    const updated = carouselPicks.filter((g) => g.rawgId !== rawgId);
    const saved = await api("/admin-carousel", { method: "PUT", body: { games: updated } });
    setCarouselPicks(saved);
  }

  // ──  data memo  ──
  const pendingRequests = useMemo(
    () => (requests || []).filter((r) => r.status === "pending"),
    [requests]
  );
  const filteredUsers = useMemo(() => {
    if (filterRole === "all") return users;
    return (users || []).filter((u) => u.role === filterRole);
  }, [users, filterRole]);
  const stats = useMemo(() => {
    const total = users.length;
    const admins = users.filter((u) => u.role === "admin").length;
    const reviewers = users.filter((u) => u.role === "reviewer").length;
    const pending = pendingRequests.length;
    return { total, admins, reviewers, pending };
  }, [users, pendingRequests.length]);

  // ──  role helpers  ──
  function confirmRoleChange(targetUser, nextRole) {
    const from = targetUser.role;
    const to = nextRole;
    if (from === to) return;
    openConfirm({
      title: "Confirm role change",
      message: `Change @${targetUser.username} from "${from}" to "${to}"?`,
      confirmText: "Yes, change role",
      danger: to === "admin" || from === "admin",
      action: async () => {
        await api(`/users/${targetUser._id}/role`, { method: "PUT", body: { role: to } });
      },
    });
  }
  function confirmApprove(req) {
    openConfirm({
      title: "Approve request",
      message: `Approve @${req.username} to become a reviewer?`,
      confirmText: "Approve",
      danger: false,
      action: async () => {
        await api(`/reviewer-requests/${req._id}/approve`, { method: "PUT", body: { decisionNote: "" } });
      },
    });
  }
  function confirmReject(req) {
    openConfirm({
      title: "Reject request",
      message: `Reject @${req.username}'s reviewer request?`,
      confirmText: "Reject",
      danger: true,
      action: async () => {
        await api(`/reviewer-requests/${req._id}/reject`, { method: "PUT", body: { decisionNote: "" } });
      },
    });
  }

  if (!user) return null;

  return (
    <div className="hc-page">
      <div className="hc-head">
        <h1 className="hc-title">Admin Dashboard</h1>
        <p className="hc-sub">Welcome back, @{user.username}.</p>
      </div>

      {error && <div className="hc-error">{error}</div>}

      {/* ---------- stats ---------- */}
      <div className="hc-cardsGrid">
        <div className="hc-cardMini">
          <div className="hc-cardMiniTop">
            <div>
              <div className="hc-cardMiniTitle">Users</div>
              <div className="hc-cardMiniMeta">Total accounts</div>
            </div>
            <span className="hc-pill">{stats.total}</span>
          </div>
        </div>
        <div className="hc-cardMini">
          <div className="hc-cardMiniTop">
            <div>
              <div className="hc-cardMiniTitle">Reviewers</div>
              <div className="hc-cardMiniMeta">Can post reviews</div>
            </div>
            <span className="hc-pill">{stats.reviewers}</span>
          </div>
        </div>
        <div className="hc-cardMini">
          <div className="hc-cardMiniTop">
            <div>
              <div className="hc-cardMiniTitle">Admins</div>
              <div className="hc-cardMiniMeta">Full access</div>
            </div>
            <span className="hc-pill">{stats.admins}</span>
          </div>
        </div>
        <div className="hc-cardMini">
          <div className="hc-cardMiniTop">
            <div>
              <div className="hc-cardMiniTitle">Pending</div>
              <div className="hc-cardMiniMeta">Reviewer requests</div>
            </div>
            <span className="hc-pill">{stats.pending}</span>
          </div>
        </div>
      </div>

      {/* ---------- top controls ---------- */}
      <div className="hc-row" style={{ marginTop: 12 }}>
        <button className="hc-pill-btn hc-post" onClick={refresh} disabled={loading}>
          {loading ? "Refreshing…" : "Refresh"}
        </button>
        <div className="hc-row" style={{ gap: 10 }}>
          <span className="hc-pill">Filter</span>
          <select
            className="admin-select"
            value={filterRole}
            onChange={(e) => setFilterRole(e.target.value)}
            disabled={loading}
          >
            <option value="all">All</option>
            <option value="admin">admin</option>
            <option value="reviewer">reviewer</option>
            <option value="user">user</option>
          </select>
        </div>
      </div>

      {/* ---------- home carousel manager ---------- */}
      <div className="hc-card hc-spaced admin-carousel-picker">
        <div className="hc-cardHead">
          <h2>Home Carousel</h2>
          <span className="hc-pill">{carouselPicks.length}/5</span>
        </div>

        <input
          className="admin-input"
          placeholder="Search RAWG to add…"
          value={carouselSearch}
          onChange={(e) => setCarouselSearch(e.target.value)}
        />

        {carouselOptions.length > 0 && (
          <div className="search-results admin-carousel-results">
            {carouselOptions.map((g) => (
              <button
                key={g.rawgId}
                type="button"
                className="item"
                onMouseDown={(e) => {
                  e.preventDefault();     // prevents focus/blur weirdness
                  e.stopPropagation();    // prevents the document mousedown closer from winning
                  addCarouselGame(g);
                }}
              >
                <img src={g.cover} alt="" />
                <div>
                  <div style={{ fontWeight: 800 }}>{g.title}</div>
                  <div style={{ opacity: 0.65, fontSize: 12 }}>
                    {g.releaseDate || "—"}
                  </div>
                </div>
              </button>
            ))}
          </div>
        )}

        <div style={{ marginTop: 12, display: "flex", flexWrap: "wrap", gap: 8 }}>
          {carouselPicks.map((g) => (
            <span key={g.rawgId} className="hc-pill">
              {g.title}
              <button
                onClick={() => removeCarouselGame(g.rawgId)}
                style={{ marginLeft: 6, border: "none", background: "none", color: "inherit" }}
                title="Remove"
              >
                ×
              </button>
            </span>
          ))}
        </div>
      </div>

      {/* ---------- two-column grid ---------- */}
      <div className="hc-grid" style={{ marginTop: 14 }}>
        {/* reviewer requests */}
        <section className="hc-card">
          <div className="hc-cardHead">
            <h2>Reviewer Requests</h2>
            <span className="hc-pill">Queue</span>
          </div>
          {pendingRequests.length === 0 ? (
            <div className="gmMuted">No pending requests.</div>
          ) : (
            <div className="hc-reviewPills">
              {pendingRequests.map((req) => (
                <div key={req._id} className="hc-reviewPill">
                  <div className="hc-reviewTop">
                    <div className="hc-reviewGame">@{req.username}</div>
                    <span className="hc-pill">pending</span>
                  </div>
                  <div className="hc-reviewText">
                    {req.message || <span className="gmMuted">No message provided.</span>}
                  </div>
                  <div className="hc-actions">
                    <button
                      className="hc-pill-btn hc-post"
                      onClick={() => confirmApprove(req)}
                      disabled={loading}
                    >
                      Approve
                    </button>
                    <button
                      className="hc-pill-btn hc-delete"
                      onClick={() => confirmReject(req)}
                      disabled={loading}
                    >
                      Reject
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* user list */}
        <section className="hc-card">
          <div className="hc-cardHead">
            <h2>User List</h2>
            <span className="hc-pill">Roles</span>
          </div>
          {filteredUsers.length === 0 ? (
            <div className="gmMuted">No users found.</div>
          ) : (
            <div className="hc-reviewPills">
              {filteredUsers.map((u) => (
                <div key={u._id} className="hc-reviewPill">
                  <div className="hc-reviewTop">
                    <div className="hc-reviewGame">@{u.username}</div>
                    <span className="hc-pill">{u.role}</span>
                  </div>
                  <div className="hc-reviewText gmMuted">{u.email}</div>
                  <div className="hc-actions">
                    <select
                      className="admin-select"
                      value={u.role}
                      disabled={loading}
                      onChange={(e) => confirmRoleChange(u, e.target.value)}
                    >
                      <option value="user">user</option>
                      <option value="reviewer">reviewer</option>
                      <option value="admin">admin</option>
                    </select>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>

      {/* ---------- confirm modal ---------- */}
      {confirm.open && (
        <div className="modalOverlay" onClick={closeConfirm}>
          <div className="hc-card" style={{ width: "min(520px, 96vw)" }} onClick={(e) => e.stopPropagation()}>
            <div className="hc-cardHead">
              <h2 style={{ margin: 0 }}>{confirm.title}</h2>
              <span className="hc-pill">{confirm.danger ? "Caution" : "Confirm"}</span>
            </div>
            <div className="gmMuted" style={{ marginBottom: 12 }}>{confirm.message}</div>
            <div className="hc-actions">
              <button className="hc-pill-btn hc-edit" onClick={closeConfirm} disabled={loading}>
                Cancel
              </button>
              <button
                className={`hc-pill-btn ${confirm.danger ? "hc-delete" : "hc-post"}`}
                onClick={runConfirm}
                disabled={loading}
              >
                {confirm.confirmText}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}