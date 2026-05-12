import { useEffect } from "react";
import { Routes, Route, useLocation } from "react-router-dom";

import Navbar from "./components/Navbar";
import Footer from "./components/Footer";
import Unauthorized from "./pages/Unauthorized";
import Home from "./pages/Home";
import Checkout from "./pages/Checkout";
import Confirmation from "./pages/Confirmation";
import DeveloperReviews from "./pages/DeveloperReviews";
import GameGallery from "./pages/Gallery";
import Upcoming from "./pages/Upcoming";
import About from "./pages/About";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Dashboard from "./pages/Dashboard";
import Settings from "./pages/Settings";
import RequireAuth from "./auth/RequireAuth";
import AdminPanel from "./pages/AdminPanel";
import RequireAdmin from "./auth/RequireAdmin";
import ChatbotButton from "./components/ChatbotButton";
import ChatbotPage from "./pages/ChatbotPage";

const USER_REVIEWS_KEY = "hc_user_reviews_v1";
const USER_REVIEWS_EVENT = "hc:userreviews";

export function loadUserReviews() {
  try { return JSON.parse(localStorage.getItem(USER_REVIEWS_KEY) || "[]"); }
  catch { return []; }
}

export function saveUserReviews(list) {
  localStorage.setItem(USER_REVIEWS_KEY, JSON.stringify(list));
  window.dispatchEvent(new CustomEvent(USER_REVIEWS_EVENT));
}

export function addUserReview(review) {
  const list = loadUserReviews();
  list.unshift(review); // newest first
  saveUserReviews(list);
  return list;
}

export function approveUserReview(reviewId) {
  const list = loadUserReviews().map(r =>
    r.id === reviewId ? { ...r, approved: true, approvedAt: Date.now() } : r
  );
  saveUserReviews(list);
  return list;
}


export default function App() {
  const { pathname } = useLocation();
  const isAuth = pathname === "/login" || pathname === "/signup";

  useEffect(() => {
    document.body.classList.toggle("auth-bg", isAuth);
    return () => document.body.classList.remove("auth-bg");
  }, [isAuth]);

  return (
    <div className="app-shell">
      <Navbar />

      <main className={isAuth ? "app-content auth" : "shell app-content"}>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/reviews" element={<DeveloperReviews />} />
          <Route path="/gallery" element={<GameGallery />} />
          <Route path="/upcoming" element={<Upcoming />} />
          <Route path="/about" element={<About />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />

          <Route path="/unauthorized" element={<Unauthorized />} />

          <Route
            path="/dashboard"
            element={
              <RequireAdmin>
                <Dashboard />
              </RequireAdmin>
            }
          />

          <Route
            path="/settings"
            element={
              <RequireAuth>
                <Settings />
              </RequireAuth>
            }
          />

          <Route
            path="/checkout"
            element={
              <RequireAdmin>
                <Checkout />
              </RequireAdmin>
            }
          />

          <Route
            path="/confirmation"
            element={
              <RequireAdmin>
                <Confirmation />
              </RequireAdmin>
            }
          />

          <Route
            path="/admin"
            element={
              <RequireAdmin>
                <AdminPanel />
              </RequireAdmin>
            }
          />

          <Route path="/chatbot" element={<ChatbotPage />} />

          <Route
            path="*"
            element={<div style={{ padding: 24 }}>Not found</div>}
          />
        </Routes>

      </main>
      {/* Chat Bot Component */}
      <ChatbotButton />

      <Footer />
    </div>
  );
}
