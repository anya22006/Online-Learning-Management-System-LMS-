// ========================================
// STUDENT LMS JAVASCRIPT
// ========================================


// ========================================
// API CONFIGURATION
// ========================================

const API_BASE_URL =
    "http://localhost:8080/api/student";


// Temporary student ID
// Later this will come from authentication

const STUDENT_ID = 1;


// ========================================
// BROWSE COURSES
// ========================================

function browseCourses() {

    window.location.href =
        "course-catalog.html";
}


// ========================================
// BACK TO MY COURSES
// ========================================

function goBackToCourses() {

    window.location.href =
        "my-courses.html";
}


// ========================================
// BACK TO COURSE DETAILS
// ========================================

function goBackToCourse() {

    const urlParams =
        new URLSearchParams(
            window.location.search
        );

    const courseId =
        urlParams.get("courseId");


    if (courseId) {

        window.location.href =
            `course-details.html?courseId=${courseId}`;

    } else {

        window.location.href =
            "my-courses.html";

    }
}


// ========================================
// CONTINUE COURSE
// ========================================

function continueCourse(
    courseName,
    courseId
) {

    if (courseId !== undefined && courseId !== null) {
        window.location.href =
            `course-details.html?courseId=${encodeURIComponent(courseId)}`;
        return;
    }

    browseCourses();
}


