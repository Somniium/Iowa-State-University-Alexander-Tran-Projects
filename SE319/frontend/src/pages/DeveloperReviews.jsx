import { useEffect, useMemo, useState } from "react";
import { useLocation } from "react-router-dom";
import { api } from "../api/client";
import GameCard from "../components/GameCard";
import GameModal from "../components/GameModal";

function useQuery() {
  const { search } = useLocation();
  return useMemo(() => new URLSearchParams(search), [search]);
}

function shuffle(arr) {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

export default function DeveloperReviews() {
  const [cards, setCards] = useState([]);
  const [active, setActive] = useState(null);
  const [err, setErr] = useState("");

  // video carousel
  const [vidPicks, setVidPicks] = useState([]);
  const [vidIdx, setVidIdx] = useState(0);

  const q = useQuery().get("search") || "";

  useEffect(() => {
    (async () => {
      try {
        const list = await api("/developer-cards"); // <-- MONGO dev cards
        const devOnly = Array.isArray(list) ? list : [];

        setCards(devOnly);
        setErr("");

        const devWithVideos = devOnly.filter((c) => Array.isArray(c.videos) && c.videos.length > 0);
        const picks3 = shuffle(devWithVideos).slice(0, 3);

        setVidPicks(picks3);
        setVidIdx(0);
      } catch (e) {
        setErr(e.message || "Failed to load developer cards");
        setCards([]);
        setVidPicks([]);
        setVidIdx(0);
      }
    })();
  }, []);

  const shown = useMemo(() => {
    const s = q.trim().toLowerCase();
    if (!s) return cards;

    return cards.filter((c) => {
      const hay = `${c.title || ""} ${c.platform || ""} ${c.publisher || ""} ${c.ownerUsername || ""}`.toLowerCase();
      return hay.includes(s);
    });
  }, [cards, q]);

  const currentVid = vidPicks[vidIdx];

  function goVidPrev() {
    if (!vidPicks.length) return;
    setVidIdx((v) => (v - 1 + vidPicks.length) % vidPicks.length);
  }
  function goVidNext() {
    if (!vidPicks.length) return;
    setVidIdx((v) => (v + 1) % vidPicks.length);
  }

  return (
    <div className="shell">
      <section className="authors-hero">
        <h1 className="hc-hero-title">Developer Reviews</h1>
        <p className="hc-hero-sub">Official-style cards created by Admins & Reviewers.</p>
      </section>

      <div className="hc-rule" />

      {/* Random 3 video carousel (no autoplay) */}
      {currentVid && (
        <div className="hc-carousel-wrap" style={{ marginBottom: 18 }}>
          <div
            className="carouselCard videoCarousel"
            role="button"
            tabIndex={0}
            onClick={() => setActive(currentVid)}
          >
            <iframe
              key={currentVid?.videos?.[0] || vidIdx}
              className="carouselVideo"
              src={currentVid.videos[0]}
              title={currentVid.title}
              frameBorder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
              allowFullScreen
            />

            <button
              className="carArrow left"
              onClick={(e) => {
                e.stopPropagation();
                goVidPrev();
              }}
              aria-label="Previous video"
              type="button"
            >
              ‹
            </button>

            <button
              className="carArrow right"
              onClick={(e) => {
                e.stopPropagation();
                goVidNext();
              }}
              aria-label="Next video"
              type="button"
            >
              ›
            </button>

            <div className="carLabel">Developer Spotlight: {currentVid.title}</div>
          </div>

          <div className="carDots">
            {vidPicks.map((_, i) => (
              <button
                key={i}
                className={`dot ${i === vidIdx ? "on" : ""}`}
                onClick={() => setVidIdx(i)}
                aria-label={`Go to video ${i + 1}`}
                type="button"
              />
            ))}
          </div>
        </div>
      )}

      <div className="hc-rule" />
      {err && <p style={{ color: "crimson" }}>{err}</p>}

      {shown.length === 0 ? (
        <div className="card p-3" style={{ background: "#b1b1b1ff", borderRadius: 12 }}>
          No developer cards found yet.
        </div>
      ) : (
        <div className="gallery-grid">
          {shown.map((c) => {
            // IMPORTANT: shape it the way GameCard/GameModal expect
            const shaped = {
              ...c,
              id: c._id,
              category: "developer",
              // ensure video is always available to slideshow
              videos: Array.isArray(c.videos) && c.videos.length
                ? c.videos
                : (c.coverVideo ? [c.coverVideo] : []),
              // poster image (not slideshow)
              cover: c.cover || c.images?.[0] || "/images/placeholder.png",

              // slideshow images
              images: Array.isArray(c.images) ? c.images : [],

              buy: c.buy || "",
              rating: c.rating ?? null,
            };


            return (
              <GameCard
                key={c._id}
                game={shaped}
                onOpen={() => setActive(shaped)}
              />
            );
          })}
        </div>
      )}

      {active && <GameModal game={active} onClose={() => setActive(null)} />}
    </div>
  );
}
