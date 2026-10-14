// ========================================
// STUDENT LMS JAVASCRIPT
// ========================================

const API_BASE_URL = "http://localhost:8080/api/student";

let CURRENT_STUDENT = null;

// ========================================
// AUTHENTICATION & STUDENT RESOLUTION
// ========================================

async function getAuthenticatedStudent() {
    if (CURRENT_STUDENT) return CURRENT_STUDENT;

    // Check cached session
    const cached = sessionStorage.getItem("currentStudent");
    if (cached) {
        try {
            CURRENT_STUDENT = JSON.parse(cached);
        } catch (e) {}
    }

    try {
        const response = await fetch(`${API_BASE_URL}/current`);
        if (response.ok) {
            const data = await response.json();
            if (data && data.authenticated) {
                CURRENT_STUDENT = data;
                sessionStorage.setItem("currentStudent", JSON.stringify(data));
                return data;
            }
        }
    } catch (e) {
        console.warn("Could not retrieve current student from /api/student/current:", e);
    }

    return CURRENT_STUDENT || { id: 1, name: "Student", email: "student@lms.com" };
}

function getStudentId() {
    if (CURRENT_STUDENT && CURRENT_STUDENT.id) return CURRENT_STUDENT.id;
    const cached = sessionStorage.getItem("currentStudent");
    if (cached) {
        try {
            const d = JSON.parse(cached);
            if (d && d.id) return d.id;
        } catch (e) {}
    }
    return 1;
}

