const QUIZZES_API_URL = "http://localhost:8080/api/student/quizzes";
const QUIZ_ATTEMPTS_API_URL = "http://localhost:8080/api/student/quiz-attempts";
const STUDENT_ID = 1;

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

function renderQuiz(quiz) {
    const courseName = quiz.courseTitle
        ? escapeHtml(quiz.courseTitle)
        : `Course ${escapeHtml(quiz.courseId ?? "unavailable")}`;
    const timeLimit = quiz.timeLimitMinutes ? `${quiz.timeLimitMinutes} Mins` : '30 Mins';

    return `
        <article class="quiz-card" data-quiz-id="${escapeHtml(quiz.quizId)}">
            <div class="quiz-card-header">
                <div>
                    <span class="course-name"><i class="fa-solid fa-graduation-cap"></i> ${courseName}</span>
                    <h2>${escapeHtml(quiz.title || "Untitled quiz")}</h2>
                </div>
            </div>
            <div class="quiz-description">${escapeHtml(quiz.description || "No description provided.")}</div>
            <div class="quiz-meta">
                <span><i class="fa-solid fa-clock" style="color: #3b82f6;"></i> <strong>Time Limit:</strong> ${escapeHtml(timeLimit)}</span>
                <span><i class="fa-solid fa-circle-question" style="color: #10b981;"></i> <strong>Format:</strong> Multiple Choice</span>
            </div>
            <div>
                <button class="primary-button start-quiz" type="button" data-quiz-id="${escapeHtml(quiz.quizId)}">
                    <i class="fa-solid fa-play"></i> Start Quiz
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
    ].filter(([, value]) => value !== null && value !== undefined && value !== "");

    const marksLabel = question.marks ? ` (${question.marks} pts)` : "";

    return `
        <fieldset class="question-card">
            <legend class="visually-hidden">Question ${questionNumber}</legend>
            <h3>${questionNumber}. ${escapeHtml(question.questionText)}<span style="font-size: 0.85rem; color: #3b82f6; margin-left: 8px;">${marksLabel}</span></h3>
            <div class="answer-options">
                ${options.map(([letter, text]) => `
                    <label class="answer-option">
                        <input type="radio" name="question-${escapeHtml(question.questionId || question.id)}" value="${letter}">
                        <span><strong>${letter}.</strong> ${escapeHtml(text)}</span>
                    </label>
                `).join("")}
            </div>
        </fieldset>`;
}

async function readApiError(response, fallbackMessage) {
    try {
        const error = await response.json();
        return error.detail || error.message || error.title
            || `${fallbackMessage} (HTTP ${response.status}).`;
    } catch {
        return `${fallbackMessage} (HTTP ${response.status}).`;
    }
}

async function startQuiz(quizId) {
    const quizList = document.getElementById("quiz-list");
    const workspace = document.getElementById("quiz-workspace");
    const startButton = quizList.querySelector(
        `.start-quiz[data-quiz-id="${CSS.escape(String(quizId))}"]`
    );

    if (!startButton) {
        return;
    }

    startButton.disabled = true;
    startButton.textContent = "Loading quiz…";
    workspace.hidden = false;
    workspace.innerHTML = `<p class="state-message">Loading questions…</p>`;

    try {
        const response = await fetch(
            `${QUIZZES_API_URL}/${encodeURIComponent(quizId)}/questions`
        );
        if (!response.ok) {
            throw new Error(await readApiError(response, "Questions could not be loaded"));
        }

        const questions = await response.json();
        if (!Array.isArray(questions)) {
            throw new Error("The questions API returned an unexpected response.");
        }

        if (questions.length === 0) {
            workspace.innerHTML = `
                <section class="quiz-panel">
                    <p class="state-message">This quiz does not have any questions yet.</p>
                    <div class="quiz-actions">
                        <button class="secondary-button back-to-quizzes" type="button">Back to quizzes</button>
                    </div>
                </section>`;
            return;
        }

        const quizCard = startButton.closest(".quiz-card");
        const quizTitle = quizCard.querySelector("h2").textContent;
        const courseTitle = quizCard.querySelector(".course-name").textContent;

        workspace.innerHTML = `
            <section class="quiz-panel">
                <p class="course-name">${escapeHtml(courseTitle)}</p>
                <h2>${escapeHtml(quizTitle)}</h2>
                <form class="quiz-form" data-quiz-id="${escapeHtml(quizId)}">
                    <div class="question-list">
                        ${questions.map((question, index) => renderQuestion(question, index + 1)).join("")}
                    </div>
                    <p class="quiz-message" role="status" aria-live="polite"></p>
                    <div class="quiz-actions">
                        <button class="primary-button submit-quiz" type="submit">Submit quiz</button>
                        <button class="secondary-button back-to-quizzes" type="button">Back to quizzes</button>
                    </div>
                </form>
            </section>`;
        workspace.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
        console.error("Unable to load quiz questions:", error);
        workspace.innerHTML = `
            <p class="state-message error">
                ${escapeHtml(error.message || "Questions could not be loaded.")}
            </p>
            <div class="quiz-actions">
                <button class="secondary-button back-to-quizzes" type="button">Back to quizzes</button>
            </div>`;
    } finally {
        startButton.disabled = false;
        startButton.textContent = "Start quiz";
    }
}

async function submitQuiz(event) {
    const form = event.target.closest(".quiz-form");
    if (!form) {
        return;
    }

    event.preventDefault();
    const answers = {};
    const questionIds = new Set();

    form.querySelectorAll('input[type="radio"]').forEach(input => {
        questionIds.add(input.name.slice("question-".length));
    });

    questionIds.forEach(questionId => {
        const selected = form.querySelector(
            `input[name="question-${CSS.escape(questionId)}"]:checked`
        );
        if (selected) {
            answers[questionId] = selected.value;
        }
    });

    const message = form.querySelector(".quiz-message");
    const submitButton = form.querySelector(".submit-quiz");
    submitButton.disabled = true;
    message.textContent = "Submitting answers…";
    message.classList.remove("error");

    try {
        const response = await fetch(QUIZ_ATTEMPTS_API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                quizId: Number(form.dataset.quizId),
                studentId: STUDENT_ID,
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
        <section class="result-panel">
            <p class="eyebrow">QUIZ COMPLETE</p>
            <h2>Your result</h2>
            <p class="result-lead">Your score has been calculated and saved.</p>
            <div class="result-summary">
                <div class="result-item">
                    <span>Score</span>
                    <strong>${escapeHtml(result.score)}%</strong>
                </div>
                <div class="result-item">
                    <span>Correct answers</span>
                    <strong>${escapeHtml(result.correctAnswers)} / ${escapeHtml(result.totalQuestions)}</strong>
                </div>
                <div class="result-item">
                    <span>Total questions</span>
                    <strong>${escapeHtml(result.totalQuestions)}</strong>
                </div>
            </div>
            <p class="result-date"><strong>Attempted:</strong> ${escapeHtml(formatAttemptDate(result.attemptedAt))}</p>
            <div class="quiz-actions">
                <button class="secondary-button back-to-quizzes" type="button">Back to quizzes</button>
            </div>
        </section>`;
}

