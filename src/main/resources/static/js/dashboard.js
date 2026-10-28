if (requireLogin()) loadAll();

async function loadAll() {
  try {
    const quizzes = await api("/api/quizzes");
    document.getElementById("quizList").innerHTML = quizzes.length
      ? quizzes.map(q => `
        <div class="card quiz-card">
          <span class="tag">${esc(q.topic)}</span>
          <h3>${esc(q.title)}</h3>
          <p>${esc(q.description || "")}</p>
          <a class="btn" href="quiz.html?id=${q.id}">Start quiz</a>
        </div>`).join("")
      : "<p class='muted'>No quizzes yet. Ask an admin to add one.</p>";

    const history = await api("/api/results/me");
    document.getElementById("history").innerHTML = history.length
      ? `<table><tr><th>Quiz</th><th>Score</th><th>Date</th></tr>` +
        history.slice(0, 8).map(h => `<tr><td>${esc(h.quizTitle)}</td><td>${h.score}/${h.total}</td><td>${new Date(h.takenAt).toLocaleDateString()}</td></tr>`).join("") + `</table>`
      : "<p class='muted'>You have not taken a quiz yet. Pick one above to start.</p>";

    const board = await api("/api/leaderboard");
    document.getElementById("board").innerHTML = board.length
      ? `<table>` + board.map((b, i) => `<tr><td class="rank">${i + 1}</td><td>${esc(b.name)}</td><td>${b.totalScore} pts</td></tr>`).join("") + `</table>`
      : "<p class='muted'>No scores yet. Be the first on the board.</p>";
  } catch (e) { toast(e.message, false); }
}
