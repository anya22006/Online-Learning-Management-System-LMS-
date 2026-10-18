const QUIZZES_API_URL = "/api/student/quizzes";
const QUIZ_ATTEMPTS_API_URL = "/api/student/quiz-attempts";
let CURRENT_STUDENT = null;

async function getAuthStudent() {
    if (CURRENT_STUDENT) return CURRENT_STUDENT;
    try {
        const res = await fetch("/api/student/current");
        if (res.ok) {
            const data = await res.json();
            if (data && data.authenticated) {
                CURRENT_STUDENT = data;
                sessionStorage.setItem("currentStudent", JSON.stringify(data));
                return data;
            }
        }
    } catch (e) {}
    const cached = sessionStorage.getItem("currentStudent");
    if (cached) {
        try {
            CURRENT_STUDENT = JSON.parse(cached);
            return CURRENT_STUDENT;
        } catch (e) {}
    }
    return { id: 1, name: "Student" };
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function formatAttemptDate(value) {
    if (!value) {
        return "Date unavailable";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return "Date unavailable";
    }

    return new Intl.DateTimeFormat(undefined, {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(date);
}

function renderQuiz(quiz, myAttempts) {
    const courseName = quiz.courseTitle
        ? escapeHtml(quiz.courseTitle)
        : `Course ${escapeHtml(quiz.courseId ?? "unavailable")}`;
    const timeLimit = quiz.timeLimitMinutes ? `${quiz.timeLimitMinutes} Mins` : '30 Mins';

    const quizAttempts = myAttempts ? myAttempts.filter(a => Number(a.quizId) === Number(quiz.quizId)) : [];
    let attemptBadge = "";
    if (quizAttempts.length > 0) {
        const bestScore = quizAttempts[0].score || quizAttempts[0].percentage || 0;
        attemptBadge = `<span class="badge" style="background: rgba(16, 185, 129, 0.2); color: #10b981; padding: 4px 10px; border-radius: 6px; font-size: 0.8rem; font-weight: 600;"><i class="fa-solid fa-circle-check"></i> Score: ${bestScore}%</span>`;
    }

    return `
        <article class="quiz-card" data-quiz-id="${escapeHtml(quiz.quizId)}">
            <div class="quiz-card-header">
                <div>
                    <span class="course-name"><i class="fa-solid fa-graduation-cap"></i> ${courseName}</span>
                    <h2>${escapeHtml(quiz.title || "Untitled quiz")}</h2>
                </div>
                ${attemptBadge}
            </div>
            <div class="quiz-description">${escapeHtml(quiz.description || "No description provided.")}</div>
            <div class="quiz-meta">
                <span><i class="fa-solid fa-clock" style="color: #3b82f6;"></i> <strong>Time Limit:</strong> ${escapeHtml(timeLimit)}</span>
                <span><i class="fa-solid fa-circle-question" style="color: #10b981;"></i> <strong>Format:</strong> Multiple Choice</span>
            </div>
            <div>
                <button class="primary-button start-quiz" type="button" data-quiz-id="${escapeHtml(quiz.quizId)}">
                    <i class="fa-solid fa-play"></i> ${quizAttempts.length > 0 ? "Retake Quiz" : "Start Quiz"}
                </button>
            </div>
        </article>`;
}

function renderQuestion(question, questionNumber) {
    const options = [
        ["A", question.optionA],
        ["B", question.optionB],
        ["C", question.optionC],
        ["D", question.optionD]
    ].filter(([, value]) => value && String(value).trim() !== "");

    return `
        <fieldset class="question-card">
            <legend class="question-number">Question ${escapeHtml(questionNumber)}</legend>
            <p class="question-text">${escapeHtml(question.questionText)}</p>
            <div class="options-list">
                ${options.map(([letter, label]) => `
                    <label class="option-label">
                        <input
                            type="radio"
                            name="question_${escapeHtml(question.questionId)}"
                            value="${escapeHtml(letter)}"
                            required
                        >
                        <span><strong>${escapeHtml(letter)}.</strong> ${escapeHtml(label)}</span>
                    </label>
                `).join("")}
            </div>
        </fieldset>`;
}

async function readApiError(response, defaultMessage) {
    try {
        const problem = await response.json();
        return problem?.detail || problem?.message || `${defaultMessage} (HTTP ${response.status}).`;
    } catch {
        return `${defaultMessage} (HTTP ${response.status}).`;
    }
}

async function startQuiz(quizId) {
    const workspace = document.getElementById("quiz-workspace");
    workspace.hidden = false;
    workspace.innerHTML = `
        <section class="quiz-active-panel">
            <div class="loading-state">
                <i class="fa-solid fa-spinner fa-spin"></i> Loading quiz questions...
            </div>
        </section>`;

    try {
        const response = await fetch(`${QUIZZES_API_URL}/${quizId}/questions`);
        if (!response.ok) {
            throw new Error(await readApiError(response, "Questions could not be loaded"));
        }

        const questions = await response.json();
        if (!Array.isArray(questions) || questions.length === 0) {
            workspace.innerHTML = `
                <section class="quiz-active-panel">
                    <h2>No questions available</h2>
                    <p>This quiz does not have any questions prepared yet.</p>
                    <button class="secondary-button back-to-quizzes" type="button">Back to quizzes</button>
                </section>`;
            return;
        }

        workspace.innerHTML = `
            <section class="quiz-active-panel">
                <div class="quiz-flow-header">
                    <div>
                        <p class="eyebrow">ACTIVE QUIZ</p>
                        <h2>Assessment</h2>
                    </div>
                    <button class="secondary-button back-to-quizzes" type="button">Cancel</button>
                </div>
                <form id="quiz-attempt-form" data-quiz-id="${escapeHtml(quizId)}">
                    <div class="questions-container">
                        ${questions.map((question, index) => renderQuestion(question, index + 1)).join("")}
                    </div>
                    <div class="form-actions">
                        <button class="primary-button" type="submit">Submit Quiz Attempt</button>
                        <span class="quiz-message" role="status" aria-live="polite"></span>
                    </div>
                </form>
            </section>`;

        workspace.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
        console.error("Unable to load quiz questions:", error);
        workspace.innerHTML = `
            <section class="quiz-active-panel">
                <p class="state-message error">${escapeHtml(error.message || "Questions could not be loaded.")}</p>
                <button class="secondary-button back-to-quizzes" type="button">Back to quizzes</button>
            </section>`;
    }
}

async function submitQuiz(event) {
    event.preventDefault();

    const form = event.currentTarget;
    const submitButton = form.querySelector("button[type='submit']");
    const message = form.querySelector(".quiz-message");
    const formData = new FormData(form);
    const answers = {};

    for (const [key, value] of formData.entries()) {
        const questionId = key.replace("question_", "");
        answers[questionId] = value;
    }

    submitButton.disabled = true;
    message.textContent = "Grading attempt...";
    message.classList.remove("error", "success");

    try {
        const student = await getAuthStudent();
        const response = await fetch(QUIZ_ATTEMPTS_API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                quizId: Number(form.dataset.quizId),
                studentId: student.id,
                studentName: student.name,
                answers
            })
        });

        if (!response.ok) {
            throw new Error(await readApiError(response, "Quiz could not be submitted"));
        }

        const result = await response.json();
        showQuizResult(result);
    } catch (error) {
        console.error("Unable to submit quiz:", error);
        message.textContent = error.message || "Quiz could not be submitted.";
        message.classList.add("error");
        submitButton.disabled = false;
    }
}

