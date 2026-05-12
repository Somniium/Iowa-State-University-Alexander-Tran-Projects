import alexPic from "../assets/images/Alex.jpg";
import christianPic from "../assets/images/Christian.jpg";

export default function About() {
  return (
    <div className="shell">
      {/* ===== HERO ===== */}
      <section className="authors-hero">
        <h1>About</h1>
        <p>HonestCritic — a COMS3190 final project from the students of MS_8.</p>
      </section>
      <div className="section-head" style={{ justifyContent: "center" }}>
        <div className="section-rule" style={{ maxWidth: 520 }} />
      </div>
      <div className="hc-rule" />
      {/* ===== PROJECT DESCRIPTION ===== */}
      <article className="glass-card about-card">
        <div className="about-head">
          <h2 className="about-title">What is HonestCritic?</h2>
          <p className="about-sub">
            A full-stack game review platform built for COM S 319.
          </p>
        </div>

        <p className="about-body">
          <strong>HonestCritic</strong> lets users browse games, view detailed pages, and
          post reviews after logging in. We started from a static prototype and refactored
          into a React SPA backed by a Node/Express API and MongoDB.
        </p>

        <div className="about-grid">
          <div className="about-kv">
            <div className="kv-label">Frontend</div>
            <div className="kv-value">React • Vite • React Router</div>
          </div>

          <div className="about-kv">
            <div className="kv-label">Backend</div>
            <div className="kv-value">Node.js • Express</div>
          </div>

          <div className="about-kv">
            <div className="kv-label">Database</div>
            <div className="kv-value">MongoDB • Mongoose</div>
          </div>

          <div className="about-kv">
            <div className="kv-label">Features</div>
            <div className="kv-value">Auth • Game browsing • Reviews CRUD • Search</div>
          </div>
        </div>

        <div className="about-badges">
          <span className="about-pill">SPA</span>
          <span className="about-pill">REST API</span>
          <span className="about-pill">MongoDB</span>
          <span className="about-pill">Auth</span>
        </div>
      </article>


      {/* ===== TEAM ===== */}
      <section>
        <h1 className="hc-hero-title" style={{ fontSize: 30, textAlign: "center", marginTop: 20 }}>Meet the Team!</h1>

        <div className="authors-grid">
          {/* ===== ALEX ===== */}
          <article className="glass-card">
            <div className="author-top">
              <div className="author-avatar">
                <img src={alexPic} alt="Alexander Tran" />
              </div>

              <div>
                <h3 className="author-name">Alexander Tran</h3>
                <div className="author-role">Student • Programmer</div>
              </div>
            </div>

            <div className="author-bio">
              <p>
                Software Engineering student at Iowa State University with a minor
                in Mathematics. Focused on frontend architecture, backend integration,
                and UI/UX refinement.
              </p>

              <p>
                Responsible for React refactor, API integration, authentication,
                review system, and overall site structure.
              </p>
            </div>

            <div className="author-meta">
              <div className="author-chips">
                <span className="chip">Frontend</span>
                <span className="chip">Backend</span>
                <span className="chip">UI/UX</span>
              </div>
            </div>
          </article>

          {/* ===== CHRISTIAN ===== */}
          <article className="glass-card">
            <div className="author-top">
              <div className="author-avatar">
                <img src={christianPic} alt="Christian Salazar" />
              </div>

              <div>
                <h3 className="author-name">Christian Salazar</h3>
                <div className="author-role">Student • Reviews Lead • Programmer</div>
              </div>
            </div>

            <div className="author-bio">
              <p>
                Worked on game review content, testing, and design direction.
                Contributed to feature planning and validation throughout development.
              </p>

              <p>
                Interested in game communities, review systems, and interactive media.
              </p>
            </div>

            <div className="author-meta">
              <div className="author-chips">
                <span className="chip">Reviews</span>
                <span className="chip">Design</span>
                <span className="chip">Testing</span>
              </div>
            </div>
          </article>
        </div>
      </section>
    </div>
  );
}