function createCourseCard(course, progress, listType) {
    const card = document.createElement("article");
    card.className = listType === "dashboard"
        ? "course-item"
        : "course-card";

    const titleText = course.title || "Untitled course";
    const rawDesc = course.description || "No description available.";
    const maxDescLen = 110;
    const shortDesc = rawDesc.length > maxDescLen 
        ? rawDesc.substring(0, maxDescLen).trim() + "..." 
        : rawDesc;

    const courseProgress = Math.max(
        0,
        Math.min(100, Number(progress?.progressPercentage) || 0)
    );

    if (listType === "dashboard") {
        card.style.cssText = "display: flex; flex-direction: column; gap: 12px; width: 100%; padding: 18px 20px; background: #0f172a; border: 1px solid #334155; border-radius: 10px; transition: all 0.2s ease;";
        card.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 12px;">
                <div style="flex: 1; min-width: 0;">
                    <h3 style="font-size: 1.05rem; font-weight: 600; color: #f8fafc; margin: 0 0 6px 0; display: flex; align-items: center; gap: 8px;">
                        <i class="fa-solid fa-book-open" style="color: #6366f1;"></i>
                        <span>${escapeHtml(titleText)}</span>
                    </h3>
                    <p style="font-size: 0.875rem; color: #94a3b8; margin: 0; line-height: 1.45;">${escapeHtml(shortDesc)}</p>
                </div>
                <span class="badge badge-published" style="white-space: nowrap; font-size: 11px;">${escapeHtml(course.category || 'Course')}</span>
            </div>
            
            <div style="display: flex; justify-content: space-between; align-items: center; gap: 20px; border-top: 1px solid #1e293b; padding-top: 12px; margin-top: 4px;">
                <div style="display: flex; align-items: center; gap: 12px; flex: 1; max-width: 280px;">
                    <div class="progress-bar" style="margin: 0; flex: 1; height: 8px; background: #1e293b; border-radius: 4px; overflow: hidden; border: 1px solid #334155;">
                        <div class="progress-fill" style="width: ${courseProgress}%; height: 100%; background: linear-gradient(90deg, #6366f1, #10b981);"></div>
                    </div>
                    <span style="font-size: 0.85rem; font-weight: 600; color: #10b981; min-width: 40px;">${courseProgress}%</span>
                </div>
                <button class="continue-btn" type="button" onclick="continueCourse('${escapeHtml(titleText).replace(/'/g, "\\'")}', ${course.courseId || course.id})">
                    <i class="fa-solid fa-play"></i> Continue Learning
                </button>
            </div>
        `;
        return card;
    }

    card.innerHTML = `
        <div style="display: flex; flex-direction: column; height: 100%; justify-content: space-between; gap: 14px;">
            <div>
                <h3 style="font-size: 1.1rem; font-weight: 600; color: #f8fafc; margin-bottom: 8px;">${escapeHtml(titleText)}</h3>
                <p style="font-size: 0.875rem; color: #94a3b8; margin-bottom: 12px; line-height: 1.4;">${escapeHtml(shortDesc)}</p>
            </div>
            <div>
                <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 6px;">
                    <span style="color: #94a3b8;">Progress</span>
                    <span style="color: #10b981; font-weight: 600;">${courseProgress}%</span>
                </div>
                <div class="progress-bar" style="margin-bottom: 14px; height: 8px; background: #1e293b; border-radius: 4px; overflow: hidden; border: 1px solid #334155;">
                    <div class="progress-fill" style="width: ${courseProgress}%; height: 100%; background: linear-gradient(90deg, #6366f1, #10b981);"></div>
                </div>
                <button class="continue-btn" style="width: 100%; justify-content: center;" type="button" onclick="continueCourse('${escapeHtml(titleText).replace(/'/g, "\\'")}', ${course.courseId || course.id})">
                    <i class="fa-solid fa-book-reader"></i> ${listType === 'catalog' ? 'View Course' : 'Continue Learning'}
                </button>
            </div>
        </div>
    `;
    return card;
}


async function loadDashboard() {
    const courseList = document.getElementById("dashboard-course-list");
    const enrolledCount = document.getElementById("dashboard-enrolled-count");
    const overallProgress = document.getElementById("dashboard-overall-progress");

    if (!courseList || !enrolledCount || !overallProgress) {
        return;
    }

    loadDashboardProfile();

    try {
        const [enrollmentResponse, courseResponse] = await Promise.all([
            fetch(`${API_BASE_URL}/enrollments/${STUDENT_ID}`),
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
            const progressResponse = await fetch(
                `${API_BASE_URL}/progress/${STUDENT_ID}`
            );
            if (!progressResponse.ok) {
                throw new Error("Unable to load progress summary.");
            }
            progressRecords = await progressResponse.json();
        } catch (error) {
            console.error("Error loading dashboard progress:", error);
        }

        enrolledCount.textContent = String(enrollments.length);
        courseList.innerHTML = "";

        if (enrollments.length === 0) {
            courseList.innerHTML = `
                <p class="dashboard-empty-state">
                    You are not enrolled in any courses yet.
                    <a href="course-catalog.html">Browse courses</a>
                </p>`;
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

        // Load side cards data (Assignments, Quiz attempts)
        try {
            const [assignRes, quizAttemptRes] = await Promise.all([
                fetch(`${API_BASE_URL}/assignments`).catch(() => null),
                fetch(`${API_BASE_URL}/quiz-attempts/${STUDENT_ID}`).catch(() => null)
            ]);

            const assignments = assignRes && assignRes.ok ? await assignRes.json() : [];
            const quizAttempts = quizAttemptRes && quizAttemptRes.ok ? await quizAttemptRes.json() : [];

            const pendingCountElem = document.getElementById("dashboard-pending-count");
            if (pendingCountElem) {
                pendingCountElem.textContent = String(assignments.length);
            }

            // Populate Assignments Widget
            const assignWidget = document.getElementById("dashboard-assignments-list");
            if (assignWidget) {
                if (assignments.length === 0) {
                    assignWidget.innerHTML = `<p class="dashboard-empty-state" style="color: var(--text-muted); font-size: 0.9rem;">No pending assignments.</p>`;
                } else {
                    assignWidget.innerHTML = assignments.slice(0, 3).map(a => `
                        <div style="background: #0f172a; border: 1px solid #334155; border-radius: 8px; padding: 12px 14px; margin-bottom: 8px; display: flex; align-items: center; gap: 12px;">
                            <div style="width: 34px; height: 34px; border-radius: 8px; background: rgba(245, 158, 11, 0.15); color: #f59e0b; display: flex; align-items: center; justify-content: center; font-size: 14px;">
                                <i class="fa-solid fa-file-pen"></i>
                            </div>
                            <div style="flex: 1; min-width: 0;">
                                <div style="font-weight: 600; font-size: 0.9rem; color: #f8fafc; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">${escapeHtml(a.title)}</div>
                                <div style="font-size: 0.78rem; color: #94a3b8;">Max Score: ${a.maxScore || 100} pts</div>
                            </div>
                        </div>
                    `).join('');
                }
            }

            // Populate Recent Grades Widget
            const gradesWidget = document.getElementById("dashboard-grades-list");
            if (gradesWidget) {
                if (quizAttempts.length === 0) {
                    gradesWidget.innerHTML = `<p class="dashboard-empty-state" style="color: var(--text-muted); font-size: 0.9rem;">No grades recorded yet.</p>`;
                } else {
                    gradesWidget.innerHTML = quizAttempts.slice(0, 3).map(q => {
                        let totalM = q.totalMarks || (q.totalQuestions ? q.totalQuestions * 5 : 5);
                        if (q.score > totalM) totalM = 100;
                        let pct = q.percentage != null ? q.percentage : ((q.score / totalM) * 100);
                        return `
                            <div style="background: #0f172a; border: 1px solid #334155; border-radius: 8px; padding: 12px 14px; margin-bottom: 8px; display: flex; align-items: center; justify-content: space-between; gap: 12px;">
                                <div style="display: flex; align-items: center; gap: 12px;">
                                    <div style="width: 34px; height: 34px; border-radius: 8px; background: rgba(16, 185, 129, 0.15); color: #10b981; display: flex; align-items: center; justify-content: center; font-size: 14px;">
                                        <i class="fa-solid fa-award"></i>
                                    </div>
                                    <div>
                                        <div style="font-weight: 600; font-size: 0.9rem; color: #f8fafc;">Quiz #${q.quizId}</div>
                                        <div style="font-size: 0.78rem; color: #94a3b8;">Score: ${q.score}/${totalM}</div>
                                    </div>
                                </div>
                                <span style="font-weight: 700; font-size: 0.9rem; color: #10b981; background: rgba(16, 185, 129, 0.1); padding: 4px 10px; border-radius: 6px;">${pct.toFixed(1)}%</span>
                            </div>
                        `;
                    }).join('');
                }
            }

            // Populate Activity List Widget
            const activityWidget = document.getElementById("dashboard-activity-list");
            if (activityWidget) {
                const activities = [];
                if (quizAttempts.length > 0) {
                    quizAttempts.slice(0, 2).forEach(att => {
                        activities.push({
                            icon: 'fa-brain',
                            color: '#10b981',
                            bg: 'rgba(16, 185, 129, 0.15)',
                            title: `Completed Quiz #${att.quizId}`,
                            subtitle: `Scored ${att.score} points (${att.percentage != null ? att.percentage.toFixed(1) : 100}%)`,
                            date: new Date(att.submittedAt).toLocaleDateString()
                        });
                    });
                }
                if (enrollments.length > 0) {
                    enrollments.slice(0, 2).forEach(enr => {
                        const courseObj = courses.find(c => Number(c.courseId || c.id) === Number(enr.courseId));
                        activities.push({
                            icon: 'fa-book-open',
                            color: '#6366f1',
                            bg: 'rgba(99, 102, 241, 0.15)',
                            title: `Enrolled in ${courseObj ? courseObj.title : 'Course #' + enr.courseId}`,
                            subtitle: 'Course curriculum unlocked',
                            date: new Date(enr.enrolledAt || Date.now()).toLocaleDateString()
                        });
                    });
                }

                if (activities.length === 0) {
                    activityWidget.innerHTML = `<p class="dashboard-empty-state" style="color: var(--text-muted);">No activity recorded yet.</p>`;
                } else {
                    activityWidget.innerHTML = `<div style="display: flex; flex-direction: column; gap: 10px;">` + activities.map(act => `
                        <div style="background: #0f172a; border: 1px solid #334155; border-radius: 8px; padding: 12px 16px; display: flex; align-items: center; justify-content: space-between; gap: 14px;">
                            <div style="display: flex; align-items: center; gap: 14px;">
                                <div style="width: 36px; height: 36px; border-radius: 8px; background: ${act.bg}; color: ${act.color}; display: flex; align-items: center; justify-content: center; font-size: 15px;">
                                    <i class="fa-solid ${act.icon}"></i>
                                </div>
                                <div>
                                    <div style="font-weight: 600; font-size: 0.9rem; color: #f8fafc;">${escapeHtml(act.title)}</div>
                                    <div style="font-size: 0.8rem; color: #94a3b8;">${escapeHtml(act.subtitle)}</div>
                                </div>
                            </div>
                            <span style="font-size: 0.8rem; color: #64748b;">${act.date}</span>
                        </div>
                    `).join('') + `</div>`;
                }
            }

        } catch (widgetErr) {
            console.error("Error loading dashboard widgets:", widgetErr);
        }

        const trackedRecords = progressRecords.filter(record =>
            Number(record.totalLessons) > 0
        );
        const totalLessons = trackedRecords.reduce(
            (total, record) => total + Number(record.totalLessons),
            0
        );
        const completedLessons = trackedRecords.reduce(
            (total, record) => total + Number(record.completedLessons || 0),
            0
        );

        overallProgress.textContent = totalLessons > 0
            ? `${Math.min(100, Math.round(
                completedLessons / totalLessons * 100
            ))}%`
            : "0%";
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