function showQuizResult(result) {
    const workspace = document.getElementById("quiz-workspace");
    workspace.innerHTML = `
        <section class="result-panel" style="background: rgba(30, 41, 59, 0.7); border: 1px solid var(--border-color); border-radius: 16px; padding: 32px; text-align: center;">
            <p class="eyebrow" style="color: var(--accent-emerald); font-weight: 700; text-transform: uppercase;">QUIZ COMPLETE</p>
            <h2 style="font-size: 2rem; color: #fff; margin: 8px 0 16px;">Your result: ${escapeHtml(result.score)}%</h2>
            <p class="result-lead" style="color: var(--text-muted); margin-bottom: 24px;">Your score has been calculated and saved to your grade record.</p>
            <div class="result-summary" style="display: flex; justify-content: center; gap: 32px; margin-bottom: 24px;">
                <div class="result-item">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Correct answers</span>
                    <div style="font-size: 1.4rem; font-weight: 700; color: var(--accent-emerald);">${escapeHtml(result.correctAnswers)} / ${escapeHtml(result.totalQuestions)}</div>
                </div>
                <div class="result-item">
                    <span style="color: var(--text-muted); font-size: 0.9rem;">Points Earned</span>
                    <div style="font-size: 1.4rem; font-weight: 700; color: #3b82f6;">${escapeHtml(result.earnedPoints)} / ${escapeHtml(result.totalPoints)}</div>
                </div>
            </div>
            <p class="result-date" style="color: var(--text-muted); font-size: 0.85rem; margin-bottom: 20px;"><strong>Attempted:</strong> ${escapeHtml(formatAttemptDate(result.attemptedAt))}</p>
            <div class="quiz-actions">
                <button class="secondary-button back-to-quizzes btn btn-primary" type="button"><i class="fa-solid fa-arrow-left"></i> Back to quizzes</button>
            </div>
        </section>`;
}

