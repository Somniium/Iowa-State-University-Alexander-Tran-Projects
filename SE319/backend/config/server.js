import express from "express";
import cors from "cors";
import dotenv from "dotenv";
import { connectDB } from "./db.js";
import usersRoutes from "../api/Users.js";
import reviewsRoutes from "../api/Reviews.js";
import gamesRoutes from "../api/Games.js";
import userCardsRoutes from "../api/UserCards.js";
import reviewerRequestsRoutes from "../api/ReviewerRequests.js";
import rawgRouter from "../api/Rawg.js";
import userGameCardsRouter from "../api/UserGameCards.js";
import adminCarouselRouter from "../api/AdminCarousel.js";
import ChatBotRouter from "../api/ChatBotSDK.js";
import developerCardsRouter from "../api/DeveloperCards.js";
import developerCardsAdminRouter from "../api/DeveloperCardsAdmin.js";


dotenv.config();

const app = express();

app.use(cors());
app.use(express.json({ limit: "10mb" }));
app.use(express.urlencoded({ limit: "10mb", extended: true }));

app.get("/api/health", (req, res) => res.json({ ok: true }));

app.use("/api/rawg", rawgRouter);
app.use("/api/games", gamesRoutes);
app.use("/api/users", usersRoutes);
app.use("/api/reviews", reviewsRoutes);
app.use("/api/usercards", userCardsRoutes);
app.use("/api/reviewer-requests", reviewerRequestsRoutes);
app.use("/api/usercards", userGameCardsRouter);
app.use("/api/admin-carousel", adminCarouselRouter);
app.use("/api/chatbot", ChatBotRouter);
app.use("/api/developer-cards", developerCardsRouter);
app.use("/api/developer-cards", developerCardsAdminRouter);

await connectDB(process.env.MONGO_URI);

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
  console.log(`[Server] Running on port ${PORT}`);
});
