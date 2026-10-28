if (getUser()) location.href = "dashboard.html";

function switchTab(tab) {
  const isLogin = tab === "login";
  document.getElementById("loginForm").classList.toggle("hidden", !isLogin);
  document.getElementById("registerForm").classList.toggle("hidden", isLogin);
  document.getElementById("tabLogin").classList.toggle("active", isLogin);
  document.getElementById("tabRegister").classList.toggle("active", !isLogin);
}

async function login() {
  try {
    const data = await api("/api/auth/login", "POST", { email: v("lEmail"), password: document.getElementById("lPass").value });
    saveUser(data);
    location.href = "dashboard.html";
  } catch (e) { toast(e.message, false); }
}

async function register() {
  try {
    const data = await api("/api/auth/register", "POST", { name: v("rName"), email: v("rEmail"), password: document.getElementById("rPass").value });
    saveUser(data);
    location.href = "dashboard.html";
  } catch (e) { toast(e.message, false); }
}

// Press Enter to submit
document.addEventListener("keydown", (e) => {
  if (e.key !== "Enter") return;
  if (document.getElementById("loginForm").classList.contains("hidden")) register(); else login();
});