function getStudentName() {
    if (CURRENT_STUDENT && CURRENT_STUDENT.name) return CURRENT_STUDENT.name;
    const cached = sessionStorage.getItem("currentStudent");
    if (cached) {
        try {
            const d = JSON.parse(cached);
            if (d && d.name) return d.name;
        } catch (e) {}
    }
    return "Student";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

// ========================================
// NAVIGATION HELPERS
// ========================================

function browseCourses() {
    window.location.href = "course-catalog.html";
}

function goBackToCourses() {
    window.location.href = "my-courses.html";
}

function goBackToCourse() {
    const urlParams = new URLSearchParams(window.location.search);
    const courseId = urlParams.get("courseId");
    if (courseId) {
        window.location.href = `course-details.html?courseId=${encodeURIComponent(courseId)}`;
    } else {
        window.location.href = "my-courses.html";
    }
}

function continueCourse(courseName, courseId) {
    if (courseId !== undefined && courseId !== null) {
        window.location.href = `course-details.html?courseId=${encodeURIComponent(courseId)}`;
        return;
    }
    browseCourses();
}

// ========================================
// COURSE CARD BUILDER
// ========================================

function createCourseCard(course, progress, listType) {
    const card = document.createElement("article");
    card.className = listType === "dashboard" ? "course-item" : "course-card";

    const title = document.createElement("h3");
    title.textContent = course.title || "Untitled course";

    const rawDesc = course.description || "No description available.";
    const maxDescLen = 110;
    const shortDesc = rawDesc.length > maxDescLen 
        ? rawDesc.substring(0, maxDescLen).trim() + "..." 
        : rawDesc;

    const description = document.createElement("p");
    description.textContent = shortDesc;

    const courseProgress = Math.max(
        0,
        Math.min(100, Number(progress?.progressPercentage) || 0)
    );

    if (listType === "dashboard") {
        const info = document.createElement("div");
        info.className = "course-info";
        info.append(title, description);

        const progressContainer = document.createElement("div");
        progressContainer.className = "progress-container";
        const progressBar = document.createElement("div");
        progressBar.className = "progress-bar";
        const progressFill = document.createElement("div");
        progressFill.className = "progress";
        progressFill.style.width = `${courseProgress}%`;
        progressBar.appendChild(progressFill);

        const progressLabel = document.createElement("span");
        progressLabel.className = "progress-label";
        progressLabel.textContent = `${courseProgress}%`;
        progressContainer.append(progressBar, progressLabel);
        info.appendChild(progressContainer);

        const button = document.createElement("button");
        button.className = "continue-btn";
        button.type = "button";
        button.innerHTML = '<i class="fa-solid fa-play"></i> Continue';
        button.addEventListener("click", () =>
            continueCourse(course.title, course.courseId || course.id)
        );

        card.append(info, button);
        return card;
    }

    card.append(title, description);

    const progressContainer = document.createElement("div");
    progressContainer.className = "progress-container";
    progressContainer.innerHTML = `
        <div class="progress-label" style="display: flex; justify-content: space-between; width: 100%; margin-bottom: 4px;">
            <span>Curriculum Progress</span>
            <span style="color: var(--accent-emerald); font-weight: 600;">${courseProgress}%</span>
        </div>
        <div class="progress-bar">
            <div class="progress-fill" style="width: ${courseProgress}%"></div>
        </div>`;
    card.appendChild(progressContainer);

    const button = document.createElement("button");
    button.className = "continue-btn";
    button.type = "button";
    button.innerHTML = '<i class="fa-solid fa-book-reader"></i> Continue Learning';
    button.addEventListener("click", () =>
        continueCourse(course.title, course.courseId || course.id)
    );
    card.appendChild(button);

    return card;
}

// ========================================
// CATALOG CARD BUILDER (With Enroll Button)
// ========================================

function createCatalogCourseCard(course, isEnrolled, studentId) {
    const card = document.createElement("article");
    card.className = "course-card";

    const title = document.createElement("h3");
    title.textContent = course.title || "Untitled course";

    const rawDesc = course.description || "No description available.";
    const shortDesc = rawDesc.length > 110 ? rawDesc.substring(0, 110).trim() + "..." : rawDesc;
    const description = document.createElement("p");
    description.textContent = shortDesc;

    card.append(title, description);

    const statusRow = document.createElement("div");
    statusRow.style.cssText = "display: flex; justify-content: space-between; align-items: center; margin: 12px 0;";

    if (isEnrolled) {
        statusRow.innerHTML = `
            <span class="badge" style="background: rgba(16, 185, 129, 0.2); color: #10b981; padding: 4px 10px; border-radius: 6px; font-size: 0.8rem; font-weight: 600;">
                <i class="fa-solid fa-circle-check"></i> Enrolled
            </span>
            <span style="font-size: 0.8rem; color: var(--text-muted);">Active in My Courses</span>
        `;
    } else {
        statusRow.innerHTML = `
            <span class="badge" style="background: rgba(99, 102, 241, 0.15); color: var(--accent-indigo); padding: 4px 10px; border-radius: 6px; font-size: 0.8rem; font-weight: 600;">
                <i class="fa-solid fa-graduation-cap"></i> ${escapeHtml(course.category || "General")}
            </span>
            <span style="font-size: 0.8rem; color: var(--accent-emerald);">Open for Enrollment</span>
        `;
    }
    card.appendChild(statusRow);

    const actions = document.createElement("div");
    actions.style.cssText = "display: flex; gap: 10px; margin-top: auto;";

    const courseId = course.courseId || course.id;

    if (isEnrolled) {
        const learnBtn = document.createElement("button");
        learnBtn.className = "continue-btn";
        learnBtn.style.cssText = "flex: 1;";
        learnBtn.innerHTML = '<i class="fa-solid fa-play"></i> Continue Learning';
        learnBtn.onclick = () => continueCourse(course.title, courseId);
        actions.appendChild(learnBtn);
    } else {
        const enrollBtn = document.createElement("button");
        enrollBtn.className = "btn btn-primary";
        enrollBtn.style.cssText = "flex: 1; padding: 10px;";
        enrollBtn.innerHTML = '<i class="fa-solid fa-plus"></i> Enroll in Course';
        enrollBtn.onclick = async () => {
            enrollBtn.disabled = true;
            enrollBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Enrolling...';
            try {
                const res = await fetch(`${API_BASE_URL}/enrollments`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ studentId, courseId })
                });
                if (res.ok) {
                    enrollBtn.className = "btn btn-secondary";
                    enrollBtn.style.color = "#10b981";
                    enrollBtn.innerHTML = '<i class="fa-solid fa-check"></i> Enrolled!';
                    setTimeout(() => {
                        window.location.href = `course-details.html?courseId=${courseId}`;
                    }, 500);
                } else {
                    enrollBtn.disabled = false;
                    enrollBtn.innerHTML = '<i class="fa-solid fa-plus"></i> Enroll in Course';
                    alert("Could not complete enrollment. Please try again.");
                }
            } catch (err) {
                console.error("Enrollment error:", err);
                enrollBtn.disabled = false;
                enrollBtn.innerHTML = '<i class="fa-solid fa-plus"></i> Enroll in Course';
            }
        };
        actions.appendChild(enrollBtn);

        const viewBtn = document.createElement("button");
        viewBtn.className = "btn btn-secondary";
        viewBtn.innerHTML = '<i class="fa-solid fa-eye"></i> View';
        viewBtn.onclick = () => continueCourse(course.title, courseId);
        actions.appendChild(viewBtn);
    }

    card.appendChild(actions);
    return card;
}

