import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api } from "../api/client";

export default function GameDetailEdit() {
  const { id } = useParams(); // RAWG ID
  const [game, setGame] = useState(null);
  const [err, setErr] = useState("");

  useEffect(() => {
    (async () => {
      try {
        const data = await api(`/rawg/games/${id}`);
        setGame(data);
      } catch (ex) {
        setErr(ex.message);
      }
    })();
  }, [id]);

  if (err) return <div className="shell">{err}</div>;
  if (!game) return <div className="shell">Loading…</div>;

  return (
    <div className="shell">
      <h1>{game.title}</h1>

      {game.cover && (
        <img
          src={game.cover}
          alt={game.title}
          style={{ width: "100%", borderRadius: 12, marginBottom: 16 }}
        />
      )}

      <p>{game.summary}</p>

      <p><b>Release:</b> {game.releaseDate || "—"}</p>
      <p><b>Platforms:</b> {(game.platforms || []).join(" • ")}</p>
      <p><b>Publisher:</b> {game.publisher || "—"}</p>
      <p><b>Metacritic:</b> {game.rating ?? "—"}</p>

      <p style={{ marginTop: 24, opacity: 0.6 }}>
        Data provided by <a href="https://rawg.io" target="_blank" rel="noreferrer">RAWG</a>
      </p>
    </div>
  );
}