async function loadDashboardProfile() {
    const nameDisplay = document.getElementById("dashboard-name");
    const profileName = document.getElementById("dashboard-profile-name");
    const profileRole = document.getElementById("dashboard-profile-role");
    const profileAvatar = document.getElementById("dashboard-profile-avatar");

    try {
        const response = await fetch(`${API_BASE_URL}/profile/${STUDENT_ID}`);
        if (!response.ok) {
            throw new Error(`Unable to load profile (HTTP ${response.status}).`);
        }

        const profile = await response.json();
        const name = profile.name || "Profile unavailable";
        if (nameDisplay) {
            nameDisplay.textContent = name;
        }
        if (profileName) {
            profileName.textContent = name;
        }
        if (profileRole) {
            profileRole.textContent = profile.role || "Role unavailable";
        }
        if (profileAvatar) {
            profileAvatar.textContent = name.charAt(0).toUpperCase() || "—";
        }
    } catch (error) {
        console.error("Error loading dashboard profile:", error);
        if (nameDisplay) {
            nameDisplay.textContent = "Profile unavailable";
        }
        if (profileName) {
            profileName.textContent = "Profile unavailable";
        }
        if (profileRole) {
            profileRole.textContent = "Role unavailable";
        }
        if (profileAvatar) {
            profileAvatar.textContent = "—";
        }
    }
}


