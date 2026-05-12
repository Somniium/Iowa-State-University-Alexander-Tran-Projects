const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:5000/api";



// RAWG (via our backend proxy)
export async function rawgSearchGames({ search = "", page = 1, pageSize = 20 } = {}) {
  const qs = new URLSearchParams({
    search,
    page: String(page),
    page_size: String(pageSize),
  });
  const res = await fetch(`/api/rawg/games?${qs.toString()}`, { credentials: "include" });
  if (!res.ok) throw new Error("RAWG search failed");
  return res.json(); // { games, next, previous, count }
}

export async function rawgGetGame(rawgId) {
  const res = await fetch(`/api/rawg/games/${rawgId}`, { credentials: "include" });
  if (!res.ok) throw new Error("RAWG game fetch failed");
  return res.json(); // mapped game object
}


export async function api(path, options = {}) {
  const url = path.startsWith("http")
    ? path
    : `${API_BASE}${path.startsWith("/") ? "" : "/"}${path}`;

  const headers = { ...(options.headers || {}) };

  let body = options.body;
  const isPlainObject =
    body && typeof body === "object" && !(body instanceof FormData) && !(body instanceof Blob);

  if (isPlainObject) {
    headers["Content-Type"] = headers["Content-Type"] || "application/json";
    body = JSON.stringify(body);
  }

  const token = localStorage.getItem("token");
  if (token && !headers.Authorization) headers.Authorization = `Bearer ${token}`; // :contentReference[oaicite:2]{index=2}

  const res = await fetch(url, { ...options, headers, body });

  let data = null;
  const ct = res.headers.get("content-type") || "";
  if (ct.includes("application/json")) data = await res.json();
  else {
    const text = await res.text();
    data = text ? { message: text } : null;
  }

  // if token expired/invalid, clear it
  if (res.status === 401) {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    // optional: hard redirect (works anywhere)
    window.location.href = "/login";
    return; // stop here
  }

  if (!res.ok) {
    const msg = (data && (data.error || data.message)) || `Request failed (${res.status})`;
    throw new Error(msg);
  }

  return data;
}
