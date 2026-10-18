const GRADES_API_URL = "/api/student/grades";
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
        ? `${escapeHtml(grade.marks)} pts`
        : "Pending Grade";
    const feedback = grade.feedback
        ? escapeHtml(grade.feedback)
        : "No instructor feedback yet.";
    const statusClass = String(grade.submissionStatus || "").toLowerCase();

    return `
        <article class="grade-card">
            <div class="grade-card-header">
                <div>
                    <p class="course-name"><i class="fa-solid fa-graduation-cap"></i> ${courseTitle}</p>
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

    const countEl = document.getElementById("graded-count");
    const avgEl = document.getElementById("average-marks");

    if (countEl) countEl.textContent = String(gradedRecords.length);
    if (avgEl) avgEl.textContent = average === null ? "—" : Number(average.toFixed(1)).toString() + "%";

    return gradedRecords.length;
}

async function loadGrades() {
    const gradeList = document.getElementById("grade-list");
    const gradeNotice = document.getElementById("grade-notice");

    try {
        const student = await getAuthStudent();
        const response = await fetch(`${GRADES_API_URL}/${student.id}`);
        if (!response.ok) {
            throw new Error(`Grades could not be loaded (HTTP ${response.status}).`);
        }

        const grades = await response.json();
        if (!Array.isArray(grades)) {
            throw new Error("The grades API returned an unexpected response.");
        }

        const gradedCount = updateGradeSummary(grades);
        if (gradeNotice) {
            if (gradedCount === 0 && grades.length > 0) {
                gradeNotice.textContent =
                    "No graded assignments yet. Your submitted assignments are shown below while they await grading.";
                gradeNotice.hidden = false;
            } else {
                gradeNotice.hidden = true;
            }
        }

        if (grades.length === 0) {
            if (gradeList) {
                gradeList.innerHTML = `
                    <div class="empty-message" style="padding: 32px; text-align: center; background: rgba(30, 41, 59, 0.4); border-radius: 12px; border: 1px dashed var(--border-color);">
                        <i class="fa-solid fa-award" style="font-size: 2.5rem; color: var(--accent-indigo); margin-bottom: 12px;"></i>
                        <h3 style="font-size: 1.2rem; margin-bottom: 6px;">No grades available yet</h3>
                        <p style="color: var(--text-muted); margin-bottom: 16px;">Once you submit assignments and your instructor evaluates them, your graded results will appear here.</p>
                        <a href="my-assignments.html" class="btn btn-primary" style="display: inline-flex; align-items: center; gap: 8px;"><i class="fa-solid fa-pen-to-square"></i> Go to Assignments</a>
                    </div>
                `;
            }
            return;
        }

        if (gradeList) {
            gradeList.innerHTML = grades.map(renderGrade).join("");
        }
    } catch (error) {
        console.error("Unable to load grades:", error);
        if (gradeList) {
            gradeList.innerHTML = `
                <p class="state-message error">
                    ${escapeHtml(error.message || "Grades could not be loaded. Please try again later.")}
                </p>`;
        }
    }
}

document.addEventListener("DOMContentLoaded", () => {
    loadGrades();
});
