import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import GameCard from "../components/GameCard";
import GameModal from "../components/GameModal";
import { loadUserReviews } from "../App";

const USER_REVIEWS_EVENT = "hc:userreviews";

export default function Home() {
  const [games, setGames] = useState([]);
  const [active, setActive] = useState(null);
  const [err, setErr] = useState("");
  const [reviewsTick, setReviewsTick] = useState(0);
  const [serverLatest, setServerLatest] = useState([]);
  const [rawgNewReleases, setRawgNewReleases] = useState([]);
  const [devCards, setDevCards] = useState([]);
  const [latestMeta, setLatestMeta] = useState({});

  useEffect(() => {
    const ids = Array.from(
      new Set(
        (serverLatest || [])
          .map((r) => r.gameId)
          .filter((gid) => typeof gid === "string" && gid.startsWith("rawg:"))
          .map((gid) => gid.replace("rawg:", ""))
      )
    ).slice(0, 12);

    const missing = ids.filter((id) => !latestMeta[id]);
    if (!missing.length) return;

    (async () => {
      try {
        const results = await Promise.all(
          missing.map(async (id) => {
            const g = await api(`/rawg/games/${id}`);
            return [String(id), { platform: g.platform || "", releaseDate: g.releaseDate || "" }];
          })
        );

        setLatestMeta((prev) => {
          const next = { ...prev };
          for (const [id, meta] of results) next[id] = meta;
          return next;
        });
      } catch (e) {
        console.error("Failed to prefetch latest RAWG meta:", e);
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [serverLatest]);


  useEffect(() => {
    (async () => {
      try {
        const list = await api("/developer-cards"); // Mongo dev cards
        setDevCards(Array.isArray(list) ? list : []);
      } catch {
        setDevCards([]);
      }
    })();
  }, []);
  useEffect(() => {
    const onChange = () => setReviewsTick(v => v + 1);
    window.addEventListener(USER_REVIEWS_EVENT, onChange);
    window.addEventListener("storage", onChange);
    return () => {
      window.removeEventListener(USER_REVIEWS_EVENT, onChange);
      window.removeEventListener("storage", onChange);
    };
  }, []);
  useEffect(() => {
    (async () => {
      try {
        const data = await api("/rawg/new-releases");
        setRawgNewReleases(data.games || []);
      } catch {
        setRawgNewReleases([]);
      }
    })();
  }, []);


  useEffect(() => {
    (async () => {
      try {
        const data = await api("/reviews/latest");
        const list = Array.isArray(data) ? data : (data?.reviews || []);
        setServerLatest(list);
      } catch {
        setServerLatest([]);
      }
    })();
  }, [reviewsTick]);

  useEffect(() => {
    (async () => {
      try {
        const data = await api("/games");

        const list =
          Array.isArray(data?.games) ? data.games :
            Array.isArray(data) && data.length && Array.isArray(data[0]?.games) ? data[0].games :
              Array.isArray(data) ? data :
                [];

        setGames(list);
      } catch (ex) {
        setErr(ex.message);
      }
    })();
  }, []);

  // ===== CAROUSEL STATE =====
  const [idx, setIdx] = useState(0);
  const intervalRef = useRef(null);

  const [adminPicks, setAdminPicks] = useState([]);


  useEffect(() => {
    (async () => {
      try {
        const saved = await api("/admin-carousel"); // should be array: [{rawgId,title,cover,...}]
        const list = Array.isArray(saved) ? saved : [];

        // hydrate from RAWG so modal + carousel always has full data
        const full = await Promise.all(
          list.slice(0, 5).map(async (g) => {
            try {
              const details = await api(`/rawg/games/${g.rawgId}`);
              return { ...details, id: `rawg:${details.rawgId}` };
            } catch {
              // fallback if RAWG detail fails
              return { ...g, id: `rawg:${g.rawgId}` };
            }
          })
        );

        setAdminPicks(full.filter(Boolean));
      } catch {
        setAdminPicks([]);
      }
    })();
  }, []);


  const picks = useMemo(() => {
    // admin override if set
    if (adminPicks.length) return adminPicks;   // ✅ no mapping

    const ft = games.filter((g) =>
      ["featured", "trending"].includes((g.category || "").toLowerCase())
    );
    const pool = ft.length ? ft : games;
    return pool.slice(0, 5);
  }, [games, adminPicks]);

  const current = picks[idx];

  useEffect(() => {
    if (idx >= picks.length) setIdx(0);
  }, [picks.length, idx]);

  function stopAuto() {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  }

  function startAuto() {
    stopAuto();
    if (picks.length <= 1) return;
    intervalRef.current = setInterval(() => {
      setIdx((v) => (v + 1) % picks.length);
    }, 5000);
  }

  useEffect(() => {
    startAuto();
    return stopAuto;
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [picks.length]);

  function goPrev() {
    if (!picks.length) return;
    setIdx((v) => (v - 1 + picks.length) % picks.length);
    startAuto();
  }

  function goNext() {
    if (!picks.length) return;
    setIdx((v) => (v + 1) % picks.length);
    startAuto();
  }

  function goTo(i) {
    setIdx(i);
    startAuto();
  }
  function isMongoId(v) {
    return typeof v === "string" && /^[a-f0-9]{24}$/i.test(v);
  }

  async function openFromHome(item) {
    try {
      // ===== DEV CARDS =====
      const directId = item?._id || item?.id;
      if (isMongoId(directId) && item?.category === "developer") {
        setActive({
          ...item,
          id: directId,
          category: "developer",
          videos: Array.isArray(item.videos) && item.videos.length
            ? item.videos
            : (item.coverVideo ? [item.coverVideo] : []),
          images: Array.isArray(item.images) ? item.images : [],
        });
        return;
      }

      const devCardId = item?.devCardId;
      if (isMongoId(devCardId)) {
        const card = await api(`/developer-cards/${devCardId}`);
        setActive({
          ...card,
          id: card._id || card.id,
          category: "developer",
          videos: Array.isArray(card.videos) && card.videos.length
            ? card.videos
            : (card.coverVideo ? [card.coverVideo] : []),
          images: Array.isArray(card.images) ? card.images : [],
        });
        return;
      }

      // ===== IMPORTANT: LATEST REVIEWS RAWG HYDRATION =====
      // Latest review items store the real game id in item.gameId (ex: "rawg:2551")
      const gid = item?.gameId || item?.gameKey || item?.id;

      if (typeof gid === "string" && gid.startsWith("rawg:")) {
        const rawgId = gid.replace("rawg:", "");
        const details = await api(`/rawg/games/${rawgId}`);

        setActive({
          ...details,
          id: `rawg:${details.rawgId}`,   // <-- THIS makes /reviews?gameId=rawg:#### work
          // keep the clicked review text separate (don’t overwrite game.summary)
          pickedReviewText: item?.summary || "",
          pickedReviewScore: item?.rating ?? null,
          pickedReviewUser: item?.username || "",
        });
        return;
      }

      // ===== NORMAL GAMES (mongo Game model list) =====
      const found = (games || []).find((g) => g.id === gid);
      if (found) {
        setActive({
          ...found,
          pickedReviewText: item?.summary || "",
          pickedReviewScore: item?.rating ?? null,
          pickedReviewUser: item?.username || "",
        });
        return;
      }

      // fallback
      setActive(item);
    } catch (e) {
      console.error("Failed to open item from home:", e);
    }
  }




  // ===== SECTIONS =====
  const featured = useMemo(
    () => games.filter((g) =>
      ["featured", "trending"].includes((g.category || "").toLowerCase())
    ),
    [games]
  );

  const userReviews = useMemo(() => {
    // reviewsTick makes this recompute when addUserReview runs
    const list = loadUserReviews();
    return list.filter((r) => r.approved);
  }, [reviewsTick]);

  const latestReviews = useMemo(() => {
    const merged = [
      ...serverLatest.map((r) => {
        const gid = r.gameId || "";
        let platform = r.platform || "";
        let releaseDate = r.releaseDate || "";

        if (typeof gid === "string" && gid.startsWith("rawg:")) {
          const rawgId = gid.replace("rawg:", "");
          platform = latestMeta[rawgId]?.platform || platform;
          releaseDate = latestMeta[rawgId]?.releaseDate || releaseDate;
        }

        return {
          type: r.official ? "official" : "user",
          id: r._id,
          gameId: r.gameId,
          gameKey: r.gameId,

          platform,     // ✅ add
          releaseDate,  // ✅ add

          title: r.gameTitle || r.gameId,
          rating: r.score,
          username: r.userId?.username || "Staff",
          summary: r.text,
          createdAt: new Date(r.createdAt).getTime(),
          cover: r.cover || "",
          official: !!r.official,
        };
      }),


      // local (storage) reviews
      ...userReviews.map((r) => ({
        type: "user",
        id: r.gameId,
        reviewId: r.id,
        gameKey: r.gameId,
        title: r.gameTitle,
        releaseDate: r.releaseDate,
        rating: r.rating,
        username: r.username,
        summary: r.text,
        createdAt: r.createdAt,
        cover: r.cover,
      })),

      ...(devCards || []).map((c) => ({
        type: "dev",
        id: c._id,
        gameId: c._id,
        gameKey: c._id,
        category: "developer",
        title: c.title,
        platform: c.platform,
        releaseDate: c.releaseDate,
        publisher: c.publisher,
        summary: c.summary,
        cover: c.cover || c.images?.[0] || "",
        images: Array.isArray(c.images) ? c.images : [],
        videos: Array.isArray(c.videos) && c.videos.length ? c.videos : (c.coverVideo ? [c.coverVideo] : []),
        buy: c.buy || "",
        rating: c.rating ?? null,
        createdAt: new Date(c.createdAt || 0).getTime(),
      })),
    ].sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));

    const seen = new Set();
    const unique = [];
    for (const item of merged) {
      const key = item.gameKey || item.id;
      if (seen.has(key)) continue;
      seen.add(key);
      unique.push(item);
    }

    return unique.slice(0, 12);
  }, [serverLatest, userReviews, devCards, latestMeta]);







  const beloved = useMemo(() => {
    return (devCards || [])
      .map((c) => ({
        ...c,
        id: c._id,
        category: "developer",
        cover: c.cover || c.images?.[0] || "",
        videos: Array.isArray(c.videos) && c.videos.length ? c.videos : (c.coverVideo ? [c.coverVideo] : []),
        images: Array.isArray(c.images) ? c.images : [],
        buy: c.buy || "",
        rating: c.rating ?? null,
      }))
      .slice(0, 6);
  }, [devCards]);


  const upcomingRail = useMemo(() => {
    const merged = rawgNewReleases
      .map((g) => ({
        type: "rawg",
        id: `rawg:${g.rawgId}`,
        gameKey: `rawg:${g.rawgId}`,
        rawgId: g.rawgId,
        title: g.title,
        releaseDate: g.releaseDate,
        rating: g.rating,
        cover: g.cover,
        platform: (g.platforms || []).join(" • "),
        createdAt: new Date(g.releaseDate || Date.now()).getTime(),
      }))
      .sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));

    return merged.slice(0, 12);
  }, [rawgNewReleases]);


  return (
    <div>
      <section className="hc-hero">
        <div className="hc-hero-inner">
          <h1 className="hc-hero-title">GAMES</h1>
          <p className="hc-hero-sub">Find your next obsession.</p>
        </div>

        <div className="hc-rule" />

        <div className="hc-picks">
          <h2 className="hc-picks-title">Tonight&apos;s picks</h2>
          <p className="hc-picks-sub">(Stop scrolling. Start playing.)</p>
        </div>

        {current && (
          <div className="hc-carousel-wrap">
            <div
              className="carouselCard"
              onMouseEnter={stopAuto}
              onMouseLeave={startAuto}
              onClick={() => setActive(current)}
              role="button"
              tabIndex={0}
            >
              <img className="carouselImg" src={current.cover} alt={current.title} />

              <button
                className="carArrow left"
                onClick={(e) => {
                  e.stopPropagation();
                  goPrev();
                }}
                aria-label="Previous"
              >
                ‹
              </button>

              <button
                className="carArrow right"
                onClick={(e) => {
                  e.stopPropagation();
                  goNext();
                }}
                aria-label="Next"
              >
                ›
              </button>

              <div className="carLabel">Spotlight: {current.title}</div>
            </div>

            <div className="carDots">
              {picks.map((_, i) => (
                <button
                  key={i}
                  className={`dot ${i === idx ? "on" : ""}`}
                  onClick={() => goTo(i)}
                  aria-label={`Go to slide ${i + 1}`}
                />
              ))}
            </div>
          </div>
        )}

        <div className="hc-rule" />
      </section>

      {err && <p style={{ color: "crimson", marginTop: 12 }}>{err}</p>}

      <Section
        title="Latest Reviews"
        subtitle="Latest reviews from the community."
        items={latestReviews}
        onOpen={openFromHome}
        emptyHint='Add games with category: "latest" to show them here.'
        seeAllTo="/gallery"
      />

      <Section
        title="Beloved"
        subtitle="Special Dev highlights."
        items={beloved}
        onOpen={openFromHome}
        emptyHint='Add games with category: "beloved/developer" to show them here.'
        seeAllTo="/reviews"
      />

      <Section
        title="New & Upcoming"
        subtitle="Fresh releases and what’s next."
        items={upcomingRail}
        onOpen={openFromHome}
        emptyHint="No new releases found on RAWG."
        seeAllTo="/upcoming"
      />

      {active && <GameModal game={active} onClose={() => setActive(null)} />}
    </div>
  );
}

function Section({ title, subtitle, items, onOpen, emptyHint, seeAllTo = "/reviews" }) {
  const railRef = useRef(null);
  const navigate = useNavigate();

  const scrollBy = (dir) => {
    const el = railRef.current;
    if (!el) return;
    const amt = Math.round(el.clientWidth * 0.9);
    el.scrollBy({ left: dir * amt, behavior: "smooth" });
  };

  return (
    <div style={{ marginTop: 28 }}>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "baseline",
          gap: 12,
        }}
      >
        <div style={{ display: "flex", alignItems: "baseline", gap: 12 }}>
          <h2 className="hc-hero-title" style={{ margin: 0, fontSize: "34px", lineHeight: "1.2" }}>
            {title}
          </h2>

          <button
            type="button"
            className="railSeeAll"
            onClick={() => navigate(seeAllTo)}
          >
            SEE ALL
          </button>
        </div>

        <div style={{ display: "flex", gap: 10 }}>
          <button type="button" className="railArrow" onClick={() => scrollBy(-1)} aria-label={`Scroll ${title} left`}>
            ‹
          </button>
          <button type="button" className="railArrow" onClick={() => scrollBy(1)} aria-label={`Scroll ${title} right`}>
            ›
          </button>
        </div>
      </div>

      {subtitle && (
        <p className="hc-hero-sub" style={{ marginTop: 3, marginBottom: 10 }}>
          {subtitle}
        </p>
      )}

      <div className="rail-line" />

      {items.length === 0 ? (
        <div className="card p-3" style={{ background: "#b3b3b3ff", borderRadius: 12 }}>
          {emptyHint}
        </div>
      ) : (
        <div className="rail" ref={railRef}>
          {items.map((g) => (
            <div className="railItem" key={g._id || g.id}>
              <GameCard game={g} onOpen={() => onOpen(g)} />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
