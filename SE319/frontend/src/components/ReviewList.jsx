import { useMemo, useState } from "react";

function scoreClass(score) {
  const n = Number(score);
  if (!Number.isFinite(n)) return "b-gray";
  if (n >= 90) return "b-green";
  if (n >= 75) return "b-yellow";
  return "b-red";
}

function fmtDate(d) {
  try {
    const dt = new Date(d);
    if (Number.isNaN(dt.getTime())) return "";
    return dt.toISOString().slice(0, 10);
  } catch {
    return "";
  }
}

function getUserName(r) {
  if (r?.userId && typeof r.userId === "object") {
    return r.userId.username || r.userId.email || "user";
  }
  return r?.username || "user";
}

export default function ReviewList({ reviews, canModify, onEdit, onDelete, user, onAdminDelete }) {
  const [open, setOpen] = useState(null);

  const normalized = useMemo(() => {
    return (reviews || []).map((r) => ({
      ...r,
      _who: getUserName(r),
      _date: fmtDate(r.createdAt || r.updatedAt),
      _score: Number.isFinite(Number(r.score)) ? Number(r.score) : r.score,
      _text: String(r.text || ""),
    }));
  }, [reviews]);

  return (
    <div className="rlList">
      {normalized.map((r) => (
        <div key={r._id} className="rlItem">
          <div className="rlTop">
            <div className="rlMeta">
              <span className="rlDate">{r._date}</span>
              <span className="rlDot">•</span>
              <span className="rlUser">@{r._who}</span>
              <span className="rlDot">•</span>
              <span className={`score-pill ${scoreClass(r._score)}`}>{r._score}</span>

              <button
                type="button"
                className="rlSeeBtn"
                onClick={() => setOpen(r)}
                title="Open full review"
              >
                See full review
              </button>
            </div>

            {canModify?.(r) || user?.role === "admin" ? (
              <div className="rlActions">
                {canModify?.(r) && (
                  <>
                    <button type="button" className="btn-edit" onClick={() => onEdit?.(r)}>
                      Edit
                    </button>
                    <button type="button" className="btn-delete" onClick={() => onDelete?.(r)}>
                      Delete
                    </button>
                  </>
                )}
                {user?.role === "admin" && (
                  <button
                    type="button"
                    className="btn-delete"
                    onClick={(e) => {
                      e.stopPropagation(); // Prevent event bubbling
                      onAdminDelete?.(r._id); // Pass just the ID string
                    }}
                    style={{ marginLeft: canModify?.(r) ? 8 : 0 }}
                  >
                    Admin Delete
                  </button>
                )}
              </div>
            ) : null}
          </div>

          {/* preview (clamped) */}
          <div className="rlPreview">{r._text}</div>
        </div>
      ))}

      {/* Popout modal */}
      {open ? (
        <div className="modalOverlay" onMouseDown={() => setOpen(null)}>
          <div className="rlModal" onMouseDown={(e) => e.stopPropagation()}>
            <button className="gmClose" onClick={() => setOpen(null)} aria-label="Close">
              ×
            </button>

            <div className="rlModalHead">
              <div className="rlModalTitle">User Review</div>
              <div className="rlModalMeta">
                <span>{open._date}</span>
                <span className="rlDot">•</span>
                <span>@{open._who}</span>
                <span className="rlDot">•</span>
                <span className={`score-pill ${scoreClass(open._score)}`}>{open._score}</span>
              </div>
            </div>

            <div className="rlModalBody">
              {open._text}
            </div>

            <div className="rlModalFoot">
              <button className="hc-pill-btn hc-edit" type="button" onClick={() => setOpen(null)}>
                Close
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}