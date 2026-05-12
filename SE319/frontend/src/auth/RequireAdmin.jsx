import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "./AuthContext";

export default function RequireAdmin({ children }) {
  const { user } = useAuth();
  const loc = useLocation();

  if (!user) {
    return <Navigate to="/login" replace state={{ from: loc.pathname }} />;
  }

  if (user.role !== "admin" && user.role !== "reviewer") {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
}
