// backend/api/Users.js
import express from "express";
import bcrypt from "bcrypt";
import jwt from "jsonwebtoken";
import User from "./User.js";
import { requireAuth, requireRole } from "./AuthMiddleware.js";

const router = express.Router();

/* ========= POST /api/users/register ========= */
router.post("/register", async (req, res) => {
  try {
    const { email, username, password } = req.body;

    if (!email || !username || !password) {
      return res.status(400).json({ error: "email, username, password are required" });
    }

    const existing = await User.findOne({ $or: [{ email }, { username }] });
    if (existing) {
      return res.status(409).json({ error: "Email or username already in use" });
    }

    const passwordHash = await bcrypt.hash(password, 10);

    const user = await User.create({ email, username, passwordHash, role: "user" });

    res.status(201).json({
      message: "Registered",
      user: { id: user._id, email: user.email, username: user.username, role: user.role }
    });
  } catch {
    res.status(500).json({ error: "Failed to register" });
  }
});

/* ========= POST /api/users/login ========= */
router.post("/login", async (req, res) => {
  try {
    const { emailOrUsername, password } = req.body;

    if (!emailOrUsername || !password) {
      return res.status(400).json({ error: "emailOrUsername and password are required" });
    }

    const user = await User.findOne({
      $or: [{ email: String(emailOrUsername).toLowerCase() }, { username: emailOrUsername }]
    });

    if (!user) return res.status(401).json({ error: "Invalid credentials" });

    const ok = await bcrypt.compare(password, user.passwordHash);
    if (!ok) return res.status(401).json({ error: "Invalid credentials" });

    const token = jwt.sign(
      { userId: String(user._id), email: user.email, username: user.username, role: user.role },
      process.env.JWT_SECRET,
      { expiresIn: "2h" }
    );

    res.status(200).json({
      message: "Logged in",
      token,
      user: { id: user._id, email: user.email, username: user.username, role: user.role }
    });
  } catch {
    res.status(500).json({ error: "Failed to login" });
  }
});

/* ========= GET /api/users/me ========= */
router.get("/me", requireAuth, async (req, res) => {
  const user = await User.findById(req.user.userId).select("-passwordHash");
  res.json(user);
});

/* ========= PUT /api/users/me/settings ========= */
router.put("/me/settings", requireAuth, async (req, res) => {
  const { anonymous, theme, avatarUrl } = req.body;

  const updates = {};
  if (typeof anonymous === "boolean") updates.anonymous = anonymous;
  if (theme === "light" || theme === "dark") updates.theme = theme;
  if (typeof avatarUrl === "string") updates.avatarUrl = avatarUrl;

  const user = await User.findByIdAndUpdate(req.user.userId, updates, { new: true })
    .select("-passwordHash");

  res.json(user);
});

/* ========= PUT /api/users/me/password ========= */
router.put("/me/password", requireAuth, async (req, res) => {
  const { currentPassword, newPassword } = req.body;

  if (!currentPassword || !newPassword) {
    return res.status(400).json({ error: "Missing fields" });
  }
  if (newPassword.length < 6) {
    return res.status(400).json({ error: "New password must be at least 6 characters" });
  }

  const user = await User.findById(req.user.userId);
  const ok = await bcrypt.compare(currentPassword, user.passwordHash);
  if (!ok) return res.status(401).json({ error: "Current password incorrect" });

  user.passwordHash = await bcrypt.hash(newPassword, 10);
  await user.save();

  res.json({ ok: true });
});

/* ========= ADMIN: list all users ========= */
router.get("/", requireAuth, requireRole("admin"), async (req, res) => {
  const users = await User.find().select("-passwordHash").sort({ createdAt: -1 });
  res.json(users);
});

/* ========= ADMIN: set a user's role ========= */
router.put("/:id/role", requireAuth, requireRole("admin"), async (req, res) => {
  const { role } = req.body;

  if (!["user", "reviewer", "admin"].includes(role)) {
    return res.status(400).json({ error: "Invalid role" });
  }

  const updated = await User.findByIdAndUpdate(req.params.id, { role }, { new: true })
    .select("-passwordHash");

  if (!updated) return res.status(404).json({ error: "User not found" });

  res.json(updated);
});

export default router;