// ========================================
// LOAD PROFILE FOR DASHBOARD & HEADER
// ========================================

async function loadDashboardProfile() {
    const student = await getAuthenticatedStudent();
    const studentId = student ? student.id : getStudentId();
    const fallbackName = student ? student.name : getStudentName();

    const nameDisplay = document.getElementById("dashboard-name");
    const profileName = document.getElementById("dashboard-profile-name");
    const profileRole = document.getElementById("dashboard-profile-role");
    const profileAvatar = document.getElementById("dashboard-profile-avatar");

    if (nameDisplay && fallbackName) {
        nameDisplay.textContent = fallbackName;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/profile/${studentId}`);
        if (!response.ok) {
            throw new Error(`Unable to load profile (HTTP ${response.status}).`);
        }

        const profile = await response.json();
        const name = profile.name || fallbackName;

        if (nameDisplay) nameDisplay.textContent = name;
        if (profileName) profileName.textContent = name;
        if (profileRole) profileRole.textContent = profile.role || "Student";
        if (profileAvatar) profileAvatar.textContent = name.charAt(0).toUpperCase() || "S";
    } catch (error) {
        console.warn("Using fallback profile name:", error);
    }
}

// ========================================
// LOAD STUDENT DASHBOARD
// ========================================

async function loadDashboard() {
    const courseList = document.getElementById("dashboard-course-list");
    const enrolledCount = document.getElementById("dashboard-enrolled-count");
    const overallProgress = document.getElementById("dashboard-overall-progress");
    const pendingAssignmentsEl = document.getElementById("dashboard-pending-assignments");
    const avgGradeEl = document.getElementById("dashboard-avg-grade");

    if (!courseList || !enrolledCount || !overallProgress) {
        return;
    }

    const student = await getAuthenticatedStudent();
    const studentId = student ? student.id : getStudentId();

    loadDashboardProfile();

    try {
        const [enrollmentResponse, courseResponse] = await Promise.all([
            fetch(`${API_BASE_URL}/enrollments/${studentId}`),
            fetch(`${API_BASE_URL}/courses`)
        ]);

        if (!enrollmentResponse.ok || !courseResponse.ok) {
            throw new Error("Unable to load dashboard courses.");
        }

        const [enrollments, courses] = await Promise.all([
            enrollmentResponse.json(),
            courseResponse.json()
        ]);

        let progressRecords = [];
        try {
            const progressResponse = await fetch(`${API_BASE_URL}/progress/${studentId}`);
            if (progressResponse.ok) {
                progressRecords = await progressResponse.json();
            }
        } catch (error) {
            console.error("Error loading dashboard progress:", error);
        }

        enrolledCount.textContent = String(enrollments.length);
        courseList.innerHTML = "";

        if (enrollments.length === 0) {
            courseList.innerHTML = `
                <div class="dashboard-empty-state" style="padding: 24px; text-align: center; background: rgba(30, 41, 59, 0.3); border-radius: 12px; border: 1px dashed var(--border-color);">
                    <p style="color: var(--text-muted); margin-bottom: 12px;">You are not enrolled in any courses yet.</p>
                    <a href="course-catalog.html" class="btn btn-primary btn-sm" style="display: inline-flex; align-items: center; gap: 6px;">
                        <i class="fa-solid fa-compass"></i> Browse Courses Catalog
                    </a>
                </div>`;
        } else {
            enrollments.forEach(enrollment => {
                const course = courses.find(item =>
                    Number(item.courseId || item.id) === Number(enrollment.courseId)
                );
                if (course) {
                    const courseProgress = progressRecords.find(record =>
                        Number(record.courseId) === Number(course.courseId || course.id)
                    );
                    courseList.appendChild(
                        createCourseCard(course, courseProgress, "dashboard")
                    );
                }
            });
        }

        // Overall progress calculation for this student
        if (progressRecords.length > 0) {
            const totalLessons = progressRecords.reduce((sum, r) => sum + Number(r.totalLessons || 0), 0);
            const compLessons = progressRecords.reduce((sum, r) => sum + Number(r.completedLessons || 0), 0);
            overallProgress.textContent = totalLessons > 0 ? `${Math.min(100, Math.round((compLessons / totalLessons) * 100))}%` : "0%";
        } else {
            overallProgress.textContent = enrollments.length > 0 ? "0%" : "—";
        }

        // Pending assignments for this student
        if (pendingAssignmentsEl) {
            try {
                const assignRes = await fetch(`${API_BASE_URL}/assignments/${studentId}`);
                if (assignRes.ok) {
                    const assignments = await assignRes.json();
                    const pending = assignments.filter(a => !a.submissionStatus || a.submissionStatus.toLowerCase() === "not submitted");
                    pendingAssignmentsEl.textContent = String(pending.length);
                }
            } catch (e) {
                pendingAssignmentsEl.textContent = "0";
            }
        }

        // Average grade calculation for this student
        if (avgGradeEl) {
            try {
                const [gradeRes, quizRes] = await Promise.all([
                    fetch(`${API_BASE_URL}/grades/${studentId}`),
                    fetch(`${API_BASE_URL}/quiz-attempts/${studentId}`)
                ]);
                let totalMarks = 0;
                let count = 0;

                if (gradeRes.ok) {
                    const grades = await gradeRes.json();
                    grades.forEach(g => {
                        if (g.marks !== null && g.marks !== undefined) {
                            totalMarks += Number(g.marks);
                            count++;
                        }
                    });
                }
                if (quizRes.ok) {
                    const attempts = await quizRes.json();
                    attempts.forEach(qa => {
                        if (qa.percentage !== null && qa.percentage !== undefined) {
                            totalMarks += Number(qa.percentage);
                            count++;
                        }
                    });
                }

                avgGradeEl.textContent = count > 0 ? `${Math.round(totalMarks / count)}%` : (enrollments.length > 0 ? "—" : "—");
            } catch (e) {
                avgGradeEl.textContent = "—";
            }
        }

    } catch (error) {
        console.error("Error loading dashboard:", error);
        enrolledCount.textContent = "—";
        overallProgress.textContent = "—";
        courseList.innerHTML = `
            <p class="dashboard-empty-state">
                Unable to load your courses. Please try again later.
            </p>`;
    }
}

// ========================================
// LOAD MY ENROLLED COURSES
// ========================================

async function loadMyCourses() {
    const courseList = document.getElementById("my-course-list");
    if (!courseList) return;

    const student = await getAuthenticatedStudent();
    const studentId = student ? student.id : getStudentId();

    try {
        const [enrollmentResponse, courseResponse] = await Promise.all([
            fetch(`${API_BASE_URL}/enrollments/${studentId}`),
            fetch(`${API_BASE_URL}/courses`)
        ]);

        if (!enrollmentResponse.ok || !courseResponse.ok) {
            throw new Error("Failed to load courses");
        }

        const [enrollments, courses] = await Promise.all([
            enrollmentResponse.json(),
            courseResponse.json()
        ]);

        let progressRecords = [];
        try {
            const progressResponse = await fetch(`${API_BASE_URL}/progress/${studentId}`);
            if (progressResponse.ok) {
                progressRecords = await progressResponse.json();
            }
        } catch (e) {}

        courseList.innerHTML = "";

        if (enrollments.length === 0) {
            courseList.innerHTML = `
                <div class="empty-message" style="grid-column: 1 / -1; padding: 40px; text-align: center; background: rgba(30, 41, 59, 0.4); border-radius: 12px; border: 1px dashed var(--border-color);">
                    <i class="fa-solid fa-book-open" style="font-size: 2.5rem; color: var(--accent-indigo); margin-bottom: 12px;"></i>
                    <h3 style="font-size: 1.2rem; color: #fff; margin-bottom: 8px;">You haven't enrolled in any courses yet</h3>
                    <p style="color: var(--text-muted); margin-bottom: 16px;">Browse our catalog of published courses and begin your learning path.</p>
                    <a href="course-catalog.html" class="btn btn-primary" style="display: inline-flex; align-items: center; gap: 8px;">
                        <i class="fa-solid fa-compass"></i> Explore Course Catalog
                    </a>
                </div>
            `;
            return;
        }

        enrollments.forEach(enrollment => {
            const course = courses.find(item =>
                Number(item.courseId || item.id) === Number(enrollment.courseId)
            );

            if (!course) return;

            const courseProgress = progressRecords.find(item =>
                Number(item.courseId) === Number(course.courseId || course.id)
            );
            courseList.appendChild(
                createCourseCard(course, courseProgress, "enrolled")
            );
        });

    } catch (error) {
        console.error("Error loading my courses:", error);
        courseList.innerHTML = `
            <div class="empty-message">
                <h3>Unable to load your courses</h3>
                <p>Please make sure the server is running.</p>
            </div>
        `;
    }
}

// ========================================
// LOAD BROWSE COURSES (CATALOG)
// ========================================

async function loadCourses() {
    const courseList = document.getElementById("course-list");
    if (!courseList) return;

    const student = await getAuthenticatedStudent();
    const studentId = student ? student.id : getStudentId();

    try {
        const [courseResponse, enrollmentResponse] = await Promise.all([
            fetch(`${API_BASE_URL}/courses`),
            fetch(`${API_BASE_URL}/enrollments/${studentId}`)
        ]);

        if (!courseResponse.ok) {
            throw new Error("Failed to load courses");
        }

        const courses = await courseResponse.json();
        const enrollments = enrollmentResponse.ok ? await enrollmentResponse.json() : [];
        const enrolledCourseIds = new Set(enrollments.map(e => Number(e.courseId)));

        courseList.innerHTML = "";

        const activeCourses = courses.filter(
            course => !course.status ||
                String(course.status).toLowerCase() === "published" ||
                String(course.status).toLowerCase() === "active"
        );

        if (activeCourses.length === 0) {
            courseList.innerHTML = `
                <div class="empty-message">
                    <h3>No courses available</h3>
                    <p>There are currently no active courses.</p>
                </div>
            `;
            return;
        }

        activeCourses.forEach(course => {
            const cId = Number(course.courseId || course.id);
            const isEnrolled = enrolledCourseIds.has(cId);
            courseList.appendChild(createCatalogCourseCard(course, isEnrolled, studentId));
        });

    } catch (error) {
        console.error("Error loading courses:", error);
        courseList.innerHTML = `
            <div class="empty-message">
                <h3>Unable to load courses</h3>
                <p>Please make sure the server is running.</p>
            </div>
        `;
    }
}

// ========================================
// LOAD COURSE DETAILS & MODULES
// ========================================

async function loadCourseDetails() {
    const moduleList = document.getElementById("module-list");
    if (!moduleList) return;

    const urlParams = new URLSearchParams(window.location.search);
    const courseId = urlParams.get("courseId");

    if (!courseId) {
        document.getElementById("course-title").textContent = "Course not found";
        document.getElementById("course-description").textContent = "No course was selected.";
        moduleList.innerHTML = `
            <div class="empty-message">
                <h3>Course not found</h3>
                <p>Please return to My Courses.</p>
            </div>
        `;
        return;
    }

    const student = await getAuthenticatedStudent();
    const studentId = student ? student.id : getStudentId();

    try {
        const [courseResponse, enrollmentResponse] = await Promise.all([
            fetch(`${API_BASE_URL}/courses`),
            fetch(`${API_BASE_URL}/enrollments/${studentId}`)
        ]);

        if (!courseResponse.ok) {
            throw new Error("Failed to load course");
        }

        const courses = await courseResponse.json();
        const enrollments = enrollmentResponse.ok ? await enrollmentResponse.json() : [];
        const isEnrolled = enrollments.some(e => Number(e.courseId) === Number(courseId));

        const course = courses.find(item => String(item.courseId || item.id) === String(courseId));
        if (!course) {
            throw new Error("Course not found");
        }

        const titleEl = document.getElementById("course-title");
        const descEl = document.getElementById("course-description");
        if (titleEl) titleEl.textContent = course.title;
        if (descEl) descEl.textContent = course.description || "No description available.";

        // If not enrolled, offer an enrollment button in course header
        const headerEl = document.getElementById("course-header");
        if (headerEl && !isEnrolled && !document.getElementById("header-enroll-btn")) {
            const enrollContainer = document.createElement("div");
            enrollContainer.style.marginTop = "16px";
            enrollContainer.innerHTML = `
                <button id="header-enroll-btn" class="btn btn-primary" style="padding: 10px 20px; font-weight: 600;">
                    <i class="fa-solid fa-plus"></i> Enroll in this Course
                </button>
            `;
            headerEl.appendChild(enrollContainer);
            document.getElementById("header-enroll-btn").onclick = async function() {
                this.disabled = true;
                this.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Enrolling...';
                await fetch(`${API_BASE_URL}/enrollments`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ studentId, courseId: Number(courseId) })
                });
                this.className = "btn btn-secondary";
                this.innerHTML = '<i class="fa-solid fa-circle-check"></i> Enrolled in Course';
            };
        }

        const moduleResponse = await fetch(`${API_BASE_URL}/modules/course/${courseId}`);
        if (!moduleResponse.ok) {
            throw new Error("Failed to load modules");
        }

        const modules = await moduleResponse.json();
        moduleList.innerHTML = "";

        if (modules.length === 0) {
            moduleList.innerHTML = `
                <div class="empty-message">
                    <h3>No modules available</h3>
                    <p>This course does not have any modules yet.</p>
                </div>
            `;
            return;
        }

        modules.forEach(function(module, index) {
            const moduleCard = document.createElement("div");
            moduleCard.className = "module-card";
            moduleCard.innerHTML = `
                <div class="module-header" onclick="toggleModule(${module.id})">
                    <div class="module-left">
                        <div class="module-number">${index + 1}</div>
                        <div class="module-info">
                            <h3>${escapeHtml(module.title)}</h3>
                            <p>${escapeHtml(module.description || "")}</p>
                        </div>
                    </div>
                    <span class="chevron" id="chevron-${module.id}">▾</span>
                </div>
                <div class="lesson-list" id="lessons-${module.id}">
                    <p class="loading">Click to view lessons</p>
                </div>
            `;
            moduleList.appendChild(moduleCard);
        });

    } catch (error) {
        console.error("Error loading course details:", error);
        moduleList.innerHTML = `
            <div class="empty-message">
                <h3>Unable to load course</h3>
                <p>Please make sure the server is running.</p>
            </div>
        `;
    }
}

// ========================================
// TOGGLE MODULE LESSONS
// ========================================

async function toggleModule(moduleId) {
    const lessonList = document.getElementById(`lessons-${moduleId}`);
    const chevron = document.getElementById(`chevron-${moduleId}`);
    if (!lessonList) return;

    if (lessonList.classList.contains("open")) {
        lessonList.classList.remove("open");
        if (chevron) chevron.textContent = "▾";
        return;
    }

    lessonList.classList.add("open");
    if (chevron) chevron.textContent = "▴";

    if (lessonList.dataset.loaded === "true") {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/lessons/module/${moduleId}`);
        if (!response.ok) {
            throw new Error("Failed to load lessons");
        }

        const lessons = await response.json();
        lessonList.innerHTML = "";

        if (lessons.length === 0) {
            lessonList.innerHTML = `
                <p class="loading" style="padding: 12px; color: var(--text-muted);">
                    No lessons available in this module.
                </p>`;
            return;
        }

        lessons.forEach(function(lesson, index) {
            const lessonItem = document.createElement("div");
            lessonItem.className = "lesson-item";
            lessonItem.onclick = function() {
                openLesson(lesson.id, moduleId);
            };
            lessonItem.innerHTML = `
                <div class="lesson-left">
                    <span class="lesson-number">${index + 1}</span>
                    <span class="lesson-title">${escapeHtml(lesson.title)}</span>
                </div>
                <span class="lesson-arrow">→</span>
            `;
            lessonList.appendChild(lessonItem);
        });

        lessonList.dataset.loaded = "true";
    } catch (error) {
        console.error("Error loading lessons:", error);
        lessonList.innerHTML = `
            <p class="loading" style="color: var(--accent-rose);">
                Unable to load lessons.
            </p>`;
    }
}

