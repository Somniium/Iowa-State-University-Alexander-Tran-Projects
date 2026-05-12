import express from "express";
import { GoogleGenAI } from "@google/genai";
import dotenv from "dotenv";
dotenv.config();

const router = express.Router();

const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_KEY });

async function getFallbackGames() {
  try {
    console.log("[HonestBot] Fetching fallback games from new releases");
    const response = await fetch("http://127.0.0.1:5000/api/rawg/new-releases");

    if (!response.ok) {
      throw new Error(`RAWG API returned status ${response.status}`);
    }

    const data = await response.json();
    const games = data.games || [];

    return games.slice(0, 3).map((game) => ({
      rawgId: game.rawgId,
      title: game.title,
      releaseDate: game.releaseDate,
      genres: game.genres || [],
      summary:
        game.summary || "A recently released game that might interest you.",
      rating: game.rating || null,
      cover: game.cover || "https://via.placeholder.com/220x308?text=No+Image",
    }));
  } catch (fallbackError) {
    console.error("[HonestBot] Fallback fetch failed:", fallbackError.message);
    return [];
  }
}

router.post("/", async (req, res) => {
  const { prompt } = req.body;

  if (!prompt) {
    return res.status(400).json({ error: "Prompt required" });
  }

  try {
    console.log("[HonestBot] Received request from client");

    if (!process.env.GEMINI_KEY) {
      console.error("[HonestBot] API key not found in environment");
      return res
        .status(500)
        .json({ error: "Server API Key configuration error" });
    }

    console.log("[HonestBot] API key validated");

    let gamesListText = "";
    let contextGames = [];

    try {
      const rawgResp = await fetch(
        "http://127.0.0.1:5000/api/rawg/games?page_size=20"
      );
      if (rawgResp.ok) {
        const rawgData = await rawgResp.json();
        contextGames = rawgData.games || [];
        console.log(
          `[HonestBot] Retrieved ${contextGames.length} games from RAWG database`
        );

        gamesListText = contextGames
          .map(
            (g) =>
              `- ${g.title} (${g.genres.join(", ")}) - Released: ${
                g.releaseDate
              } - Rating: ${g.rating || "N/A"}`
          )
          .join("\n");
      }
    } catch (rawgErr) {
      console.warn("[HonestBot] RAWG fetch failed:", rawgErr.message);
    }

    const structuredPrompt = `
You are a video game recommendation assistant. Based on the user's request, recommend exactly 3 games.

User Request: "${prompt}"

Available games from our database:
${gamesListText || "No games available in context"}

IMPORTANT INSTRUCTIONS:
1. Try to recommend games from the "Available games" list above that match the user's request
2. If you can't find 3 matching games in the list, you may suggest similar well-known games
3. Return ONLY a valid JSON array with no additional text, markdown, or formatting
4. Each game MUST have these exact fields:
   - title: string (game name)
   - releaseDate: string (YYYY-MM-DD format)
   - genres: array of strings (e.g., ["Action", "RPG"])
   - summary: string (2-3 sentences describing the game)
   - rating: number (0-100, use null if unknown)
   - cover: string (use a placeholder URL)

Example format:
[
  {
    "title": "The Witcher 3: Wild Hunt",
    "releaseDate": "2015-05-19",
    "genres": ["Action", "RPG"],
    "summary": "An open-world RPG set in a fantasy universe full of meaningful choices and impactful consequences.",
    "rating": 92,
    "cover": "https://via.placeholder.com/220x308?text=The+Witcher+3"
  }
]

RESPOND WITH ONLY THE JSON ARRAY, NO OTHER TEXT.
`;

    console.log("[HonestBot] Sending request to Gemini API...");

    const response = await ai.models.generateContent({
      /* Choose one of the available Gemini models */
      //model: "gemini-1.5-flash-latest",
      //model: "gemini-1.5-pro-latest",
      //model: "gemini-2.0-flash-exp",
      model: "gemini-2.5-flash",
      contents: structuredPrompt,
    });

    const text = response.text;

    console.log("[HonestBot] Response received from Gemini");
    console.log(`[HonestBot] Response preview: ${text.substring(0, 100)}...`);

    const cleanJson = text.replace(/```json|```/g, "").trim();

    try {
      const recommendations = JSON.parse(cleanJson);

      if (!Array.isArray(recommendations) || recommendations.length === 0) {
        console.error("[HonestBot] Invalid recommendations format");
        return res.status(500).json({
          error: "Invalid recommendations format",
          rawResponse: cleanJson,
        });
      }

      console.log(
        `[HonestBot] Parsed ${recommendations.length} game recommendations`
      );
      console.log("[HonestBot] Enriching recommendations with RAWG metadata");

      const enrichedRecommendations = recommendations.map((rec) => {
        const matchingGame = contextGames.find(
          (g) => g.title.toLowerCase() === rec.title.toLowerCase()
        );

        if (matchingGame) {
          console.log(`[HonestBot] Matched: ${rec.title}`);
          return {
            ...rec,
            rawgId: matchingGame.rawgId,
            cover: matchingGame.cover || rec.cover,
            rating: matchingGame.rating || rec.rating,
            genres:
              matchingGame.genres.length > 0 ? matchingGame.genres : rec.genres,
            rawgUrl: matchingGame.rawgUrl,
          };
        }

        return rec;
      });

      console.log("[HonestBot] Sending recommendations to client");
      res.json({ recommendations: enrichedRecommendations });
    } catch (parseError) {
      console.error("[HonestBot] JSON parse failed");
      console.error(`[HonestBot] Error: ${parseError.message}`);
      console.error(`[HonestBot] Raw response: ${text}`);
      return res.status(500).json({
        error: "Failed to parse AI response",
        parseError: parseError.message,
        rawResponse: text,
      });
    }
  } catch (err) {
    console.error("[HonestBot] Request failed");
    console.error(`[HonestBot] Error type: ${err.constructor.name}`);
    console.error(`[HonestBot] Message: ${err.message}`);

    if (process.env.NODE_ENV === "development") {
      console.error("[HonestBot] Stack trace:");
      console.error(err.stack);
    }

    console.log("[HonestBot] Attempting to provide fallback recommendations");
    const fallbackGames = await getFallbackGames();

    if (fallbackGames.length > 0) {
      console.log(
        `[HonestBot] Sending ${fallbackGames.length} fallback games to client`
      );
      return res.json({
        recommendations: fallbackGames,
        fallback: true,
        message:
          "AI service unavailable. Here are some recent releases you might enjoy.",
      });
    }

    res.status(500).json({
      error: "Internal server error",
      details: err.message,
    });
  }
});

export default router;