// ========================================
// LOAD ACTIVE COURSES
// ========================================

async function loadCourses() {

    const courseList =
        document.getElementById(
            "course-list"
        );


    if (!courseList) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE_URL}/courses`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load courses"
            );
        }


        const courses =
            await response.json();


        courseList.innerHTML = "";


        const activeCourses = courses.filter(
            course => !course.status ||
                String(course.status).toLowerCase() === "active"
        );

        if (activeCourses.length === 0) {

            courseList.innerHTML = `

                <div class="empty-message">

                    <h3>
                        No courses available
                    </h3>

                    <p>
                        There are currently
                        no active courses.
                    </p>

                </div>

            `;

            return;
        }


        activeCourses.forEach(course =>
            courseList.appendChild(
                createCourseCard(course, null, "catalog")
            )
        );

    }
    catch (error) {

        console.error(
            "Error loading courses:",
            error
        );


        courseList.innerHTML = `

            <div class="empty-message">

                <h3>
                    Unable to load courses
                </h3>

                <p>
                    Please make sure
                    the server is running.
                </p>

            </div>

        `;
    }
}


// ========================================
// LOAD MY COURSES
// ========================================

async function loadMyCourses() {

    const courseList =
        document.getElementById(
            "my-course-list"
        );


    if (!courseList) {
        return;
    }


    try {

        const enrollmentResponse =
            await fetch(
                `${API_BASE_URL}/enrollments/${STUDENT_ID}`
            );


        if (!enrollmentResponse.ok) {

            throw new Error(
                "Failed to load enrollments"
            );
        }


        const enrollments =
            await enrollmentResponse.json();


        const courseResponse =
            await fetch(
                `${API_BASE_URL}/courses`
            );


        if (!courseResponse.ok) {

            throw new Error(
                "Failed to load courses"
            );
        }


        const courses =
            await courseResponse.json();

        const progressResponse = await fetch(
            `${API_BASE_URL}/progress/${STUDENT_ID}`
        );
        if (!progressResponse.ok) {
            throw new Error("Failed to load course progress");
        }
        const progressRecords = await progressResponse.json();


        courseList.innerHTML = "";


        if (enrollments.length === 0) {

            courseList.innerHTML = `

                <div class="empty-message">

                    <h3>
                        You haven't enrolled
                        in any courses yet
                    </h3>

                    <p>
                        Browse available courses
                        and start learning.
                    </p>

                </div>

            `;

            return;
        }


        enrollments.forEach(enrollment => {
            const course = courses.find(item =>
                Number(item.courseId || item.id) === Number(enrollment.courseId)
            );

            if (!course) {
                return;
            }

            const courseProgress = progressRecords.find(item =>
                Number(item.courseId) === Number(course.courseId || course.id)
            );
            courseList.appendChild(
                createCourseCard(course, courseProgress, "enrolled")
            );
        });

    }
    catch (error) {

        console.error(
            "Error loading my courses:",
            error
        );


        courseList.innerHTML = `

            <div class="empty-message">

                <h3>
                    Unable to load your courses
                </h3>

                <p>
                    Please make sure
                    the server is running.
                </p>

            </div>

        `;
    }
}


// ========================================
// LOAD COURSE DETAILS
// ========================================

async function loadCourseDetails() {

    const moduleList =
        document.getElementById(
            "module-list"
        );


    if (!moduleList) {
        return;
    }


    const urlParams =
        new URLSearchParams(
            window.location.search
        );


    const courseId =
        urlParams.get("courseId");


    if (!courseId) {

        document.getElementById(
            "course-title"
        ).textContent =
            "Course not found";


        document.getElementById(
            "course-description"
        ).textContent =
            "No course was selected.";


        moduleList.innerHTML = `

            <div class="empty-message">

                <h3>
                    Course not found
                </h3>

                <p>
                    Please return to My Courses.
                </p>

            </div>

        `;

        return;
    }


    try {

        const courseResponse =
            await fetch(
                `${API_BASE_URL}/courses`
            );


        if (!courseResponse.ok) {

            throw new Error(
                "Failed to load course"
            );
        }


        const courses =
            await courseResponse.json();


        const course =
            courses.find(
                function(item) {

                    return (
                        String(item.courseId || item.id) ===
                        String(courseId)
                    );

                }
            );


        if (!course) {

            throw new Error(
                "Course not found"
            );
        }


        document.getElementById(
            "course-title"
        ).textContent =
            course.title;


        document.getElementById(
            "course-description"
        ).textContent =
            course.description ||
            "No description available.";


        const moduleResponse =
            await fetch(
                `${API_BASE_URL}/modules/course/${courseId}`
            );


        if (!moduleResponse.ok) {

            throw new Error(
                "Failed to load modules"
            );
        }


        const modules =
            await moduleResponse.json();


        moduleList.innerHTML = "";


        if (modules.length === 0) {

            moduleList.innerHTML = `

                <div class="empty-message">

                    <h3>
                        No modules available
                    </h3>

                    <p>
                        This course does not
                        have any modules yet.
                    </p>

                </div>

            `;

            return;
        }


        modules.forEach(
            function(module, index) {

                const moduleCard =
                    document.createElement(
                        "div"
                    );


                moduleCard.className =
                    "module-card";


                moduleCard.innerHTML = `

                    <div
                        class="module-header"
                        onclick="toggleModule(
                            ${module.id}
                        )"
                    >

                        <div class="module-left">

                            <div
                                class="module-number"
                            >
                                ${index + 1}
                            </div>


                            <div
                                class="module-info"
                            >

                                <h3>
                                    ${module.title}
                                </h3>

                                <p>
                                    ${
                                        module.description ||
                                        "No description"
                                    }
                                </p>

                            </div>

                        </div>


                        <span>
                            ▼
                        </span>

                    </div>


                    <div
                        id="lessons-${module.id}"
                        class="lesson-list"
                    >

                        <p class="loading">
                            Click to load lessons...
                        </p>

                    </div>

                `;


                moduleList.appendChild(
                    moduleCard
                );

            }
        );

    }
    catch (error) {

        console.error(
            "Error loading course details:",
            error
        );


        moduleList.innerHTML = `

            <div class="empty-message">

                <h3>
                    Unable to load course
                </h3>

                <p>
                    Please make sure
                    the server is running.
                </p>

            </div>

        `;
    }
}


// ========================================
// TOGGLE MODULE
// ========================================

async function toggleModule(
    moduleId
) {

    const lessonList =
        document.getElementById(
            `lessons-${moduleId}`
        );


    if (!lessonList) {
        return;
    }


    if (
        lessonList.classList.contains(
            "open"
        )
    ) {

        lessonList.classList.remove(
            "open"
        );

        return;
    }


    lessonList.classList.add(
        "open"
    );


    if (
        lessonList.dataset.loaded ===
        "true"
    ) {

        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE_URL}/lessons/module/${moduleId}`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load lessons"
            );
        }


        const lessons =
            await response.json();


        lessonList.innerHTML = "";


        if (lessons.length === 0) {

            lessonList.innerHTML = `

                <p class="loading">
                    No lessons available
                    in this module.
                </p>

            `;

            lessonList.dataset.loaded =
                "true";

            return;
        }


        lessons.forEach(
            function(lesson) {

                const lessonItem =
                    document.createElement(
                        "div"
                    );


                lessonItem.className =
                    "lesson-item";


                lessonItem.onclick =
                    function() {

                        openLesson(
                            lesson.id,
                            moduleId
                        );

                    };


                lessonItem.innerHTML = `

                    <div class="lesson-left">

                        <div
                            class="lesson-icon"
                        >
                            ▶
                        </div>


                        <span
                            class="lesson-title"
                        >
                            ${lesson.title}
                        </span>

                    </div>


                    <span
                        class="lesson-arrow"
                    >
                        →
                    </span>

                `;


                lessonList.appendChild(
                    lessonItem
                );

            }
        );


        lessonList.dataset.loaded =
            "true";

    }
    catch (error) {

        console.error(
            "Error loading lessons:",
            error
        );


        lessonList.innerHTML = `

            <p class="loading">
                Unable to load lessons.
            </p>

        `;
    }
}