function showQuizList() {
    const workspace = document.getElementById("quiz-workspace");
    if (workspace) {
        workspace.hidden = true;
        workspace.innerHTML = "";
    }
    loadQuizzes();
}

async function loadQuizzes() {
    const quizList = document.getElementById("quiz-list");
    if (!quizList) return;

    try {
        const student = await getAuthStudent();
        const [quizRes, attemptsRes] = await Promise.all([
            fetch(`${QUIZZES_API_URL}/${student.id}`),
            fetch(`${QUIZ_ATTEMPTS_API_URL}/${student.id}`)
        ]);

        if (!quizRes.ok) {
            throw new Error(await readApiError(quizRes, "Quizzes could not be loaded"));
        }

        const quizzes = await quizRes.json();
        const attempts = attemptsRes.ok ? await attemptsRes.json() : [];

        if (!Array.isArray(quizzes) || quizzes.length === 0) {
            quizList.innerHTML = `
                <div class="empty-message" style="padding: 32px; text-align: center; background: rgba(30, 41, 59, 0.4); border-radius: 12px; border: 1px dashed var(--border-color);">
                    <i class="fa-solid fa-brain" style="font-size: 2.5rem; color: var(--accent-indigo); margin-bottom: 12px;"></i>
                    <h3 style="font-size: 1.2rem; margin-bottom: 6px;">No quizzes available</h3>
                    <p style="color: var(--text-muted);">Quizzes prepared by your instructors will appear here.</p>
                </div>`;
            return;
        }

        quizList.innerHTML = quizzes.map(q => renderQuiz(q, attempts)).join("");
    } catch (error) {
        console.error("Unable to load quizzes:", error);
        quizList.innerHTML = `
            <p class="state-message error">${escapeHtml(error.message || "Quizzes could not be loaded.")}</p>`;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    loadQuizzes();

    document.addEventListener("click", (event) => {
        const startButton = event.target.closest(".start-quiz");
        if (startButton) {
            startQuiz(Number(startButton.dataset.quizId));
            return;
        }

        const backButton = event.target.closest(".back-to-quizzes");
        if (backButton) {
            showQuizList();
        }
    });

    document.addEventListener("submit", (event) => {
        if (event.target && event.target.id === "quiz-attempt-form") {
            submitQuiz(event);
        }
    });
});
