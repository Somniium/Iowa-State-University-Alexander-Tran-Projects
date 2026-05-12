import { useState, useRef, useEffect } from "react";
import GameModal from "../components/GameModal";
import { api } from "../api/client";

export default function ChatbotPage() {
  const [input, setInput] = useState("");
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedGame, setSelectedGame] = useState(null);
  const messagesContainerRef = useRef(null);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    if (messagesContainerRef.current) {
      messagesContainerRef.current.scrollTop =
        messagesContainerRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSubmit = async () => {
    if (!input.trim() || loading) return;

    const userMsg = { sender: "user", text: input };
    setMessages((prev) => [...prev, userMsg]);
    setInput("");
    setLoading(true);

    try {
      const res = await fetch("/api/chatbot", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ prompt: input }),
      });

      if (!res.ok) {
        let errorData;
        try {
          errorData = await res.json();
        } catch {
          const errorText = await res.text();
          errorData = { error: `Request failed with status ${res.status}` };
        }

        throw new Error(
          errorData.error ||
            errorData.details?.message ||
            `API request failed with status ${res.status}`
        );
      }

      const data = await res.json();

      const botIntro = {
        sender: "bot",
        text: data.fallback ? data.message : "Here are 3 games you might like:",
      };

      const gameRecommendations = {
        type: "game-group",
        sender: "bot",
        games: (data.recommendations || [])
          .filter((game) => game.rawgId) // REQUIRED for RAWG links
          .map((game, idx) => ({
            id: game.rawgId, 
            rawgId: game.rawgId, 
            title: game.title,
            cover:
              game.cover || "https://via.placeholder.com/320x240?text=No+Image",
            rating: game.rating ?? "tbd",
            platform: game.genres ? game.genres.join(" • ") : "",
            releaseDate: game.releaseDate,
            summary: game.summary || game.description,
            buy: game.rawgSlug
              ? `https://rawg.io/games/${game.rawgSlug}`
              : game.rawgId
              ? `https://rawg.io/games/${game.rawgId}`
              : "#",
          })),
      };


      if (gameRecommendations.games.length > 0) {
        setMessages((prev) => [...prev, botIntro, gameRecommendations]);
      } else {
        setMessages((prev) => [
          ...prev,
          {
            sender: "bot",
            text: "I couldn't find any specific games for that.",
          },
        ]);
      }
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        {
          sender: "bot",
          text: `Error: ${err.message}`,
        },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleCardClick = async (game) => {
    if (!game?.rawgId) return;

    try {
      const fullGame = await api(`/rawg/games/${game.rawgId}`);
      setSelectedGame(fullGame);
    } catch (e) {
      console.error("Failed to load RAWG game details", e);
    }
  };


  function scoreClass(value) {
    const n = Number(value);
    if (!Number.isFinite(n)) return "b-gray";
    if (n >= 90) return "b-green";
    if (n >= 75) return "b-yellow";
    return "b-red";
  }

  return (
    <div className="chat-area">
      <div className="top-input-bar">
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
          placeholder="Speak to your robot overlord!"
          disabled={loading}
        />
        <button onClick={handleSubmit} disabled={loading}>
          {loading ? "..." : "Send"}
        </button>
      </div>

      <div className="messages-container" ref={messagesContainerRef}>
        {messages.map((m, i) => {
          if (m.type === "game-group") {
            return (
              <div key={i} className="message bot-card">
                {m.games.map((game, gameIndex) => {
                  const rating = game.rating ?? "tbd";

                  return (
                    <article key={gameIndex} className="game-card">
                      <a
                        className="game-cover"
                        href="#"
                        onClick={(e) => {
                          e.preventDefault();
                          handleCardClick(game);
                        }}
                        style={{ cursor: "pointer" }}
                      >
                        <img src={game.cover} alt={game.title} />
                      </a>

                      {game.releaseDate && (
                        <div className="game-date">
                          <span>📅</span>
                          <span>{game.releaseDate}</span>
                        </div>
                      )}

                      <h3 className="game-title">{game.title}</h3>

                      <div className="game-meta">
                        <span className={`score-pill ${scoreClass(rating)}`}>
                          <span>
                            {Number.isFinite(Number(rating)) ? rating : "—"}
                          </span>
                        </span>
                        {game.platform && (
                          <span className="sentiment">{game.platform}</span>
                        )}
                      </div>

                      {game.summary && (
                        <p
                          style={{
                            marginTop: "10px",
                            fontSize: "13px",
                            opacity: 0.85,
                            lineHeight: "1.4",
                          }}
                        >
                          {game.summary}
                        </p>
                      )}
                    </article>
                  );
                })}
              </div>
            );
          }

          return (
            <div
              key={i}
              className={`message ${m.sender}`}
              style={{ whiteSpace: "pre-wrap" }}
            >
              {m.text}
            </div>
          );
        })}
        <div ref={messagesEndRef} />
      </div>

      {selectedGame && (
        <GameModal game={selectedGame} onClose={() => setSelectedGame(null)} />
      )}
    </div>
  );
}