// ========================================
// OPEN LESSON
// ========================================

function openLesson(
    lessonId,
    moduleId
) {

    const urlParams =
        new URLSearchParams(
            window.location.search
        );


    const courseId =
        urlParams.get("courseId");


    window.location.href =
        `lesson.html?lessonId=${lessonId}&courseId=${courseId}`;
}


// ========================================
// LOAD LESSON
// ========================================

async function loadLesson() {

    const lessonBody =
        document.getElementById(
            "lesson-body"
        );


    if (!lessonBody) {
        return;
    }


    const urlParams =
        new URLSearchParams(
            window.location.search
        );


    const lessonId =
        urlParams.get("lessonId");


    if (!lessonId) {

        document.getElementById(
            "lesson-title"
        ).textContent =
            "Lesson not found";


        lessonBody.innerHTML = `

            <div class="error-message">

                <h3>
                    Lesson not found
                </h3>

                <p>
                    No lesson was selected.
                </p>

            </div>

        `;

        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE_URL}/lessons/${lessonId}`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load lesson"
            );
        }


        const lesson =
            await response.json();


        document.getElementById(
            "lesson-title"
        ).textContent =
            lesson.title;


        if (
            lesson.content &&
            lesson.content.trim() !== ""
        ) {

            lessonBody.textContent =
                lesson.content;

        } else {

            lessonBody.innerHTML = `

                <div class="error-message">

                    <h3>
                        No lesson content
                    </h3>

                    <p>
                        This lesson does not
                        contain any content yet.
                    </p>

                </div>

            `;

        }

    }
    catch (error) {

        console.error(
            "Error loading lesson:",
            error
        );


        document.getElementById(
            "lesson-title"
        ).textContent =
            "Unable to load lesson";


        lessonBody.innerHTML = `

            <div class="error-message">

                <h3>
                    Unable to load lesson
                </h3>

                <p>
                    Please make sure
                    the server is running.
                </p>

            </div>

        `;
    }
}


// ========================================
// MARK LESSON COMPLETE
// ========================================

async function markLessonComplete() {

    const button =
        document.querySelector(
            ".complete-button"
        );


    if (!button) {
        return;
    }


    const urlParams =
        new URLSearchParams(
            window.location.search
        );


    const lessonId =
        urlParams.get("lessonId");


    const courseId =
        urlParams.get("courseId");


    if (!lessonId || !courseId) {

        alert(
            "Course or lesson information is missing."
        );

        return;
    }


    try {

        button.disabled = true;

        button.textContent =
            "Updating progress...";


        const completionResponse =
            await fetch(
                `${API_BASE_URL}/progress/${STUDENT_ID}/course/${courseId}/lesson/${lessonId}/complete`,
                {
                    method: "POST"
                }
            );


        if (!completionResponse.ok) {

            throw new Error(
                "Failed to update progress"
            );
        }

        const completion =
            await completionResponse.json();
        const progress = completion.progress;
        const completionStatus =
            document.getElementById("lesson-completion-status");

        if (completionStatus && progress) {
            const percentage = Math.max(
                0,
                Math.min(100, Number(progress.progressPercentage) || 0)
            );
            completionStatus.textContent =
                `Course progress: ${progress.completedLessons} of ` +
                `${progress.totalLessons} lessons (${percentage}%).`;
        }

        // ====================================
        // SUCCESS
        // ====================================

        button.textContent =
            completion.alreadyCompleted
                ? "✓ Already Completed"
                : "✓ Lesson Completed";


        button.classList.add(
            "completed"
        );


        alert(completion.alreadyCompleted
            ? "This lesson was already complete. Your progress has not increased."
            : "Lesson completed! Your progress has been updated.");

    }
    catch (error) {

        console.error(
            "Error updating progress:",
            error
        );


        button.disabled = false;

        button.textContent =
            "✓ Mark Lesson Complete";


        alert(
            "Unable to update progress. Please make sure the server is running."
        );
    }
}


// ========================================
// LOAD MY PROGRESS
// ========================================

async function loadMyProgress() {

    const progressList =
        document.getElementById(
            "progress-list"
        );


    if (!progressList) {
        return;
    }


    try {

        const response =
            await fetch(
                `${API_BASE_URL}/progress/${STUDENT_ID}`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load progress"
            );
        }


        const progressData =
            await response.json();


        progressList.innerHTML = "";


        if (progressData.length === 0) {

            progressList.innerHTML = `

                <div class="empty-message">

                    <h3>
                        No progress available
                    </h3>

                    <p>
                        Start learning a course
                        to see your progress here.
                    </p>

                </div>

            `;

            return;
        }


        // ====================================
        // GET COURSE INFORMATION
        // ====================================

        const courseResponse =
            await fetch(
                `${API_BASE_URL}/courses`
            );


        if (!courseResponse.ok) {

            throw new Error(
                "Failed to load courses"
            );
        }


        const courses =
            await courseResponse.json();


        progressData.forEach(
            function(progress) {

                const course =
                    courses.find(
                        function(item) {

                            return (
                                (item.courseId || item.id) ===
                                progress.courseId
                            );

                        }
                    );


                if (!course) {
                    return;
                }


                const percentage =
                    Number(
                        progress.progressPercentage ||
                        0
                    );


                const completed =
                    progress.completedLessons ||
                    0;


                const total =
                    progress.totalLessons ||
                    0;


                const card =
                    document.createElement(
                        "div"
                    );


                card.className =
                    "progress-card";


                card.innerHTML = `

                    <h3>
                        ${course.title}
                    </h3>


                    <div
                        class="progress-info"
                    >

                        <span>
                            Course Progress
                        </span>

                        <span
                            class="progress-percentage"
                        >
                            ${percentage}%
                        </span>

                    </div>


                    <div
                        class="progress-bar"
                    >

                        <div
                            class="progress-fill"
                            style="width: ${percentage}%"
                        ></div>

                    </div>


                    <div
                        class="lesson-details"
                    >

                        <span>
                            ${completed}
                            /
                            ${total}
                            lessons completed
                        </span>

                        <span>
                            ${
                                progress.lastAccessed
                                ? "Recently accessed"
                                : "Not started"
                            }
                        </span>

                    </div>

                `;


                progressList.appendChild(
                    card
                );

            }
        );

    }
    catch (error) {

        console.error(
            "Error loading progress:",
            error
        );


        progressList.innerHTML = `

            <div class="empty-message">

                <h3>
                    Unable to load progress
                </h3>

                <p>
                    Please make sure
                    the server is running.
                </p>

            </div>

        `;
    }
}


// ========================================
// LOGOUT
// ========================================

function setupLogout() {

    const logoutButton =
        document.querySelector(
            ".logout"
        );


    if (!logoutButton) {
        return;
    }


    logoutButton.addEventListener(
        "click",
        function(event) {

            event.preventDefault();


            const confirmLogout =
                confirm(
                    "Are you sure you want to logout?"
                );


            if (confirmLogout) {

                alert(
                    "Logged out successfully!"
                );

            }

        }
    );
}


// ========================================
// PAGE LOAD
// ========================================

document.addEventListener(
    "DOMContentLoaded",
    function() {

        loadCourses();

        loadMyCourses();

        loadDashboard();

        loadCourseDetails();

        loadLesson();

        loadMyProgress();

        setupLogout();

    }
);