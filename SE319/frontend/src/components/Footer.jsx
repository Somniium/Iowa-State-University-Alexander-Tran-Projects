import { Link } from "react-router-dom";
import glassesLogo from "../assets/images/glasseslogo.png";

export default function Footer() {
  return (
    <footer className="site-footer">
      <div className="footer-inner">
        <div className="footer-left">
          <img
            src={glassesLogo}
            alt="HonestCritic logo"
            className="footer-logo"
          />
          <span className="footer-text">
            © {new Date().getFullYear()} Alexander Tran & Christian Salazar, Iowa State University.
          </span>
        </div>

        <div className="footer-right">
          {/* Instagram */}
          <a
            href="https://instagram.com"
            target="_blank"
            rel="noreferrer"
            aria-label="Instagram"
            className="footer-icon"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M7 2C4.2 2 2 4.2 2 7v10c0 2.8 2.2 5 5 5h10c2.8 0 5-2.2 5-5V7c0-2.8-2.2-5-5-5H7zm10 2a3 3 0 013 3v10a3 3 0 01-3 3H7a3 3 0 01-3-3V7a3 3 0 013-3h10zm-5 3.2A4.8 4.8 0 107.2 12 4.8 4.8 0 0012 7.2zm0 7.8a3 3 0 113-3 3 3 0 01-3 3zm4.9-8.9a1.1 1.1 0 11-1.1-1.1 1.1 1.1 0 011.1 1.1z" />
            </svg>
          </a>

          {/* Facebook */}
          <a
            href="https://facebook.com"
            target="_blank"
            rel="noreferrer"
            aria-label="Facebook"
            className="footer-icon"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M22 12a10 10 0 10-11.5 9.9v-7h-2v-2.9h2V9.5c0-2 1.2-3.1 3-3.1.9 0 1.8.2 1.8.2v2h-1c-1 0-1.3.6-1.3 1.2v1.6h2.3l-.4 2.9h-1.9v7A10 10 0 0022 12z" />
            </svg>
          </a>

          {/* GitHub (optional but nice) */}
          <a
            href="https://github.com"
            target="_blank"
            rel="noreferrer"
            aria-label="GitHub"
            className="footer-icon"
          >
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path d="M12 2a10 10 0 00-3.2 19.5c.5.1.7-.2.7-.5v-1.8c-3 .7-3.6-1.4-3.6-1.4-.5-1.2-1.2-1.5-1.2-1.5-1-.7.1-.7.1-.7 1.1.1 1.7 1.2 1.7 1.2 1 .1 1.6-.7 2-.9.1-.7.4-1.1.7-1.4-2.4-.3-4.9-1.2-4.9-5.2 0-1.1.4-2 1.1-2.7-.1-.3-.5-1.4.1-2.8 0 0 .9-.3 2.9 1.1a10 10 0 015.3 0c2-1.4 2.9-1.1 2.9-1.1.6 1.4.2 2.5.1 2.8.7.7 1.1 1.6 1.1 2.7 0 4-2.5 4.9-4.9 5.2.4.4.7 1 .7 2v3c0 .3.2.6.7.5A10 10 0 0012 2z" />
            </svg>
          </a>
        </div>
      </div>
    </footer>
  );
}

