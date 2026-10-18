const ASSIGNMENTS_API_URL = "/api/student/assignments";
const SUBMISSIONS_API_URL = "/api/student/submissions";
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

function formatDate(value) {
    if (!value) {
        return "No due date set";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return "Due date unavailable";
    }

    return new Intl.DateTimeFormat(undefined, {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(date);
}

function getStatusClass(status) {
    const normalizedStatus = String(status || "").toLowerCase();
    if (normalizedStatus.includes("graded")) return " status-graded";
    if (normalizedStatus.includes("submitted")) return " status-submitted";
    return " status-not-submitted";
}

function renderAssignment(assignment) {
    const courseName = assignment.courseTitle
        ? escapeHtml(assignment.courseTitle)
        : `Course ${escapeHtml(assignment.courseId)}`;
    const title = escapeHtml(assignment.title || "Untitled assignment");
    const description = assignment.description
        ? escapeHtml(assignment.description)
        : "No assignment description was provided.";
    const rawStatus = assignment.submissionStatus || "Not submitted";
    const statusText = escapeHtml(rawStatus);
    const statusClass = getStatusClass(rawStatus);

    let statusIcon = '<i class="fa-solid fa-clock"></i>';
    if (statusClass.includes("graded")) statusIcon = '<i class="fa-solid fa-award"></i>';
    else if (statusClass.includes("submitted")) statusIcon = '<i class="fa-solid fa-circle-check"></i>';
    else if (statusClass.includes("not-submitted")) statusIcon = '<i class="fa-solid fa-circle-exclamation"></i>';

    const marks = assignment.marks === null || assignment.marks === undefined
        ? "Not graded"
        : `${escapeHtml(assignment.marks)} / ${escapeHtml(assignment.maxMarks || 100)} pts`;

    const feedback = assignment.feedback
        ? `
            <section class="feedback-panel">
                <h3>Instructor feedback</h3>
                <p>${escapeHtml(assignment.feedback)}</p>
            </section>`
        : "";

    const submissionMeta = assignment.submittedAt
        ? `<span><strong>Submitted:</strong> ${escapeHtml(formatDate(assignment.submittedAt))}</span>`
        : "";

    const submissionForm = rawStatus === "Not submitted"
        ? `
            <form class="submission-form" data-assignment-id="${escapeHtml(assignment.assignmentId)}">
                <label for="submission-${escapeHtml(assignment.assignmentId)}">
                    Submit work
                </label>
                <textarea
                    id="submission-${escapeHtml(assignment.assignmentId)}"
                    name="submissionFile"
                    rows="3"
                    placeholder="Enter your submission answers, repository link, or text..."
                    required
                ></textarea>
                <div class="form-actions">
                    <button class="primary-button" type="submit">
                        <i class="fa-solid fa-paper-plane"></i> Submit Assignment
                    </button>
                    <span class="submission-message" role="status" aria-live="polite"></span>
                </div>
            </form>`
        : "";

    return `
        <article class="assignment-card">
            <div class="assignment-header">
                <div>
                    <span class="course-name"><i class="fa-solid fa-graduation-cap"></i> ${courseName}</span>
                    <h2>${title}</h2>
                </div>
                <div class="header-status">
                    <span class="status-badge${statusClass}">${statusIcon} ${statusText}</span>
                    <strong class="assignment-marks">${marks}</strong>
                </div>
            </div>
            <p class="assignment-description">${description}</p>
            <div class="assignment-meta">
                <span><strong>Due:</strong> ${escapeHtml(formatDate(assignment.dueDate))}</span>
                ${submissionMeta}
            </div>
            ${feedback}
            ${submissionForm}
        </article>`;
}

async function getSubmissionError(response) {
    try {
        const problem = await response.json();
        return problem?.detail || problem?.message || `Your assignment could not be submitted (HTTP ${response.status}).`;
    } catch {
        return `Your assignment could not be submitted (HTTP ${response.status}).`;
    }
}

async function submitAssignment(event) {
    event.preventDefault();

    const form = event.currentTarget;
    const button = form.querySelector("button[type='submit']");
    const message = form.querySelector(".submission-message");
    const submissionField = form.elements.namedItem("submissionFile");
    const submissionFile = submissionField ? submissionField.value.trim() : "";

    if (!submissionFile) {
        message.textContent = "Please enter your submission response before submitting.";
        message.className = "submission-message error";
        return;
    }

    button.disabled = true;
    message.textContent = "Submitting work...";
    message.classList.remove("error", "success");

    try {
        const student = await getAuthStudent();
        const response = await fetch(SUBMISSIONS_API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                assignmentId: Number(form.dataset.assignmentId),
                studentId: student.id,
                studentName: student.name,
                content: submissionFile,
                submissionFile
            })
        });

        if (!response.ok) {
            throw new Error(await getSubmissionError(response));
        }

        const savedSubmission = await response.json();
        const card = form.closest(".assignment-card");
        const badge = card.querySelector(".status-badge");
        badge.textContent = savedSubmission.status || "Submitted";
        badge.className = `status-badge status-submitted`;

        const successMessage = document.createElement("p");
        successMessage.className = "submission-notice";
        successMessage.style.cssText = "color: #10b981; margin-top: 12px; font-weight: 600;";
        successMessage.innerHTML = '<i class="fa-solid fa-circle-check"></i> Your assignment was submitted successfully.';
        form.replaceWith(successMessage);
    } catch (error) {
        console.error("Unable to submit assignment:", error);
        message.textContent = error.message || "Your assignment could not be submitted.";
        message.classList.add("error");
        button.disabled = false;
    }
}

async function loadAssignments() {
    const assignmentList = document.getElementById("assignment-list");
    if (!assignmentList) return;

    try {
        const student = await getAuthStudent();
        const response = await fetch(`${ASSIGNMENTS_API_URL}/${student.id}`);
        if (!response.ok) {
            throw new Error(`Assignments could not be loaded (HTTP ${response.status}).`);
        }

        const assignments = await response.json();
        if (!Array.isArray(assignments)) {
            throw new Error("The assignments API returned an unexpected response.");
        }

        if (assignments.length === 0) {
            assignmentList.innerHTML = `
                <p class="state-message">
                    No assignments are available for your enrolled courses yet.
                </p>`;
            return;
        }

        assignmentList.innerHTML = assignments.map(renderAssignment).join("");
        document.querySelectorAll(".submission-form").forEach(form => {
            form.addEventListener("submit", submitAssignment);
        });
    } catch (error) {
        console.error("Unable to load student assignments:", error);
        assignmentList.innerHTML = `
            <p class="state-message error">
                ${escapeHtml(error.message || "Assignments could not be loaded. Please try again later.")}
            </p>`;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    loadAssignments();
});
