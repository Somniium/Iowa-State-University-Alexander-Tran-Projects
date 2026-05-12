// frontend/src/pages/Gallery.jsx
import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { api } from "../api/client";
import GameCard from "../components/GameCard";
import GameModal from "../components/GameModal";

function shuffleArray(arr) {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

function useQuery() {
  const { search } = useLocation();
  return useMemo(() => new URLSearchParams(search), [search]);
}

export default function Gallery() {
  const nav = useNavigate();

  const [games, setGames] = useState([]);
  const [active, setActive] = useState(null);
  const [err, setErr] = useState("");
  const [page, setPage] = useState(1);

  // shuffle/filter UI
  const [genrePick, setGenrePick] = useState("all");
  const [shuffleSeed, setShuffleSeed] = useState(0);

  // URL search param (source of truth)
  const q = (useQuery().get("search") || "").replace(/\+/g, " ");

  // Search bar UI state
  const [searchText, setSearchText] = useState(q);
  const [suggestions, setSuggestions] = useState([]);
  const [showSug, setShowSug] = useState(false);

  // keep input synced if user navigates/back/forward
  useEffect(() => {
    setSearchText(q);
  }, [q]);

  // reset page when query changes
  useEffect(() => {
    setPage(1);
  }, [q]);

  // Pull genre strings out of RAWG shapes
  const getGenreStrings = (g) => {
    const raw =
      Array.isArray(g.genres) ? g.genres :
        typeof g.genre === "string" ? [g.genre] :
          Array.isArray(g.tags) ? g.tags :
            [];

    return raw
      .map((x) => (typeof x === "string" ? x : x?.name))
      .filter(Boolean)
      .map((s) => String(s).toLowerCase());
  };

  const genres = useMemo(() => {
    const set = new Set();
    (games || []).forEach((g) => {
      getGenreStrings(g).forEach((name) => set.add(name));
    });
    return ["all", ...Array.from(set).sort()];
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [games]);

  const shownGames = useMemo(() => {
    const base = games || [];
    const filtered =
      genrePick === "all"
        ? base
        : base.filter((g) => getGenreStrings(g).includes(genrePick));

    return shuffleSeed > 0 ? shuffleArray(filtered) : filtered;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [games, genrePick, shuffleSeed]);

  useEffect(() => {
    const term = String(searchText || "")
      .replace(/\s+/g, " ")
      .trimStart();

    // don’t spam suggestions for tiny input
    if (term.length < 2) {
      setSuggestions([]);
      return;
    }

    const ac = new AbortController();
    const t = setTimeout(async () => {
      try {
        // quick lightweight fetch for dropdown
        const qs = new URLSearchParams({
          search: term,     // or effective
          page: "1",
          page_size: "30",
        });
        const data = await api(`/rawg/games?${qs.toString()}`, {
          signal: ac.signal,
        });

        const list = Array.isArray(data?.games) ? data.games : [];
        const phrase = term.trim().toLowerCase();
        const words = phrase.split(/\s+/).filter(Boolean);

        const ranked = list
          .map((g) => {
            const title = String(g.title || "").toLowerCase();

            const phraseMatch = phrase && title.includes(phrase);
            const allWordsMatch =
              words.length > 1 && words.every((w) => title.includes(w));

            // penalize "drift" toward only the last word
            const lastWord = words[words.length - 1];
            const lastOnly =
              lastWord &&
              title.includes(lastWord) &&
              !phraseMatch &&
              !allWordsMatch;

            let score = 0;
            if (phraseMatch) score += 1000;        // absolute priority
            if (allWordsMatch) score += 500;       // strong
            if (lastOnly) score -= 200;            // DEMOTE drift

            return { g, score, title };
          })
          .sort((a, b) => b.score - a.score || a.title.localeCompare(b.title))
          .map((x) => x.g)
          .slice(0, 8);

        setSuggestions(ranked);

      } catch {
        // ignore suggestion errors
        setSuggestions([]);
      }
    }, 250); // small debounce

    return () => {
      clearTimeout(t);
      ac.abort();
    };
  }, [searchText]);

  function goSearch(next) {
    const raw = next ?? searchText ?? "";

    const term = String(raw)
      .replace(/\s+/g, " ")
      .trim();

    // keep input in sync with what we actually searched
    setSearchText(term);

    if (term) {
      nav(`/gallery?search=${encodeURIComponent(term)}`);
    } else {
      nav(`/gallery`);
    }

    setGenrePick("all");
    setShuffleSeed(0);
    setShowSug(false);
  }


  async function openRawgGame(g) {
    try {
      const full = await api(`/rawg/games/${g.rawgId}`);
      setActive({
        ...full,
        id: `rawg:${full.rawgId}`,
      });
    } catch {
      setActive({
        ...g,
        id: `rawg:${g.rawgId}`,
      });
    }
  }

  // fetch RAWG games using URL query
  useEffect(() => {
    (async () => {
      try {
        const qs = new URLSearchParams({
          search: q,
          page: String(page),
          page_size: "300",
        });

        const data = await api(`/rawg/games?${qs.toString()}`);
        setGames(data.games || []);
        setErr("");

        setShuffleSeed(0);
      } catch (ex) {
        setErr(ex.message || "Failed to load games");
        setGames([]);
      }
    })();
  }, [q, page]);

  return (
    <div>
      <h1
        className="hc-hero-title"
        style={{ fontSize: 40, marginBottom: 5, marginTop: 20 }}
      >
        Game Gallery
      </h1>

      <h2 className="hc-hero-sub" style={{ marginBottom: 5 }}>
        Discover games powered by RAWG.
      </h2>

      <div className="hc-rule" />
      {err && <p style={{ color: "crimson" }}>{err}</p>}

      <div style={{ position: "relative", margin: "14px 0" }}>
        <div className="hc-row" style={{ gap: 10 }}>
          <span className="hc-pill">Search</span>

          <input
            className="admin-select"
            style={{ width: "min(520px, 100%)" }}
            value={searchText}
            placeholder="Search RAWG…"
            onChange={(e) => {
              setSearchText(e.target.value);
              setShowSug(true);
            }}
            onFocus={() => setShowSug(true)}
            onKeyDown={(e) => {
              if (e.key === "Enter") goSearch();
              if (e.key === "Escape") setShowSug(false);
            }}
          />

          <button
            className="hc-pill-btn hc-post"
            type="button"
            onClick={() => goSearch()}
          >
            Go
          </button>

          {q && (
            <button
              className="hc-pill-btn hc-edit"
              type="button"
              onClick={() => {
                setSearchText("");
                goSearch("");
              }}
            >
              Clear
            </button>
          )}
        </div>

        {showSug && suggestions.length > 0 && (
          <div
            style={{
              position: "absolute",
              top: "calc(100% + 8px)",
              left: 0,
              width: "min(620px, 100%)",
              background: "#fff",
              border: "1px solid #e5e7eb",
              borderRadius: 14,
              boxShadow: "0 12px 30px rgba(17,19,24,.14)",
              zIndex: 50,
              overflow: "hidden",
            }}
          >
            {suggestions.map((s) => (
              <button
                key={s.rawgId}
                type="button"
                onClick={() => goSearch(s.title)}
                style={{
                  width: "100%",
                  textAlign: "left",
                  padding: "10px 12px",
                  border: "none",
                  background: "transparent",
                  cursor: "pointer",
                }}
                onMouseDown={(e) => e.preventDefault()} // prevents blur closing before click
              >
                <div style={{ fontWeight: 900 }}>{s.title}</div>
                <div style={{ opacity: 0.65, fontSize: 12 }}>
                  {s.releaseDate || "—"}
                </div>
              </button>
            ))}
          </div>
        )}
      </div>

      <div className="hc-row" style={{ gap: 10, margin: "14px 0" }}>
        <span className="hc-pill">Genre</span>

        <select
          className="admin-select"
          value={genrePick}
          onChange={(e) => {
            setGenrePick(e.target.value);
            setShuffleSeed((s) => s + 1);
          }}
        >
          {genres.map((g) => (
            <option key={g} value={g}>
              {g === "all" ? "All" : g}
            </option>
          ))}
        </select>

        <button
          className="hc-pill-btn hc-post"
          type="button"
          onClick={() => setShuffleSeed((s) => s + 1)}
        >
          Shuffle
        </button>
      </div>

      <div className="gallery-grid">
        {shownGames.map((g) => (
          <GameCard
            key={g.rawgId}
            game={{
              id: `rawg:${g.rawgId}`,
              rawgId: g.rawgId,
              title: g.title,
              releaseDate: g.releaseDate,
              rating: g.rating,
              cover: g.cover,
              platform: (g.platforms || []).join(" • "),
              publisher: g.publisher || "",
              summary: g.summary || "",
              rawgUrl: g.rawgUrl,
              website: g.website,
            }}
            onOpen={() => openRawgGame(g)}
          />
        ))}
      </div>

      <div style={{ display: "flex", gap: 12, marginTop: 20 }}>
        <button
          className="btn"
          disabled={page <= 1}
          onClick={() => setPage((p) => p - 1)}
        >
          Prev
        </button>
        <button className="btn" onClick={() => setPage((p) => p + 1)}>
          Next
        </button>
      </div>

      {active && <GameModal game={active} onClose={() => setActive(null)} />}

      <p style={{ marginTop: 24, opacity: 0.6 }}>
        Data provided by{" "}
        <a href="https://rawg.io" target="_blank" rel="noreferrer">
          RAWG
        </a>
      </p>
    </div>
  );
}
