// backend/api/ReviewerRequests.js
import express from "express";
import ReviewerRequest from "./ReviewerRequest.js";
import User from "./User.js";
import { requireAuth, requireRole } from "./AuthMiddleware.js";

const router = express.Router();

/** User: submit request */
router.post("/", requireAuth, async (req, res) => {
  const message = (req.body?.message || "").slice(0, 2000);

  // prevent spam duplicates
  const existing = await ReviewerRequest.findOne({
    userId: req.user.userId,
    status: "pending"
  });
  if (existing) return res.status(409).json({ error: "Request already pending" });

  // read username from DB to be safe
  const u = await User.findById(req.user.userId);
  const doc = await ReviewerRequest.create({
    userId: u._id,
    username: u.username,
    message
  });

  res.status(201).json(doc);
});

/** Admin: list requests */
router.get("/", requireAuth, requireRole("admin"), async (req, res) => {
  const list = await ReviewerRequest.find().sort({ createdAt: -1 });
  res.json(list);
});

/** Admin: approve request => set role reviewer */
router.put("/:id/approve", requireAuth, requireRole("admin"), async (req, res) => {
  const decisionNote = (req.body?.decisionNote || "").slice(0, 2000);

  const rr = await ReviewerRequest.findByIdAndUpdate(
    req.params.id,
    { status: "approved", decisionNote },
    { new: true }
  );
  if (!rr) return res.status(404).json({ error: "Not found" });

  await User.findByIdAndUpdate(rr.userId, { role: "reviewer" });
  res.json(rr);
});

/** Admin: reject request */
router.put("/:id/reject", requireAuth, requireRole("admin"), async (req, res) => {
  const decisionNote = (req.body?.decisionNote || "").slice(0, 2000);

  const rr = await ReviewerRequest.findByIdAndUpdate(
    req.params.id,
    { status: "rejected", decisionNote },
    { new: true }
  );
  if (!rr) return res.status(404).json({ error: "Not found" });

  res.json(rr);
});

export default router;
