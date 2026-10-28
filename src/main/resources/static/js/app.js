// Shared helpers used by every page

function getUser() { return JSON.parse(localStorage.getItem("user") || "null"); }
function saveUser(data) { localStorage.setItem("user", JSON.stringify(data)); }
function logout() { localStorage.removeItem("user"); location.href = "index.html"; }
function v(id) { return document.getElementById(id).value.trim(); }

// Prevents XSS: converts <, >, & etc. before putting text into the page
function esc(text) {
  const d = document.createElement("div");
  d.textContent = text == null ? "" : text;
  return d.innerHTML;
}

function toast(message, ok = true) {
  const t = document.getElementById("toast");
  t.textContent = message;
  t.className = "toast show " + (ok ? "ok" : "bad");
  setTimeout(() => (t.className = "toast"), 3200);
}

// One function for every backend call. It adds the JWT token automatically.
async function api(url, method = "GET", body) {
  const user = getUser();
  const headers = { "Content-Type": "application/json" };
  if (user) headers["Authorization"] = "Bearer " + user.token;

  const res = await fetch(url, { method, headers, body: body ? JSON.stringify(body) : undefined });

  if (res.status === 401 && user) { logout(); return; }   // token expired
  if (res.status === 403) throw new Error("You do not have permission to do this");

  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch (e) { /* not JSON */ }
  if (!res.ok) throw new Error((data && data.message) || "Request failed");
  return data;
}

function renderNav() {
  const u = getUser();
  document.getElementById("nav").innerHTML =
    `<div class="brand">QuizMaster</div>
     <div class="nav-right">
       <a href="dashboard.html">Home</a>
       ${u.role === "ADMIN" ? '<a href="admin.html">Admin</a>' : ""}
       <span class="pill">${esc(u.name)}</span>
       <button class="btn small ghost" onclick="logout()">Logout</button>
     </div>`;
}

// Call at the top of every protected page
function requireLogin(adminOnly = false) {
  const u = getUser();
  if (!u) { location.href = "index.html"; return false; }
  if (adminOnly && u.role !== "ADMIN") { location.href = "dashboard.html"; return false; }
  renderNav();
  return true;
}
