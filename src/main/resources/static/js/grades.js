const GRADES_API_URL = "http://localhost:8080/api/student/grades";
const STUDENT_ID = 1;

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function formatSubmittedDate(value) {
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

function isGraded(grade) {
    return grade.marks !== null
        && grade.marks !== undefined
        && Number.isFinite(Number(grade.marks));
}

function renderGrade(grade) {
    const courseTitle = grade.courseTitle
        ? escapeHtml(grade.courseTitle)
        : `Course ${escapeHtml(grade.courseId ?? "unavailable")}`;
    const assignmentTitle = escapeHtml(grade.assignmentTitle || "Assignment");
    const status = escapeHtml(grade.submissionStatus || "Status unavailable");
    const marks = isGraded(grade)
        ? escapeHtml(grade.marks)
        : "Not graded";
    const feedback = grade.feedback
        ? escapeHtml(grade.feedback)
        : "No instructor feedback yet.";
    const statusClass = String(grade.submissionStatus || "").toLowerCase();

    return `
        <article class="grade-card">
            <div class="grade-card-header">
                <div>
                    <p class="course-name">${courseTitle}</p>
                    <h2>${assignmentTitle}</h2>
                </div>
                <strong class="grade-marks">${marks}</strong>
            </div>
            <div class="grade-details">
                <span><strong>Status:</strong> <span class="status-badge status-${escapeHtml(statusClass)}">${status}</span></span>
                <span><strong>Submitted:</strong> ${escapeHtml(formatSubmittedDate(grade.submittedAt))}</span>
            </div>
            <section class="feedback-panel">
                <h3>Instructor feedback</h3>
                <p>${feedback}</p>
            </section>
        </article>`;
}

function updateGradeSummary(grades) {
    const gradedRecords = grades.filter(isGraded);
    const totalMarks = gradedRecords.reduce(
        (sum, grade) => sum + Number(grade.marks),
        0
    );
    const average = gradedRecords.length === 0
        ? null
        : totalMarks / gradedRecords.length;

    document.getElementById("graded-count").textContent =
        String(gradedRecords.length);
    document.getElementById("average-marks").textContent =
        average === null ? "—" : Number(average.toFixed(2)).toString();

    return gradedRecords.length;
}

async function loadGrades() {
    const gradeList = document.getElementById("grade-list");
    const gradeNotice = document.getElementById("grade-notice");

    try {
        const response = await fetch(`${GRADES_API_URL}/${STUDENT_ID}`);
        if (!response.ok) {
            throw new Error(`Grades could not be loaded (HTTP ${response.status}).`);
        }

        const grades = await response.json();
        if (!Array.isArray(grades)) {
            throw new Error("The grades API returned an unexpected response.");
        }

        const gradedCount = updateGradeSummary(grades);
        if (gradedCount === 0 && grades.length > 0) {
            gradeNotice.textContent =
                "No graded assignments yet. Your submitted assignments are shown below while they await grading.";
            gradeNotice.hidden = false;
        }

        if (grades.length === 0) {
            gradeList.innerHTML = `
                <p class="state-message">No graded assignments yet.</p>`;
            return;
        }

        gradeList.innerHTML = grades.map(renderGrade).join("");
    } catch (error) {
        console.error("Unable to load student grades:", error);
        gradeList.innerHTML = `
            <p class="state-message error">
                ${escapeHtml(error.message || "Grades could not be loaded. Please try again later.")}
            </p>`;
    }
}

document.addEventListener("DOMContentLoaded", loadGrades);
