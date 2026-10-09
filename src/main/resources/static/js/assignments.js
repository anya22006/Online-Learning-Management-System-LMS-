const ASSIGNMENTS_API_URL = "http://localhost:8080/api/student/assignments";
const SUBMISSIONS_API_URL = "http://localhost:8080/api/student/submissions";
const STUDENT_ID = 1;

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
    return ["submitted", "graded", "reviewed", "late", "rejected"]
        .includes(normalizedStatus)
        ? ` status-${normalizedStatus}`
        : "";
}

function renderAssignment(assignment) {
    const courseName = assignment.courseTitle
        ? escapeHtml(assignment.courseTitle)
        : `Course ${escapeHtml(assignment.courseId)}`;
    const title = escapeHtml(assignment.title || "Untitled assignment");
    const description = assignment.description
        ? escapeHtml(assignment.description)
        : "No assignment description was provided.";
    const status = assignment.submissionStatus
        ? escapeHtml(assignment.submissionStatus)
        : "Status not set";
    const marks = assignment.marks === null || assignment.marks === undefined
        ? "Not graded"
        : escapeHtml(assignment.marks);
    const feedback = assignment.feedback
        ? `<section class="feedback-panel"><h3>Instructor feedback</h3><p>${escapeHtml(assignment.feedback)}</p></section>`
        : "";
    const isNotSubmitted = String(assignment.submissionStatus || "").toLowerCase()
        === "not submitted";
    const submissionForm = isNotSubmitted
        ? `
            <form class="submission-form" data-assignment-id="${escapeHtml(assignment.assignmentId)}">
                <label for="submission-${escapeHtml(assignment.assignmentId)}">
                    Your submission or file reference
                </label>
                <textarea
                    id="submission-${escapeHtml(assignment.assignmentId)}"
                    name="submissionFile"
                    maxlength="500"
                    required
                    placeholder="Paste your response or a shareable file link (500 characters maximum)"
                ></textarea>
                <div class="submission-form-footer">
                    <span class="submission-limit">Text or link only · up to 500 characters</span>
                    <button class="submit-button" type="submit">Submit assignment</button>
                </div>
                <p class="submission-message" role="status" aria-live="polite"></p>
            </form>`
        : "";

    return `
        <article class="assignment-card" data-assignment-id="${escapeHtml(assignment.assignmentId)}">
            <div class="assignment-topline">
                <div>
                    <p class="course-name">${courseName}</p>
                    <h2>${title}</h2>
                </div>
                <span class="status-badge${getStatusClass(assignment.submissionStatus)}">${status}</span>
            </div>
            <p class="assignment-description">${description}</p>
            <div class="assignment-meta">
                <span><strong>Due:</strong> ${escapeHtml(formatDate(assignment.dueDate))}</span>
                <span><strong>Marks:</strong> ${marks}</span>
                ${assignment.submittedAt
                    ? `<span><strong>Submitted:</strong> ${escapeHtml(formatDate(assignment.submittedAt))}</span>`
                    : ""}
            </div>
            ${feedback}
            ${submissionForm}
        </article>`;
}

async function getSubmissionError(response) {
    try {
        const error = await response.json();
        return error.detail || error.message || error.title
            || `Submission failed (HTTP ${response.status}).`;
    } catch {
        return `Submission failed (HTTP ${response.status}).`;
    }
}

async function submitAssignment(event) {
    const form = event.target.closest(".submission-form");
    if (!form) {
        return;
    }

    event.preventDefault();

    const submissionFile = form.elements.submissionFile.value.trim();
    const message = form.querySelector(".submission-message");
    const button = form.querySelector(".submit-button");
    if (!submissionFile) {
        message.textContent = "Enter your response or a file reference first.";
        message.classList.add("error");
        return;
    }

    button.disabled = true;
    message.textContent = "Submitting…";
    message.classList.remove("error", "success");

    try {
        const response = await fetch(SUBMISSIONS_API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                assignmentId: Number(form.dataset.assignmentId),
                studentId: STUDENT_ID,
                submissionFile
            })
        });

        if (!response.ok) {
            throw new Error(await getSubmissionError(response));
        }

        const savedSubmission = await response.json();
        const card = form.closest(".assignment-card");
        const badge = card.querySelector(".status-badge");
        badge.textContent = savedSubmission.status || "submitted";
        badge.className = `status-badge${getStatusClass(savedSubmission.status)}`;

        if (savedSubmission.submittedAt) {
            const submittedAt = document.createElement("span");
            const label = document.createElement("strong");
            label.textContent = "Submitted: ";
            submittedAt.append(label, formatDate(savedSubmission.submittedAt));
            card.querySelector(".assignment-meta").appendChild(submittedAt);
        }

        const successMessage = document.createElement("p");
        successMessage.className = "submission-notice";
        successMessage.setAttribute("role", "status");
        successMessage.textContent = "Your assignment was submitted successfully.";
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
    if (!assignmentList) {
        return;
    }

    try {
        const response = await fetch(`${ASSIGNMENTS_API_URL}/${STUDENT_ID}`);
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
    } catch (error) {
        console.error("Unable to load student assignments:", error);
        assignmentList.innerHTML = `
            <p class="state-message error">
                ${escapeHtml(error.message || "Assignments could not be loaded. Please try again later.")}
            </p>`;
    }
}

document.addEventListener("DOMContentLoaded", function() {
    const assignmentList = document.getElementById("assignment-list");
    if (assignmentList) {
        assignmentList.addEventListener("submit", submitAssignment);
    }

    loadAssignments();
});
