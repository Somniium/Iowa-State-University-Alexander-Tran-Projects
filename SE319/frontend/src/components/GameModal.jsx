import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";
import ReviewForm from "./ReviewForm";
import ReviewList from "./ReviewList";

function scoreClass(value) {
  const n = Number(value);
  if (!Number.isFinite(n)) return "b-gray";
  if (n >= 90) return "b-green";
  if (n >= 75) return "b-yellow";
  return "b-red";
}

function scorePercent(value) {
  const n = Number(value);
  if (!Number.isFinite(n)) return 0;
  return Math.max(0, Math.min(100, n));
}

// Split text into sentences and return first N sentences
function splitIntoSentences(text) {
  if (!text) return [];
  return String(text)
    .replace(/\s+/g, " ")
    .trim()
    .split(/(?<=[.!?])\s+/)
    .filter(Boolean);
}

export default function GameModal({ game, onClose }) {
  const { user, token } = useAuth();
  const nav = useNavigate();

  const [reviews, setReviews] = useState([]);
  const [reviewErr, setReviewErr] = useState("");

  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);

  // reviewer/admin comment UI (comment on an existing user review)
  const [showComment, setShowComment] = useState(false);
  const [commentTargetId, setCommentTargetId] = useState("");
  const [commentText, setCommentText] = useState("");
  const [commentStatus, setCommentStatus] = useState("");
  const [commentErr, setCommentErr] = useState("");

  // reviewer request UI
  const [requestMsg, setRequestMsg] = useState("");
  const [requestStatus, setRequestStatus] = useState("");

  const rating = game?.rating ?? "tbd";
  const pct = useMemo(() => scorePercent(rating), [rating]);
  const pillClass = useMemo(() => scoreClass(rating), [rating]);
  const isDev = game?.category === "developer";
  const primaryVideo = game?.videos?.[0];

  // ----- Slideshow images -----
  const images = useMemo(() => {
    const list = [];
    if (isDev) {
      if (Array.isArray(game?.images)) list.push(...game.images);
      return Array.from(new Set(list)).filter(Boolean);
    }
    if (game?.cover) list.push(game.cover);
    if (Array.isArray(game?.images)) list.push(...game.images);
    return Array.from(new Set(list)).filter(Boolean);
  }, [isDev, game?.cover, game?.images]);

  const mediaSlides = useMemo(() => {
    if (!isDev) return [];
    const slides = [];
    if (primaryVideo) slides.push({ type: "video", src: primaryVideo });
    (images || []).forEach((u) => slides.push({ type: "image", src: u }));
    return slides;
  }, [isDev, primaryVideo, images]);

  // existing non-dev image index
  const [imgIdx, setImgIdx] = useState(0);
  // dev mixed slideshow index
  const [slideIdx, setSlideIdx] = useState(0);

  // reset on game change
  useEffect(() => {
    setImgIdx(0);
    setSlideIdx(0);
  }, [game?.id, game?.rawgId]);

  function imgPrev() {
    if (!images.length) return;
    setImgIdx((i) => (i - 1 + images.length) % images.length);
  }
  function imgNext() {
    if (!images.length) return;
    setImgIdx((i) => (i + 1) % images.length);
  }

  function slidePrev() {
    if (!mediaSlides.length) return;
    setSlideIdx((i) => (i - 1 + mediaSlides.length) % mediaSlides.length);
  }
  function slideNext() {
    if (!mediaSlides.length) return;
    setSlideIdx((i) => (i + 1) % mediaSlides.length);
  }
  function youtubeIdFromEmbed(url = "") {
    const u = String(url);
    const m = u.match(/youtube\.com\/embed\/([^?&#/]+)/);
    return m?.[1] || "";
  }

  function youtubeThumb(url = "") {
    const id = youtubeIdFromEmbed(url);
    return id ? `https://img.youtube.com/vi/${id}/hqdefault.jpg` : "";
  }


  const isReviewerOrAdmin = user?.role === "reviewer" || user?.role === "admin";

  // dev “see full review” expands inside modal
  const [showFullSummary, setShowFullSummary] = useState(false);
  useEffect(() => {
    setShowFullSummary(false);
  }, [game?.id, game?.rawgId]);

  // --- Summary preview (first 4 sentences) ---
  const fullSummary = useMemo(() => {
    const raw = game?.summary || "";
    return String(raw).replace(/<[^>]*>/g, "").trim();
  }, [game?.summary]);

  const summarySentences = useMemo(() => splitIntoSentences(fullSummary), [fullSummary]);
  const summaryLimit = 4;
  const hasMoreSummary = summarySentences.length > summaryLimit;

  const summaryPreview = useMemo(() => {
    if (!fullSummary) return "";
    if (!hasMoreSummary) return fullSummary;
    return summarySentences.slice(0, summaryLimit).join(" ");
  }, [fullSummary, hasMoreSummary, summarySentences]);

  // Prefer RAWG page (if present), then website, then buy
  const seeFullHref = game?.rawgUrl || game?.website || game?.buy || "";

  async function loadReviews() {
    try {
      setReviewErr("");
      const data = await api(`/reviews?gameId=${encodeURIComponent(game?.id)}`);
      const list = Array.isArray(data) ? data : [];
      setReviews(list);

      if (!commentTargetId && list.length) {
        setCommentTargetId(list[0]._id);
      }
    } catch (e) {
      setReviews([]);
      setReviewErr(e?.message || "Failed to load reviews");
    }
  }

  useEffect(() => {
    if (!game?.id) return;
    loadReviews();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [game?.id]);

  function canModify(r) {
    const rid = typeof r.userId === "object" ? r.userId?._id : r.userId;
    return !!user?.id && String(rid) === String(user.id);
  }

  async function handleDeleteComment(reviewId, commentId) {
    try {
      await api(`/reviews/${reviewId}/comments/${commentId}`, { method: "DELETE" });
      await loadReviews();
    } catch (e) {
      setCommentErr(e?.message || "Failed to delete comment");
    }
  }

  async function handleSubmit({ score, text }) {
    try {
      if (!token) {
        nav("/login");
        return;
      }

      const postUrl = isReviewerOrAdmin ? "/reviews/official" : "/reviews";

      if (editing) {
        await api(`/reviews/${editing._id}`, {
          method: "PUT",
          body: { score, text },
        });
      } else {
        const gameTitle = game.title || game.name || game.slug || String(game.id);
        const cover = game.cover || game.background_image || game.backgroundImage || "";

        await api(postUrl, {
          method: "POST",
          body: {
            gameId: game.id,
            gameTitle,
            cover,
            score,
            text,
          },
        });
      }

      window.dispatchEvent(new Event("hc:userreviews"));

      setShowForm(false);
      setEditing(null);
      await loadReviews();
    } catch (e) {
      setReviewErr(e?.message || "Failed to submit review");
    }
  }

  async function handleDelete(r) {
    try {
      await api(`/reviews/${r._id}`, { method: "DELETE" });
      await loadReviews();
    } catch (e) {
      setReviewErr(e?.message || "Failed to delete review");
    }
  }

  async function handleDeleteReviewAsAdmin(reviewId) {
    try {
      if (!reviewId || typeof reviewId !== "string") throw new Error("Invalid review ID");
      await api(`/reviews/${reviewId}/admin`, { method: "DELETE" });
      await loadReviews();
    } catch (e) {
      setReviewErr(e?.message || "Failed to delete review");
    }
  }

  function onReviewClick() {
    if (!token) {
      nav("/login");
      return;
    }
    setEditing(null);
    setShowForm(true);
  }

  function onCommentClick() {
    if (!token) {
      nav("/login");
      return;
    }
    setCommentErr("");
    setCommentStatus("");
    setShowComment((v) => !v);

    setTimeout(() => {
      document.querySelector(".gmReviewsBox")?.scrollIntoView({ behavior: "smooth" });
    }, 0);
  }

  async function submitComment() {
    try {
      if (!token) {
        nav("/login");
        return;
      }
      setCommentErr("");
      setCommentStatus("");

      const text = String(commentText || "").trim();
      if (!text) {
        setCommentErr("Please enter a comment.");
        return;
      }
      if (!commentTargetId) {
        setCommentErr("Choose a review to comment on.");
        return;
      }

      await api(`/reviews/${commentTargetId}/comment`, {
        method: "POST",
        body: { text },
      });

      setCommentText("");
      setCommentStatus("Comment posted.");
      await loadReviews();
    } catch (e) {
      setCommentErr(e?.message || "Failed to post comment");
    }
  }

  async function submitReviewerRequest() {
    try {
      if (!token) {
        nav("/login");
        return;
      }
      setRequestStatus("");
      await api("/reviewer-requests", { method: "POST", body: { message: requestMsg } });
      setRequestMsg("");
      setRequestStatus("Request sent! An admin will review it soon.");
    } catch (e) {
      setRequestStatus(e?.message || "Failed to send request");
    }
  }

  const devCurrent = isDev ? mediaSlides[slideIdx] : null;

  return (
    <div className="modalOverlay" onMouseDown={onClose}>
      <div className="gmModal" onMouseDown={(e) => e.stopPropagation()}>
        <button className="gmClose" onClick={onClose} aria-label="Close">
          ×
        </button>

        {/* LEFT MEDIA */}
        <div className="gmLeft">
          <div className="gmMedia">
            {isDev ? (
              <div className="gmHeroWrap">
                {devCurrent?.type === "video" ? (
                  <iframe
                    className="gmVideo"
                    src={devCurrent?.src}
                    title={game?.title}
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                    allowFullScreen
                  />
                ) : (
                  <img className="gmHeroImg" src={devCurrent?.src || game?.cover} alt={game?.title} />
                )}

                {mediaSlides.length > 1 && (
                  <>
                    <button className="gmImgArrow left" type="button" onClick={slidePrev} aria-label="Previous slide">
                      ‹
                    </button>
                    <button className="gmImgArrow right" type="button" onClick={slideNext} aria-label="Next slide">
                      ›
                    </button>
                  </>
                )}
              </div>
            ) : primaryVideo ? (
              <iframe
                className="gmVideo"
                src={primaryVideo}
                title={game?.title}
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              />
            ) : (
              <div className="gmHeroWrap">
                <img className="gmHeroImg" src={images[imgIdx] || game?.cover} alt={game?.title} />

                {images.length > 1 && (
                  <>
                    <button className="gmImgArrow left" type="button" onClick={imgPrev} aria-label="Previous image">
                      ‹
                    </button>
                    <button className="gmImgArrow right" type="button" onClick={imgNext} aria-label="Next image">
                      ›
                    </button>
                  </>
                )}
              </div>
            )}
          </div>

          {/* THUMBNAILS */}
          {isDev ? (
            mediaSlides.length > 1 && (
              <div className="gmThumbRow">
                {mediaSlides.slice(0, 6).map((s, i) => (
                  <button
                    key={`${s.type}:${s.src}:${i}`}
                    className={`gmThumb ${i === slideIdx ? "on" : ""}`}
                    type="button"
                    onClick={() => setSlideIdx(i)}
                    title={s.type === "video" ? "View video" : "View image"}
                  >
                    {s.type === "video" ? (
                      <img
                        src={youtubeThumb(s.src)}
                        alt="Video thumbnail"
                        onError={(e) => {
                          e.currentTarget.onerror = null;
                          e.currentTarget.replaceWith(Object.assign(document.createElement("div"), { className: "gmThumbVideo", innerText: "▶" }));
                        }}
                      />
                    ) : (
                      <img src={s.src} alt="" />
                    )}

                  </button>
                ))}
              </div>
            )
          ) : (
            images.length > 1 && (
              <div className="gmThumbRow">
                {images.slice(0, 6).map((url, i) => (
                  <button
                    key={url + i}
                    className={`gmThumb ${i === imgIdx ? "on" : ""}`}
                    type="button"
                    onClick={() => setImgIdx(i)}
                    title="View image"
                  >
                    <img src={url} alt={`Screenshot ${i + 1}`} />
                  </button>
                ))}
              </div>
            )
          )}

          <div className="gmInfoCard">
            <h3>Game publishing information</h3>
            <div className="gmInfoBody">{game?.publisher || "—"}</div>
          </div>

          <div className="gmInfoCard">
            <h3>Where to buy</h3>
            <div className="gmInfoBody">
              {game?.buy ? (
                <a href={game.buy} target="_blank" rel="noreferrer" className="gmStoreLink">
                  Visit store →
                </a>
              ) : (
                "—"
              )}
            </div>
          </div>
        </div>

        {/* RIGHT DETAILS */}
        <div className="gmRight">
          <div className="gmHeader">
            <h2 className="gmTitle">{game?.title}</h2>
            <div className="gmMeta">
              {game?.platform || "—"} • {game?.releaseDate || "—"}
            </div>
          </div>

          <div className="gmScoreRow">
            <div className="gmScoreBar">
              <div className={`gmScoreFill ${pillClass}`} style={{ width: `${pct}%` }} />
            </div>
            <div className={`score-pill ${pillClass}`}>
              <span>{Number.isFinite(Number(rating)) ? rating : "—"}</span>
            </div>
          </div>

          <div className="gmCard gmDescCard">
            <div className="gmCardBody gmDescBody">
              {fullSummary ? (
                <>
                  {isDev ? (showFullSummary ? fullSummary : summaryPreview) : summaryPreview}
                  {!isDev && hasMoreSummary ? " …" : ""}
                  {hasMoreSummary ? (
                    <div style={{ marginTop: 10 }}>
                      {isDev ? (
                        <button type="button" className="gmStoreLink" onClick={() => setShowFullSummary((v) => !v)}>
                          {showFullSummary ? "Show less" : "See full review →"}
                        </button>
                      ) : seeFullHref ? (
                        <a className="gmStoreLink" href={seeFullHref} target="_blank" rel="noreferrer">
                          See full review →
                        </a>
                      ) : null}
                    </div>
                  ) : null}
                </>
              ) : (
                "—"
              )}
            </div>
          </div>

          {/* Review CTA */}
          <div className="gmCard">
            <div className="gmCardTitle">
              {isReviewerOrAdmin ? "Post an Official Review" : "Want to submit a review?"}
            </div>

            <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
              {isReviewerOrAdmin && (
                <button className="gmPrimaryBtn" type="button" onClick={onReviewClick}>
                  {user?.role === "reviewer" ? "Write Review" : "Write Official Review"}
                </button>
              )}

              {isReviewerOrAdmin && (
                <button
                  className="gmPrimaryBtn"
                  type="button"
                  onClick={onCommentClick}
                  style={{ background: "rgba(124,58,237,.14)", color: "#5a2bd6" }}
                >
                  Comment
                </button>
              )}
            </div>

            {isReviewerOrAdmin && showComment ? (
              <div
                style={{
                  marginTop: 12,
                  padding: 12,
                  border: "1px solid rgba(0,0,0,.10)",
                  borderRadius: 14,
                  background: "rgba(0,0,0,.03)",
                }}
              >
                {!reviews.length ? (
                  <div className="gmMuted">No user reviews yet — nothing to comment on.</div>
                ) : (
                  <>
                    <div className="gmMuted" style={{ marginBottom: 8 }}>
                      Comment on an existing user review (this does not create a new review).
                    </div>

                    <select className="admin-select" value={commentTargetId} onChange={(e) => setCommentTargetId(e.target.value)}>
                      {reviews.map((r) => {
                        const uname = typeof r.userId === "object" ? r.userId?.username : "user";
                        const date = new Date(r.createdAt).toISOString().slice(0, 10);
                        return (
                          <option key={r._id} value={r._id}>
                            {date} • @{uname} • {r.score}
                          </option>
                        );
                      })}
                    </select>

                    <textarea
                      value={commentText}
                      onChange={(e) => setCommentText(e.target.value)}
                      placeholder="Write your comment (reviewer/admin)…"
                      style={{ width: "100%", minHeight: 90, marginTop: 10 }}
                    />

                    <div style={{ display: "flex", gap: 10, marginTop: 10, flexWrap: "wrap" }}>
                      <button className="gmPrimaryBtn" type="button" onClick={submitComment}>
                        Post comment
                      </button>
                      <button
                        className="gmPrimaryBtn"
                        type="button"
                        onClick={() => {
                          setShowComment(false);
                          setCommentText("");
                          setCommentErr("");
                          setCommentStatus("");
                        }}
                        style={{ background: "rgba(0,0,0,.06)", color: "#111" }}
                      >
                        Cancel
                      </button>
                    </div>

                    {commentErr ? <div className="gmErr" style={{ marginTop: 8 }}>{commentErr}</div> : null}
                    {commentStatus ? <div className="gmMuted" style={{ marginTop: 8 }}>{commentStatus}</div> : null}

                    {(() => {
                      const selected = reviews.find((r) => r._id === commentTargetId);
                      const comments = selected?.comments || [];
                      if (!comments.length) return null;
                      return (
                        <div style={{ marginTop: 12 }}>
                          <div className="gmCardTitle" style={{ marginBottom: 6 }}>
                            Staff comments
                          </div>
                          <div style={{ display: "grid", gap: 8, maxHeight: 160, overflow: "auto" }}>
                            {comments
                              .slice()
                              .reverse()
                              .map((c, idx) => (
                                <div
                                  key={`${c._id || idx}`}
                                  style={{
                                    padding: 10,
                                    borderRadius: 12,
                                    border: "1px solid rgba(0,0,0,.10)",
                                    background: "#fff",
                                    position: "relative",
                                  }}
                                >
                                  <div style={{ display: "flex", gap: 8, alignItems: "center", marginBottom: 6 }}>
                                    <span className="hc-pill" style={{
                                      background: c.role === "admin" ? "rgba(239,68,68,.12)" :
                                        c.role === "reviewer" ? "rgba(34,197,94,.12)" : "rgba(148,163,184,.12)",
                                      color: c.role === "admin" ? "#ef4444" :
                                        c.role === "reviewer" ? "#22c55e" : "#6b7280",
                                      border: `1px solid ${c.role === "admin" ? "rgba(239,68,68,.35)" :
                                        c.role === "reviewer" ? "rgba(34,197,94,.35)" : "rgba(148,163,184,.35)"}`
                                    }}>
                                      {c.role || "user"}
                                    </span>
                                    <span style={{ fontWeight: 800 }}>@{c.username || "staff"}</span>
                                    <span style={{ opacity: 0.6 }}>•</span>
                                    <span style={{ opacity: 0.75, fontSize: 12 }}>
                                      {new Date(c.createdAt || Date.now()).toLocaleDateString()}
                                    </span>

                                    {user?.role === "admin" && (
                                      <button
                                        onClick={() => handleDeleteComment(selected._id, c._id)}
                                        style={{
                                          marginLeft: "auto",
                                          background: "rgba(239,68,68,.15)",
                                          color: "#b91c1c",
                                          border: "1px solid rgba(239,68,68,.35)",
                                          borderRadius: 6,
                                          padding: "4px 8px",
                                          fontSize: 11,
                                          fontWeight: 800,
                                          cursor: "pointer",
                                        }}
                                        title="Delete comment"
                                      >
                                        Delete
                                      </button>
                                    )}
                                  </div>
                                  <div style={{ whiteSpace: "pre-wrap", wordBreak: "break-word" }}>{c.text}</div>
                                </div>
                              ))}
                          </div>
                        </div>
                      );
                    })()}
                  </>
                )}
              </div>
            ) : null}

            {!isReviewerOrAdmin && token && (
              <div style={{ marginTop: 10 }}>
                <div className="gmMuted" style={{ marginBottom: 6 }}>
                  Want to review games? Send a request to become a reviewer.
                </div>

                <textarea
                  value={requestMsg}
                  onChange={(e) => setRequestMsg(e.target.value)}
                  placeholder="Tell us why you should be a reviewer..."
                  style={{ width: "100%", minHeight: 80 }}
                />

                <button className="gmPrimaryBtn" type="button" onClick={submitReviewerRequest} style={{ marginTop: 8 }}>
                  Send a request
                </button>

                {requestStatus ? <div className="gmMuted" style={{ marginTop: 8 }}>{requestStatus}</div> : null}
              </div>
            )}

            {isReviewerOrAdmin && showForm && (
              <div style={{ marginTop: 10 }}>
                <ReviewForm initial={editing} onSubmit={handleSubmit} />
              </div>
            )}
          </div>

          {/* User Reviews */}
          <div className="gmReviewsBox">
            <div className="gmReviewsTitle">User Reviews</div>

            {reviewErr ? <div className="gmErr">{reviewErr}</div> : null}

            <div className="gmReviewsScroll">
              {!reviews.length ? (
                <div className="gmMuted">
                  {token ? (
                    "Be the first to review."
                  ) : (
                    <>
                      <button type="button" className="gmInlineLink" onClick={() => nav("/login")}>
                        Log in
                      </button>{" "}
                      to post a review.
                    </>
                  )}
                </div>
              ) : (
                <ReviewList
                  reviews={reviews}
                  canModify={canModify}
                  onEdit={(r) => {
                    setEditing(r);
                    setShowForm(true);
                  }}
                  onDelete={handleDelete}
                  user={user}
                  onAdminDelete={handleDeleteReviewAsAdmin}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
