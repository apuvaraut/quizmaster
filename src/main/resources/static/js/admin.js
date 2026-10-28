if (requireLogin(true)) loadQuizzes();

async function loadQuizzes() {
  try {
    const list = await api("/api/quizzes");
    document.getElementById("qQuiz").innerHTML = list.map(q => `<option value="${q.id}">${esc(q.title)}</option>`).join("");
    document.getElementById("quizTable").innerHTML = list.length
      ? `<table>` + list.map(q => `<tr><td>${esc(q.title)}</td><td style="text-align:right"><button class="btn small danger" onclick="deleteQuiz(${q.id})">Delete</button></td></tr>`).join("") + `</table>`
      : "<p class='muted'>No quizzes yet.</p>";
  } catch (e) { toast(e.message, false); }
}

async function generate() {
  const btn = document.getElementById("genBtn");
  btn.disabled = true;
  btn.textContent = "Generating...";
  try {
    const d = await api("/api/admin/quizzes/generate", "POST",
      { topic: v("gTopic"), count: parseInt(v("gCount")) || 5, difficulty: v("gDiff") });
    toast(d.message);
    loadQuizzes();
  } catch (e) { toast(e.message, false); }
  btn.disabled = false;
  btn.textContent = "Generate quiz";
}

async function createQuiz() {
  try {
    await api("/api/admin/quizzes", "POST", { title: v("cTitle"), topic: v("cTopic"), description: v("cDesc") });
    toast("Quiz created");
    ["cTitle", "cTopic", "cDesc"].forEach(id => (document.getElementById(id).value = ""));
    loadQuizzes();
  } catch (e) { toast(e.message, false); }
}

async function addQuestion() {
  try {
    await api(`/api/admin/quizzes/${v("qQuiz")}/questions`, "POST", {
      text: v("qText"), optionA: v("qA"), optionB: v("qB"), optionC: v("qC"), optionD: v("qD"),
      correctOption: v("qCorrect"), explanation: v("qExp")
    });
    toast("Question added");
    ["qText", "qA", "qB", "qC", "qD", "qExp"].forEach(id => (document.getElementById(id).value = ""));
  } catch (e) { toast(e.message, false); }
}

async function deleteQuiz(id) {
  if (!confirm("Delete this quiz and all its results?")) return;
  try {
    await api(`/api/admin/quizzes/${id}`, "DELETE");
    toast("Quiz deleted");
    loadQuizzes();
  } catch (e) { toast(e.message, false); }
}
