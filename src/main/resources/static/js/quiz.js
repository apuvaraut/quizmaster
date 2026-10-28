const quizId = new URLSearchParams(location.search).get("id");
const box = document.getElementById("box");
let questions = [];
let answers = {};      // { questionId: "A" }
let current = 0;
let lastReview = [];

if (requireLogin()) start();

async function start() {
  try {
    questions = await api(`/api/quizzes/${quizId}/questions`);
    if (!questions.length) {
      box.innerHTML = "<p>This quiz has no questions yet.</p><a class='btn' href='dashboard.html'>Back to home</a>";
      return;
    }
    showQuestion();
  } catch (e) { toast(e.message, false); }
}

function showQuestion() {
  const q = questions[current];
  const letters = ["A", "B", "C", "D"];
  const options = [q.optionA, q.optionB, q.optionC, q.optionD];
  const isLast = current === questions.length - 1;

  box.innerHTML = `
    <div class="progress"><div style="width:${((current + 1) / questions.length) * 100}%"></div></div>
    <p class="q-count">Question ${current + 1} of ${questions.length}</p>
    <h2 class="q-text">${esc(q.text)}</h2>
    ${options.map((text, i) => `
      <button class="option ${answers[q.id] === letters[i] ? "selected" : ""}" onclick="choose('${letters[i]}')">
        <span class="key">${letters[i]}</span><span>${esc(text)}</span>
      </button>`).join("")}
    <div class="nav-btns">
      <button class="btn ghost" onclick="go(-1)" ${current === 0 ? "disabled" : ""}>Previous</button>
      ${isLast ? `<button class="btn" onclick="submitQuiz()">Submit quiz</button>`
               : `<button class="btn" onclick="go(1)">Next</button>`}
    </div>`;
}

function choose(letter) { answers[questions[current].id] = letter; showQuestion(); }
function go(step) { current += step; showQuestion(); }

async function submitQuiz() {
  const unanswered = questions.length - Object.keys(answers).length;
  if (unanswered > 0 && !confirm(`${unanswered} question(s) are unanswered. Submit anyway?`)) return;
  try {
    const result = await api(`/api/quizzes/${quizId}/submit`, "POST", { answers });
    showResult(result);
  } catch (e) { toast(e.message, false); }
}

function showResult(r) {
  lastReview = r.review;
  const pct = r.total ? Math.round((r.score * 100) / r.total) : 0;
  const message = pct >= 70 ? "Great work." : pct >= 40 ? "Good effort. Review the mistakes below." : "Keep practicing. The explanations below will help.";

  box.innerHTML = `
    <div class="result">
      <div class="score-ring" style="--p:${pct}"><span>${pct}%</span></div>
      <h2>You scored ${r.score} out of ${r.total}</h2>
      <p class="muted">${message}</p>
    </div>
    ${r.review.map((x, i) => `
      <div class="review-item ${x.correct ? "right" : "wrong"}">
        <b>${i + 1}. ${esc(x.question)}</b>
        <p>Your answer: <span class="${x.correct ? "good" : "bad"}">${esc(x.yourAnswer)}</span></p>
        ${x.correct ? "" : `<p>Correct answer: <span class="good">${esc(x.correctAnswer)}</span></p>`}
        ${x.explanation ? `<p class="muted">${esc(x.explanation)}</p>` : ""}
        ${x.correct ? "" : `<button class="btn small" onclick="explain(${i})">Explain with AI</button>
                            <div id="ai${i}" class="ai-box hidden"></div>`}
      </div>`).join("")}
    <a class="btn" href="dashboard.html">Back to home</a>`;
}

async function explain(i) {
  const x = lastReview[i];
  const out = document.getElementById("ai" + i);
  out.classList.remove("hidden");
  out.textContent = "Thinking...";
  try {
    const d = await api("/api/ai/explain", "POST", { question: x.question, yourAnswer: x.yourAnswer, correctAnswer: x.correctAnswer });
    out.textContent = d.explanation;
  } catch (e) { out.textContent = e.message; }
}
