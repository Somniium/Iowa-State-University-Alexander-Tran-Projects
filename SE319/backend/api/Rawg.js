import express from "express";
const router = express.Router();

const RAWG_BASE = "https://api.rawg.io/api";

/**
 * Appends RAWG API key to a URL
 */
function withKey(url) {
  const u = new URL(url);
  u.searchParams.set("key", process.env.RAWG_KEY || "");
  return u.toString();
}

/**
 * GET /api/rawg/games
 * List/search games (used by Gallery + Upcoming)
 *
 * Supports:
 * - search
 * - page / page_size
 * - ordering
 * - dates (YYYY-MM-DD,YYYY-MM-DD)
 */
router.get("/games", async (req, res) => {
  try {
    if (!process.env.RAWG_KEY) {
      return res.status(500).json({ error: "RAWG_KEY missing in backend .env" });
    }

    const {
      search = "",
      page = "1",
      page_size = "24",
      ordering = "-added",
      dates = "",
    } = req.query;

    // Build base URL
    const urlObj = new URL(`${RAWG_BASE}/games`);
    if (search) {
      urlObj.searchParams.set("search", search);
      // Make multi-word searches behave more “literal”
      urlObj.searchParams.set("search_precise", "true");
      // (optional) only for super strict titles:
      // urlObj.searchParams.set("search_exact", "true");
    }
    urlObj.searchParams.set("page", page);
    urlObj.searchParams.set("page_size", page_size);
    urlObj.searchParams.set("ordering", ordering);

    // format: "2025-12-15,2026-12-15"
    if (dates) urlObj.searchParams.set("dates", dates);

    const url = withKey(urlObj.toString());

    const r = await fetch(url);
    if (!r.ok) {
      return res.status(r.status).json({ error: "RAWG error", status: r.status });
    }

    const data = await r.json();

    const games = (data.results || []).map((g) => ({
      rawgId: g.id,
      title: g.name,
      releaseDate: g.released || "",
      rating: g.metacritic ?? null,
      cover: g.background_image || "",
      platforms: (g.platforms || []).map((p) => p.platform?.name).filter(Boolean),
      genres: (g.genres || []).map((x) => x.name).filter(Boolean),
      rawgUrl: g.slug ? `https://rawg.io/games/${g.slug}` : "",

      // modal fields (filled by detail endpoint)
      publisher: "",
      summary: "",
      images: [],
      buy: "",
    }));

    res.json({
      games,
      count: data.count,
      next: data.next,
      previous: data.previous,
    });
  } catch (e) {
    res.status(500).json({
      error: "RAWG request failed",
      detail: String(e),
    });
  }
});

/**
 * GET /api/rawg/games/:id
 * Full game details (used by GameModal)
 */
router.get("/games/:id", async (req, res) => {
  try {
    if (!process.env.RAWG_KEY) {
      return res.status(500).json({ error: "RAWG_KEY missing in backend .env" });
    }

    const id = req.params.id;

    const gameUrl = withKey(`${RAWG_BASE}/games/${id}`);
    const gameRes = await fetch(gameUrl);
    if (!gameRes.ok) {
      return res
        .status(gameRes.status)
        .json({ error: "RAWG error", status: gameRes.status });
    }
    const g = await gameRes.json();

    const shotsUrl = withKey(`${RAWG_BASE}/games/${id}/screenshots?page_size=10`);
    const shotsRes = await fetch(shotsUrl);
    const shotsJson = shotsRes.ok ? await shotsRes.json() : { results: [] };

    const images = (shotsJson.results || []).map((s) => s.image).filter(Boolean);

    /* -----------------------------
       3) Store links ("Where to buy")
    ------------------------------ */
    const storesUrl = withKey(`${RAWG_BASE}/games/${id}/stores`);
    const storesRes = await fetch(storesUrl);
    const storesJson = storesRes.ok ? await storesRes.json() : { results: [] };

    const buy =
      (storesJson.results || []).map((s) => s.url).find(Boolean) || g.website || "";

    res.json({
      rawgId: g.id,
      title: g.name,
      releaseDate: g.released || "",
      rating: g.metacritic ?? null,
      cover: g.background_image || "",
      platform: (g.platforms || [])
        .map((p) => p.platform?.name)
        .filter(Boolean)
        .join(" • "),
      publisher: (g.publishers || [])
        .map((p) => p.name)
        .filter(Boolean)
        .join(", "),
      summary: g.description_raw || "",
      images,
      buy,
      rawgUrl: g.slug ? `https://rawg.io/games/${g.slug}` : "",
      videos: [],
    });
  } catch (e) {
    res.status(500).json({
      error: "RAWG request failed",
      detail: String(e),
    });
  }
});

// GET /api/rawg/new-releases  (last 30 days)
router.get("/new-releases", async (_req, res) => {
  const end = new Date().toISOString().slice(0, 10);
  const start = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
    .toISOString()
    .slice(0, 10);

  const url = withKey(
    `${RAWG_BASE}/games?dates=${start},${end}&ordering=-released&page_size=12`
  );

  try {
    const r = await fetch(url);
    if (!r.ok) {
      return res.status(r.status).json({ error: "RAWG error", status: r.status });
    }

    const data = await r.json();

    const games = (data.results || []).map((g) => ({
      rawgId: g.id,
      title: g.name,
      releaseDate: g.released || "",
      rating: g.metacritic ?? null,
      cover: g.background_image || "",
      platforms: (g.platforms || []).map((p) => p.platform?.name).filter(Boolean),
      genres: (g.genres || []).map((x) => x.name).filter(Boolean),
      rawgUrl: g.slug ? `https://rawg.io/games/${g.slug}` : "",

      // keep list fields empty; modal hydrates via /games/:id
      publisher: "",
      summary: "",
      images: [],
      buy: "",
      videos: [],
    }));

    res.json({ games });
  } catch (e) {
    res.status(500).json({
      error: "RAWG new-releases failed",
      detail: String(e?.message || e),
    });
  }
});

export default router;
