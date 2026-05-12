import { useState } from "react";

export default function ReviewForm({ initial, onSubmit }) {
  const [score, setScore] = useState(initial?.score ?? 80);
  const [text, setText] = useState(initial?.text ?? "");

  function submit(e) {
    e.preventDefault();
    onSubmit({ score: Number(score), text });
  }

  return (
    <form className="hc-review-form" onSubmit={submit}>
      <label className="hc-label">
        Score
        <input
          type="number"
          min="0"
          max="100"
          value={score}
          onChange={e => setScore(e.target.value)}
        />
      </label>

      <label className="hc-label">
        Review
        <textarea
          value={text}
          onChange={e => setText(e.target.value)}
          placeholder="Write your review..."
        />
      </label>

      <div className="hc-form-actions">
        <button
          type="submit"
          className={`hc-pill-btn ${initial ? "hc-edit" : "hc-post"}`}
        >
          {initial ? "Update Review" : "Post Review"}
        </button>
      </div>
    </form>
  );
}
