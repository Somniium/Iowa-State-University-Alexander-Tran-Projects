import { Link, useNavigate } from "react-router-dom";
import { useEffect, useMemo, useRef, useState } from "react";
import glasses from "../assets/images/glasseslogo.png";
import { useAuth } from "../auth/AuthContext";
import { api } from "../api/client";



const COVER_URLS = import.meta.glob("../assets/images/*", {
  eager: true,
  query: "?url",
  import: "default",
});
function resolveCover(src) {
  if (!src) return "";
  if (/^https?:\/\//i.test(src)) return src;
  const file = src.split("/").pop();
  return COVER_URLS[`../assets/images/${file}`] || src;
}

export default function Navbar() {
  const { user, token, logout } = useAuth();

  const [q, setQ] = useState("");
  const [games, setGames] = useState([]);
  const isAdmin = user?.role === "admin";
  const isReviewer = user?.role === "reviewer";
  const [searchOpen, setSearchOpen] = useState(false);
  const [userOpen, setUserOpen] = useState(false);

  const searchRef = useRef(null);
  const userRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    (async () => {
      try {
        const data = await api("/games");
        const list =
          Array.isArray(data) && data.length && Array.isArray(data[0].games)
            ? data[0].games
            : Array.isArray(data)
              ? data
              : [];
        setGames(list);
      } catch { }
    })();
  }, []);

  // Close dropdowns on outside click
  useEffect(() => {
    const onDoc = (e) => {
      if (searchRef.current && !searchRef.current.contains(e.target)) setSearchOpen(false);
      if (userRef.current && !userRef.current.contains(e.target)) setUserOpen(false);
    };
    document.addEventListener("mousedown", onDoc);
    return () => document.removeEventListener("mousedown", onDoc);
  }, []);

  const results = useMemo(() => {
    const s = q.trim().toLowerCase();
    if (!s) return [];
    return games
      .filter((g) => {
        const hay = `${g.title || ""} ${g.platform || ""} ${g.publisher || ""}`.toLowerCase();
        return hay.includes(s);
      })
      .slice(0, 6);
  }, [q, games]);

  function submit(e) {
    e.preventDefault();
    if (!q.trim()) return;
    navigate(`/gallery?search=${encodeURIComponent(q)}`);
    setSearchOpen(false);
    setQ("");
  }

  function pickGame(g) {
    navigate(`/gallery?search=${encodeURIComponent(g.title || "")}`);
    setSearchOpen(false);
    setQ("");
  }

  function go(path) {
    navigate(path);
    setUserOpen(false);
  }

  return (
    <header className="nav">
      <Link to="/" className="brand">
        <img src={glasses} alt="HonestCritic logo" className="brand-logo" />
        <span>
          Honest<span className="accent">Critic</span>
        </span>
      </Link>

      <nav className="links">
        <Link to="/">Home</Link>
        <Link to="/reviews">Developer Reviews</Link>
        <Link to="/gallery">Discover</Link>
        <Link to="/upcoming">Upcoming Games</Link>
        <Link to="/about">About</Link>
      </nav>

      {/* SEARCH */}
      <form
        className="nav-search search-wrap"
        onSubmit={submit}
        ref={searchRef}
        autoComplete="off"
      >
        <input
          value={q}
          onChange={(e) => {
            setQ(e.target.value);
            setSearchOpen(true);
          }}
          onFocus={() => setSearchOpen(true)}
          placeholder="Search games..."
          aria-label="Search games"
        />

        {q && (
          <button
            type="button"
            aria-label="Clear search"
            onClick={() => {
              setQ("");
              setSearchOpen(false);
            }}
            style={{ fontSize: 18, lineHeight: 1 }}
          >
            ×
          </button>
        )}

        <button type="submit" aria-label="Search">
          🔍
        </button>

        {searchOpen && results.length > 0 && (
          <div className="search-results" role="listbox">
            {results.map((g) => (
              <div
                key={g._id || g.id}
                className="item"
                role="option"
                tabIndex={0}
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => pickGame(g)}
                onKeyDown={(e) => e.key === "Enter" && pickGame(g)}
              >
                <img
                  src={resolveCover(g.cover)}
                  alt=""
                  onError={(e) => {
                    e.currentTarget.style.display = "none";
                  }}
                />
                <div>
                  <div style={{ fontWeight: 800 }}>{g.title}</div>
                  <div style={{ opacity: 0.75, fontSize: 12 }}>
                    {g.platform || "—"} • {g.releaseDate || "—"}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </form>

      {/* ACTIONS */}
      <div className="actions">
        {token ? (
          <div className="user-menu" ref={userRef}>
            <button
              className="avatar-btn"
              onClick={() => setUserOpen((o) => !o)}
              aria-label="User menu"
              type="button"
            >
              <span className="navMenuBtn" aria-label="Menu">☰</span>
            </button>

            {userOpen && (
              <div className="user-dropdown">
                <div className="user-info">
                  <strong>@{user?.username}</strong>
                </div>
                {isAdmin && (<button type="button" onClick={() => go("/admin")}>Admin Panel</button>)}
                {(isAdmin || isReviewer) && (<button type="button" onClick={() => go("/dashboard")}>Dashboard</button>)}
                <button type="button" onClick={() => go("/settings")}>Settings</button>

                <button
                  className="logout"
                  type="button"
                  onClick={() => {
                    logout();
                    go("/");
                  }}
                >
                  Logout
                </button>
              </div>
            )}
          </div>
        ) : (
          <>
            <Link className="btn ghost" to="/login">Login</Link>
            <Link className="btn primary" to="/signup">Register</Link>
          </>
        )}
      </div>
    </header>
  );
}