function showQuizList() {
    const workspace = document.getElementById("quiz-workspace");
    workspace.hidden = true;
    workspace.innerHTML = "";
    document.getElementById("quiz-list").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

async function loadQuizzes() {
    const quizList = document.getElementById("quiz-list");

    try {
        const response = await fetch(`${QUIZZES_API_URL}/${STUDENT_ID}`);
        if (!response.ok) {
            throw new Error(await readApiError(response, "Quizzes could not be loaded"));
        }

        const quizzes = await response.json();
        if (!Array.isArray(quizzes)) {
            throw new Error("The quizzes API returned an unexpected response.");
        }

        if (quizzes.length === 0) {
            quizList.innerHTML = `
                <p class="state-message">No quizzes are available for your enrolled courses yet.</p>`;
            return;
        }

        quizList.innerHTML = quizzes.map(renderQuiz).join("");
    } catch (error) {
        console.error("Unable to load student quizzes:", error);
        quizList.innerHTML = `
            <p class="state-message error">
                ${escapeHtml(error.message || "Quizzes could not be loaded.")}
            </p>`;
    }
}

document.addEventListener("DOMContentLoaded", function() {
    const quizList = document.getElementById("quiz-list");
    const workspace = document.getElementById("quiz-workspace");

    quizList.addEventListener("click", function(event) {
        const button = event.target.closest(".start-quiz");
        if (button) {
            startQuiz(button.dataset.quizId);
        }
    });

    workspace.addEventListener("click", function(event) {
        if (event.target.closest(".back-to-quizzes")) {
            showQuizList();
        }
    });

    workspace.addEventListener("submit", submitQuiz);
    loadQuizzes();
});
