import { createContext, useContext, useEffect, useState } from "react";
import { api } from "../api/client";

const AuthCtx = createContext(null);

export function AuthProvider({ children }) {
  const [token, setToken] = useState(localStorage.getItem("token") || null);
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  });

  useEffect(() => {
    if (!token) {
      setUser(null);
      localStorage.removeItem("user");
    }
  }, [token]);

  async function login(emailOrUsername, password) {
    const data = await api("/users/login", {
      method: "POST",
      body: { emailOrUsername, password }
    });

    localStorage.setItem("token", data.token);
    localStorage.setItem("user", JSON.stringify(data.user));

    setToken(data.token);
    setUser(data.user);

    return data.user;
  }

  async function signup(email, username, password) {
    return await api("/users/register", {
      method: "POST",
      body: { email, username, password }
    });
  }

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setToken(null);
    setUser(null);
  }

  return (
    <AuthCtx.Provider value={{ user, token, login, signup, logout }}>
      {children}
    </AuthCtx.Provider>
  );
}

export function useAuth() {
  return useContext(AuthCtx);
}
