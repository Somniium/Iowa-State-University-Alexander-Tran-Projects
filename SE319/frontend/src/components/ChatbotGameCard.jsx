export default function ChatbotGameCard({ game, onOpen }) {
  const rating = game.rating ?? "tbd";

  // Rating = color rules 
  function scoreClass(value) {
    const n = Number(value);
    if (!Number.isFinite(n)) return "b-gray"; // tbd / Unrated
    if (n >= 90) return "b-green";
    if (n >= 75) return "b-yellow";
    return "b-red";
  }

  return (
    <article
      className="game-card"
      onClick={onOpen}
      role="button"
      tabIndex={0}
      style={{ cursor: onOpen ? "pointer" : "default" }}
    >
      <a className="game-cover" href="#" onClick={(e) => e.preventDefault()}>
        <img src={game.cover} alt={game.title} />
      </a>

      <h3 className="game-title">{game.title}</h3>

      <div className="game-meta">
        <span className={`score-pill ${scoreClass(rating)}`}>
          <span>{Number.isFinite(Number(rating)) ? rating : "—"}</span>
        </span>
        {game.platform && <span className="sentiment">{game.platform}</span>}
      </div>

      {game.releaseDate && (
        <div className="game-date">
          <i className="bi bi-calendar-event"></i>
          <span>{game.releaseDate}</span>
        </div>
      )}

      {/* Show summary in chatbot context */}
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
}
