// frontend/src/pages/Upcoming.jsx
import { useEffect, useMemo, useState } from "react";
import { api } from "../api/client";
import GameCard from "../components/GameCard";
import GameModal from "../components/GameModal";

function yyyyMMdd(d) {
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

export default function Upcoming() {
  const [games, setGames] = useState([]);
  const [active, setActive] = useState(null);
  const [err, setErr] = useState("");
  const [page, setPage] = useState(1);

  // upcoming window: today -> 1 year from today
  const dates = useMemo(() => {
    const start = new Date();
    const end = new Date();
    end.setFullYear(end.getFullYear() + 1);
    return `${yyyyMMdd(start)},${yyyyMMdd(end)}`;
  }, []);

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

  useEffect(() => {
    (async () => {
      try {
        setErr("");
        const qs = new URLSearchParams({
          dates,
          ordering: "released", // soonest first
          page: String(page),
          page_size: "24",
        });

        const data = await api(`/rawg/games?${qs.toString()}`);
        setGames(data.games || []);
      } catch (e) {
        setErr(e.message || "Failed to load upcoming games");
        setGames([]);
      }
    })();
  }, [dates, page]);

  return (
    <div>
      <h1
        className="hc-hero-title"
        style={{ fontSize: 40, marginBottom: 5, marginTop: 20 }}
      >
        Upcoming Games
      </h1>

      <h2 className="hc-hero-sub" style={{ marginBottom: 5 }}>
        Pulled live from RAWG upcoming releases.
      </h2>

      <div className="hc-rule" />
      {err && <p style={{ color: "crimson" }}>{err}</p>}

      <div className="gallery-grid">
        {games.map((g) => (
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
              genres: g.genres || [],
            }}
            onOpen={() => openRawgGame(g)}
          />
        ))}
      </div>

      <div style={{ display: "flex", gap: 12, marginTop: 20 }}>
        <button className="btn" disabled={page <= 1} onClick={() => setPage((p) => p - 1)}>
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