function openLesson(lessonId, moduleId) {
    const urlParams = new URLSearchParams(window.location.search);
    const courseId = urlParams.get("courseId");
    window.location.href = `lesson.html?lessonId=${lessonId}&courseId=${courseId}`;
}

// ========================================
// LOAD LESSON
// ========================================

async function loadLesson() {
    const lessonBody = document.getElementById("lesson-body");
    if (!lessonBody) return;

    const urlParams = new URLSearchParams(window.location.search);
    const lessonId = urlParams.get("lessonId");

    if (!lessonId) {
        document.getElementById("lesson-title").textContent = "Lesson not found";
        lessonBody.innerHTML = `
            <div class="error-message">
                <h3>Lesson not found</h3>
                <p>No lesson was selected.</p>
            </div>
        `;
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/lessons/${lessonId}`);
        if (!response.ok) {
            throw new Error("Failed to load lesson");
        }

        const lesson = await response.json();
        document.getElementById("lesson-title").textContent = lesson.title;

        if (lesson.content && lesson.content.trim() !== "") {
            lessonBody.textContent = lesson.content;
        } else {
            lessonBody.innerHTML = `
                <div class="empty-message" style="padding: 24px; text-align: center; color: var(--text-muted);">
                    <p>Welcome to <strong>${escapeHtml(lesson.title)}</strong>. Read through this module material carefully and click "Mark Lesson Complete" below to save your progress.</p>
                </div>
            `;
        }
    } catch (error) {
        console.error("Error loading lesson:", error);
        document.getElementById("lesson-title").textContent = "Unable to load lesson";
        lessonBody.innerHTML = `
            <div class="error-message">
                <h3>Unable to load lesson</h3>
                <p>Please make sure the server is running.</p>
            </div>
        `;
    }
}

// ========================================
// MARK LESSON COMPLETE
// ========================================

async function markLessonComplete() {
    const button = document.querySelector(".complete-button");
    if (!button) return;

    const urlParams = new URLSearchParams(window.location.search);
    const courseId = urlParams.get("courseId");
    const lessonId = urlParams.get("lessonId");

    if (!courseId || !lessonId) return;

    const student = await getAuthenticatedStudent();
    const studentId = student ? student.id : getStudentId();

    try {
        button.disabled = true;
        button.textContent = "Updating progress...";

        const completionResponse = await fetch(
            `${API_BASE_URL}/progress/${studentId}/course/${courseId}/lesson/${lessonId}/complete`,
            { method: "POST" }
        );

        if (!completionResponse.ok) {
            throw new Error("Failed to update progress");
        }

        const completion = await completionResponse.json();
        const progress = completion.progress;
        const completionStatus = document.getElementById("lesson-completion-status");

        if (completionStatus && progress) {
            completionStatus.textContent = `Course progress: ${progress.completedLessons} of ${progress.totalLessons} lessons (${progress.progressPercentage}%).`;
        }

        button.textContent = completion.alreadyCompleted ? "✓ Already Completed" : "✓ Lesson Completed";
        button.classList.add("completed");
        button.disabled = false;
    } catch (error) {
        console.error("Error updating progress:", error);
        button.disabled = false;
        button.textContent = "✓ Mark Lesson Complete";
        alert("Unable to update progress. Please make sure the server is running.");
    }
}

// ========================================
// LOAD MY PROGRESS
// ========================================

async function loadMyProgress() {
    const progressList = document.getElementById("progress-list") || document.getElementById("progress-course-list");
    if (!progressList) return;

    const student = await getAuthenticatedStudent();
    const studentId = student ? student.id : getStudentId();

    try {
        const [progressResponse, courseResponse, quizResponse] = await Promise.all([
            fetch(`${API_BASE_URL}/progress/${studentId}`),
            fetch(`${API_BASE_URL}/courses`),
            fetch(`${API_BASE_URL}/quiz-attempts/${studentId}`)
        ]);

        if (!progressResponse.ok || !courseResponse.ok) {
            throw new Error("Failed to load progress data");
        }

        const progressData = await progressResponse.json();
        const courses = await courseResponse.json();
        const attempts = quizResponse.ok ? await quizResponse.json() : [];

        progressList.innerHTML = "";

        if (progressData.length === 0) {
            progressList.innerHTML = `
                <div class="empty-message" style="padding: 40px; text-align: center; background: rgba(30, 41, 59, 0.4); border-radius: 12px; border: 1px dashed var(--border-color);">
                    <i class="fa-solid fa-chart-line" style="font-size: 2.5rem; color: var(--accent-indigo); margin-bottom: 12px;"></i>
                    <h3 style="font-size: 1.2rem; color: #fff; margin-bottom: 8px;">No progress available</h3>
                    <p style="color: var(--text-muted); margin-bottom: 16px;">Enroll in a course and begin your lessons to see your detailed completion metrics here.</p>
                    <a href="course-catalog.html" class="btn btn-primary" style="display: inline-flex; align-items: center; gap: 8px;">
                        <i class="fa-solid fa-compass"></i> Explore Course Catalog
                    </a>
                </div>
            `;
            return;
        }

        progressData.forEach(function(progress) {
            const course = courses.find(item => Number(item.courseId || item.id) === Number(progress.courseId));
            if (!course) return;

            const percentage = Number(progress.progressPercentage || 0);
            const completed = Number(progress.completedLessons || 0);
            const total = Number(progress.totalLessons || 5);

            const courseAttempts = attempts.filter(a => Number(a.courseId) === Number(course.courseId || course.id));
            const quizScoreText = courseAttempts.length > 0 ? `${courseAttempts[0].score || courseAttempts[0].percentage}%` : "Not attempted";

            const card = document.createElement("div");
            card.className = "progress-card";
            card.style.cssText = "background: rgba(30, 41, 59, 0.6); border: 1px solid var(--border-color); border-radius: 12px; padding: 24px; margin-bottom: 16px;";
            card.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px;">
                    <div>
                        <span style="font-size: 0.8rem; font-weight: 700; color: var(--accent-indigo); text-transform: uppercase;">Course Track</span>
                        <h3 style="font-size: 1.2rem; color: #fff; margin-top: 4px;">${escapeHtml(course.title)}</h3>
                    </div>
                    <span style="font-size: 1.5rem; font-weight: 800; color: var(--accent-emerald);">${percentage}%</span>
                </div>
                <div style="background: rgba(15, 23, 42, 0.8); height: 10px; border-radius: 999px; overflow: hidden; margin-bottom: 16px;">
                    <div style="background: linear-gradient(90deg, var(--accent-indigo), var(--accent-emerald)); height: 100%; width: ${percentage}%; border-radius: 999px; transition: width 0.4s ease;"></div>
                </div>
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px; font-size: 0.9rem; color: var(--text-muted);">
                    <div><i class="fa-solid fa-circle-check" style="color: var(--accent-emerald);"></i> <strong>Lessons:</strong> ${completed} / ${total} completed</div>
                    <div><i class="fa-solid fa-brain" style="color: var(--accent-amber);"></i> <strong>Quiz Status:</strong> ${quizScoreText}</div>
                </div>
            `;
            progressList.appendChild(card);
        });

    } catch (error) {
        console.error("Error loading progress:", error);
    }
}

// ========================================
// LOGOUT
// ========================================

function setupLogout() {
    const logoutButtons = document.querySelectorAll(".logout, a[href='/logout']");
    logoutButtons.forEach(btn => {
        btn.addEventListener("click", function(event) {
            sessionStorage.removeItem("currentStudent");
            window.location.href = "/logout";
        });
    });
}

// ========================================
// INITIALIZATION
// ========================================

document.addEventListener("DOMContentLoaded", async function() {
    await getAuthenticatedStudent();

    loadCourses();
    loadMyCourses();
    loadDashboard();
    loadCourseDetails();
    loadLesson();
    loadMyProgress();
    setupLogout();
});