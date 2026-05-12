// src/pages/Dashboard.jsx
import { useEffect, useState } from "react";
import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import { useNavigate } from "react-router-dom";

function scoreClass(score) {
  const n = Number(score);
  if (!Number.isFinite(n)) return "s-gray";
  if (n >= 90) return "s-green";
  if (n >= 75) return "s-yellow";
  return "s-red";
}

export default function Dashboard() {
  const { user } = useAuth();
  const nav = useNavigate();
  const [myReviews, setMyReviews] = useState([]);
  const [myCards, setMyCards] = useState([]);
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);
  const [cooldown, setCooldown] = useState(false);

  function triggerError(msg) {
    setErr(msg);
    setCooldown(true);

    setTimeout(() => {
      setCooldown(false);
    }, 5000);
  }



  // Create DEV card form (separate fields)
  const [cardForm, setCardForm] = useState({
    title: "",
    platform: "",
    releaseDate: "",
    publisher: "",
    summary: "",
    // user can paste a normal YouTube URL here
    coverVideo: "",
    // 0-100 rating
    rating: "",
    // 3 separate image fields
    img1: "",
    img2: "",
    img3: "",
  });

  // Review edit state
  const [editingId, setEditingId] = useState(null);
  const [editScore, setEditScore] = useState(80);
  const [editText, setEditText] = useState("");

  async function load() {
    setErr("");
    try {
      const [reviews, cards] = await Promise.all([api("/reviews/me"), api("/usercards")]);
      setMyReviews(Array.isArray(reviews) ? reviews : []);
      setMyCards(Array.isArray(cards) ? cards : []);
    } catch (e) {
      setErr(e.message || "Failed to load dashboard");
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function startEdit(r) {
    setEditingId(r._id);
    setEditScore(r.score ?? 80);
    setEditText(r.text ?? "");
  }

  function cancelEdit() {
    setEditingId(null);
    setEditScore(80);
    setEditText("");
  }

  async function saveEdit() {
    if (!editingId) return;

    setErr("");
    try {
      await api(`/reviews/${editingId}`, {
        method: "PUT",
        body: { score: editScore, text: editText },
      });

      cancelEdit();
      load();
    } catch (e) {
      setErr(e.message || "Failed to update review");
    }
  }

  async function deleteCard(card) {
    const ok = window.confirm("Delete this card? This cannot be undone.");
    if (!ok) return;

    setErr("");
    try {
      const isDev = card?.category === "developer";
      const path = isDev
        ? `/developer-cards/admin/${card._id}`
        : `/usercards/${card._id}`;

      await api(path, { method: "DELETE" });

      setMyCards((prev) => prev.filter((c) => c._id !== card._id));
    } catch (e) {
      setErr(e.message || "Failed to delete card");
    }
  }

  async function deleteReview(id) {
    const ok = window.confirm("Delete this review? This cannot be undone.");
    if (!ok) return;

    setErr("");
    try {
      await api(`/reviews/${id}`, { method: "DELETE" }); // uses the new backend route
      setMyReviews((prev) => prev.filter((r) => r._id !== id));
    } catch (e) {
      setErr(e?.message || "Failed to delete review");
    }
  }


  // Create DEV card (now uses separate img fields + rating)
  async function addCard() {
    // hard stop if there's already an error showing
    if (loading || cooldown) return;

    // basic validation (stop card creation if invalid)
    if (!cardForm.title?.trim()) return triggerError("Title is required.");
    if (!cardForm.publisher?.trim()) return triggerError("Publisher is required.");
    if (!cardForm.summary?.trim()) return triggerError("Summary is required.");

    setLoading(true);
    setErr("");
    const images = [cardForm.img1, cardForm.img2, cardForm.img3]
      .map((s) => (s || "").trim())
      .filter(Boolean);
    const rating = cardForm.rating ? Number(cardForm.rating.trim()) : null;
    try {
      // define created
      const created = await api("/developer-cards/admin", {
        method: "POST",
        body: {
          title: cardForm.title.trim(),
          platform: cardForm.platform.trim(),
          releaseDate: cardForm.releaseDate.trim(),
          publisher: cardForm.publisher.trim(),
          summary: cardForm.summary.trim(),
          cover: cardForm.cover.trim(),
          buy: cardForm.buy.trim(),
          coverVideo: cardForm.coverVideo.trim(),
          images,
          rating,
        },
      });

      // ONLY navigate if we successfully created
      nav("/checkout", {
        state: {
          message: "CONGRATS — YOU MADE A REVIEW",
          title: created?.title || cardForm.title.trim(),
          kind: "Developer Card",
          againPath: "/dashboard",
          seePath: "/",
        },
      });
    } catch (e) {
      setErr(e?.message || "Failed to create developer card.");
    } finally {
      setLoading(false);
    }
  }


  return (
    <div className="hc-page">
      <div className="hc-head">
        <h1 className="hc-title">Dashboard</h1>
        <p className="hc-sub">Welcome back{user?.username ? `, @${user.username}` : ""}.</p>
      </div>

      {err ? <div className="hc-error">{err}</div> : null}

      <div className="hc-grid">
        {/* MY REVIEWS */}
        <section className="hc-card hc-spaced">
          <div className="hc-cardHead">
            <h2>Your Reviews</h2>
            <span className="hc-pill">{myReviews.length}</span>
          </div>

          {!myReviews.length ? (
            <div className="hc-muted">You haven't posted any reviews yet.</div>
          ) : (
            <div className="hc-reviewPills">
              {myReviews.map((r) => (
                <div key={r._id} className="hc-reviewPill">
                  <div className="hc-reviewTop">
                    <div className="hc-reviewGame">{r.gameTitle || r.gameId}</div>
                    <span className={`hc-pill ${scoreClass(r.score)}`}>{r.score}</span>
                  </div>

                  {editingId === r._id ? (
                    <>
                      <div className="hc-actions" style={{ marginBottom: 10 }}>
                        <span className="hc-pill">Score</span>
                        <input
                          type="number"
                          min="0"
                          max="100"
                          value={editScore}
                          onChange={(e) => setEditScore(Number(e.target.value))}
                          className="admin-select"
                          style={{ width: 120 }}
                        />
                      </div>

                      <textarea
                        value={editText}
                        onChange={(e) => setEditText(e.target.value)}
                        style={{ width: "100%", minHeight: 90 }}
                      />

                      <div className="hc-actions" style={{ marginTop: 10 }}>
                        <button className="hc-pill-btn hc-post" type="button" onClick={saveEdit}>
                          Save
                        </button>
                        <button className="hc-pill-btn hc-edit" type="button" onClick={cancelEdit}>
                          Cancel
                        </button>
                      </div>
                    </>
                  ) : (
                    <>
                      <div className="hc-reviewText">{r.text}</div>

                      <div className="hc-actions">
                        <button className="hc-pill-btn hc-edit" type="button" onClick={() => startEdit(r)}>
                          Edit
                        </button>
                        <button className="hc-pill-btn hc-delete" type="button" onClick={() => deleteReview(r._id)}>
                          Delete
                        </button>
                      </div>
                    </>
                  )}
                </div>
              ))}
            </div>
          )}
        </section>

        {/* CREATE DEV CARD */}
        <section className="hc-card hc-spaced">
          <div className="hc-cardHead">
            <h2>Create Developer Card</h2>
            <span className="hc-pill">DEV</span>
          </div>

          <div className="hc-formGrid" style={{ gap: 10 }}>
            <input
              className="admin-select"
              placeholder="Title *"
              value={cardForm.title}
              onChange={(e) => setCardForm({ ...cardForm, title: e.target.value })}
            />
            <input
              className="admin-select"
              placeholder="Platform *"
              value={cardForm.platform}
              onChange={(e) => setCardForm({ ...cardForm, platform: e.target.value })}
            />
            <input
              className="admin-select"
              placeholder="Release date (YYYY-MM-DD)"
              value={cardForm.releaseDate}
              onChange={(e) => setCardForm({ ...cardForm, releaseDate: e.target.value })}
            />
            <input
              className="admin-select"
              placeholder="Publisher"
              value={cardForm.publisher}
              onChange={(e) => setCardForm({ ...cardForm, publisher: e.target.value })}
            />

            <textarea
              placeholder="Summary *"
              value={cardForm.summary}
              onChange={(e) => setCardForm({ ...cardForm, summary: e.target.value })}
              style={{ width: "100%", minHeight: 90 }}
            />

            <input
              placeholder="YouTube URL (watch?v=... or youtu.be/...) *"
              className="admin-select"
              value={cardForm.coverVideo}
              onChange={(e) => setCardForm({ ...cardForm, coverVideo: e.target.value })}
            />

            <input
              type="number"
              min="0"
              max="100"
              placeholder="Rating (0-100)"
              className="admin-select"
              value={cardForm.rating}
              onChange={(e) => setCardForm({ ...cardForm, rating: e.target.value })}
            />
            <input
              className="admin-select"
              placeholder="Cover image URL (poster, not in slideshow)"
              value={cardForm.cover}
              onChange={(e) => setCardForm({ ...cardForm, cover: e.target.value })}
            />

            <input
              className="admin-select"
              placeholder="Store link (Steam / Epic / etc.)"
              value={cardForm.buy}
              onChange={(e) => setCardForm({ ...cardForm, buy: e.target.value })}
            />


            <input
              className="admin-select"
              placeholder="Slide image URL #1 *"
              value={cardForm.img1}
              onChange={(e) => setCardForm({ ...cardForm, img1: e.target.value })}
            />
            <input
              className="admin-select"
              placeholder="Slide image URL #2 *"
              value={cardForm.img2}
              onChange={(e) => setCardForm({ ...cardForm, img2: e.target.value })}
            />
            <input
              className="admin-select"
              placeholder="Slide image URL #3 *"
              value={cardForm.img3}
              onChange={(e) => setCardForm({ ...cardForm, img3: e.target.value })}
            />
          </div>

          <button
            type="button"
            onClick={addCard}
            disabled={loading || cooldown}
            className={`hc-pill-btn hc-post ${loading || cooldown ? "disabledButton" : ""}`}
          >
            {loading
              ? "Processing..."
              : cooldown
                ? "Please wait..."
                : "Create Developer Card"}
          </button>

        </section>
      </div>

      {/* USER CARDS */}
      <section className="hc-card hc-spaced">
        <div className="hc-cardHead">
          <h2>Your Cards</h2>
          <span className="hc-pill">{myCards.length}</span>
        </div>

        {!myCards.length ? (
          <div className="hc-muted">No cards yet. Create one above.</div>
        ) : (
          <div className="hc-cardsGrid">
            {myCards.map((c) => (
              <div key={c._id} className="hc-cardMini">
                <div className="hc-cardMiniTop">
                  <div>
                    <div className="hc-cardMiniTitle">{c.title}</div>
                    <div className="hc-cardMiniMeta">
                      {c.platform || "—"} • {c.releaseDate || "—"}
                    </div>
                  </div>
                  <span className="hc-pill">Custom</span>
                </div>

                <div className="hc-cardMiniBody">{c.summary || <span className="hc-muted">No summary.</span>}</div>

                {c.rating != null ? (
                  <div className="hc-actions" style={{ marginTop: 10 }}>
                    <span className={`hc-pill ${scoreClass(c.rating)}`}>{c.rating}</span>
                  </div>
                ) : null}

                {c.images?.length ? (
                  <div className="hc-miniThumbs">
                    {c.images.slice(0, 3).map((src, i) => (
                      <img key={i} src={src} alt="" />
                    ))}
                  </div>
                ) : null}

                <div className="hc-actions" style={{ marginTop: 10 }}>
                  <button type="button" className="hc-pill-btn hc-delete" onClick={() => deleteCard(c)}>
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
