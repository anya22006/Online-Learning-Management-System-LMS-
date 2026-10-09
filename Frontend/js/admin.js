// ========================================
// ADMIN LMS JAVASCRIPT
// ========================================

const API_BASE = "http://localhost:8080/api/admin";

document.addEventListener("DOMContentLoaded", () => {
    initAdminDashboard();
});

function initAdminDashboard() {
    loadMetrics();
    loadUsers();
    loadCourses();
    loadActivityLogs();
    loadSettings();
}

// 1. Load Admin Summary Metrics
async function loadMetrics() {
    try {
        const res = await fetch(`${API_BASE}/metrics`);
        if (!res.ok) return;
        const data = await res.json();

        const studentCountEl = document.getElementById("metricTotalStudents");
        const instCountEl = document.getElementById("metricTotalInstructors");
        const courseCountEl = document.getElementById("metricTotalCourses");
        const publishedEl = document.getElementById("metricPublishedCourses");
        const pendingEl = document.getElementById("metricPendingCourses");
        const enrollCountEl = document.getElementById("metricTotalEnrollments");

        if (studentCountEl) studentCountEl.textContent = data.totalStudents || 0;
        if (instCountEl) instCountEl.textContent = data.totalInstructors || 0;
        if (courseCountEl) courseCountEl.textContent = data.totalCourses || 0;
        if (publishedEl) publishedEl.textContent = data.publishedCourses || 0;
        if (pendingEl) pendingEl.textContent = data.pendingCourses || 0;
        if (enrollCountEl) enrollCountEl.textContent = data.totalEnrollments || 0;
    } catch (err) {
        console.error("Failed to load admin metrics:", err);
    }
}

// 2. User Management Functions
let allUsersData = [];

async function loadUsers() {
    const tbody = document.getElementById("userTableBody");
    if (!tbody) return;

    try {
        const res = await fetch(`${API_BASE}/users`);
        if (!res.ok) throw new Error("Failed to fetch users");
        allUsersData = await res.json();
        renderUserTable(allUsersData);
    } catch (err) {
        console.error("Error loading users:", err);
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-muted);">Failed to load users.</td></tr>`;
    }
}

function renderUserTable(users) {
    const tbody = document.getElementById("userTableBody");
    if (!tbody) return;

    if (users.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-muted);">No users found.</td></tr>`;
        return;
    }

    tbody.innerHTML = users.map(user => `
        <tr>
            <td>#${user.id}</td>
            <td><strong>${escapeHtml(user.name)}</strong></td>
            <td>${escapeHtml(user.email)}</td>
            <td>
                <span class="badge ${user.role === 'ADMIN' ? 'badge-admin' : user.role === 'INSTRUCTOR' ? 'badge-instructor' : 'badge-published'}">
                    ${user.role}
                </span>
            </td>
            <td>
                <span style="color: ${user.status === 'ACTIVE' || user.status === 'Active' ? 'var(--accent-emerald)' : '#ef4444'}; font-weight:600;">
                    ${user.status.toUpperCase()}
                </span>
            </td>
            <td style="display: flex; gap: 8px;">
                <button class="btn btn-secondary btn-sm" onclick="openEditUserModal(${user.id}, '${escapeHtml(user.name)}', '${escapeHtml(user.email)}', '${user.role}', '${user.status}')">
                    <i class="fa-solid fa-pen-to-square"></i> Edit
                </button>
                ${user.role !== 'ADMIN' ? `
                    <button class="btn btn-danger btn-sm" onclick="deleteUser('${user.role}', ${user.id})">
                        <i class="fa-solid fa-trash"></i> Delete
                    </button>
                ` : ''}
            </td>
        </tr>
    `).join("");
}

function filterUsers() {
    const searchVal = (document.getElementById("userSearchInput")?.value || "").toLowerCase();
    const roleVal = document.getElementById("userRoleFilter")?.value || "";

    const filtered = allUsersData.filter(user => {
        const matchesSearch = user.name.toLowerCase().includes(searchVal) || user.email.toLowerCase().includes(searchVal);
        const matchesRole = !roleVal || user.role.toUpperCase() === roleVal.toUpperCase();
        return matchesSearch && matchesRole;
    });

    renderUserTable(filtered);
}

function openCreateUserModal() {
    const modal = document.getElementById("userModal");
    if (!modal) return;
    document.getElementById("userModalTitle").textContent = "Create New User Account";
    document.getElementById("userIdInput").value = "";
    document.getElementById("userNameInput").value = "";
    document.getElementById("userEmailInput").value = "";
    document.getElementById("userRoleSelect").value = "STUDENT";
    document.getElementById("userStatusSelect").value = "ACTIVE";
    modal.style.display = "flex";
}

function openEditUserModal(id, name, email, role, status) {
    const modal = document.getElementById("userModal");
    if (!modal) return;
    document.getElementById("userModalTitle").textContent = `Edit User #${id}`;
    document.getElementById("userIdInput").value = id;
    document.getElementById("userNameInput").value = name;
    document.getElementById("userEmailInput").value = email;
    document.getElementById("userRoleSelect").value = role;
    document.getElementById("userStatusSelect").value = status.toUpperCase();
    modal.style.display = "flex";
}

function closeUserModal() {
    const modal = document.getElementById("userModal");
    if (modal) modal.style.display = "none";
}

async function handleSaveUser(e) {
    e.preventDefault();
    const id = document.getElementById("userIdInput").value;
    const name = document.getElementById("userNameInput").value.trim();
    const email = document.getElementById("userEmailInput").value.trim();
    const role = document.getElementById("userRoleSelect").value;
    const status = document.getElementById("userStatusSelect").value;

    try {
        let res;
        if (id) {
            // Update
            res = await fetch(`${API_BASE}/users/${role}/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name, email, status })
            });
        } else {
            // Create
            res = await fetch(`${API_BASE}/users`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name, email, role, status })
            });
        }

        if (res.ok) {
            alert("User account saved successfully!");
            closeUserModal();
            loadUsers();
            loadMetrics();
            loadActivityLogs();
        } else {
            alert("Failed to save user account.");
        }
    } catch (err) {
        console.error("Error saving user:", err);
    }
}

async function deleteUser(role, id) {
    if (!confirm(`Are you sure you want to delete user #${id}? This action cannot be undone.`)) return;

    try {
        const res = await fetch(`${API_BASE}/users/${role}/${id}`, { method: "DELETE" });
        if (res.ok) {
            alert("User deleted successfully!");
            loadUsers();
            loadMetrics();
            loadActivityLogs();
        } else {
            alert("Failed to delete user.");
        }
    } catch (err) {
        console.error("Error deleting user:", err);
    }
}

// 3. Course Management Functions
let allCoursesData = [];

async function loadCourses() {
    const tbody = document.getElementById("adminCourseTableBody");
    if (!tbody) return;

    try {
        const res = await fetch(`${API_BASE}/courses`);
        if (!res.ok) throw new Error("Failed to fetch courses");
        allCoursesData = await res.json();
        renderAdminCourseTable(allCoursesData);
    } catch (err) {
        console.error("Error loading admin courses:", err);
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted);">Failed to load courses.</td></tr>`;
    }
}

function renderAdminCourseTable(courses) {
    const tbody = document.getElementById("adminCourseTableBody");
    if (!tbody) return;

    if (courses.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted);">No courses created yet.</td></tr>`;
        return;
    }

    tbody.innerHTML = courses.map(c => `
        <tr>
            <td>#${c.courseId}</td>
            <td><strong>${escapeHtml(c.title)}</strong></td>
            <td>${escapeHtml(c.category)}</td>
            <td>${escapeHtml(c.instructorName || 'Instructor #' + c.instructorId)}</td>
            <td><strong style="color: var(--accent-indigo);">${c.enrollmentCount || 0}</strong></td>
            <td>
                <span class="badge ${c.status === 'PUBLISHED' ? 'badge-published' : 'badge-draft'}">
                    ${c.status}
                </span>
            </td>
            <td style="display: flex; gap: 8px;">
                <button class="btn ${c.status === 'DRAFT' ? 'btn-success' : 'btn-secondary'} btn-sm" onclick="toggleCourseStatus(${c.courseId}, '${c.status}')">
                    ${c.status === 'DRAFT' ? '<i class="fa-solid fa-paper-plane"></i> Publish' : '<i class="fa-solid fa-box-archive"></i> Unpublish'}
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteAdminCourse(${c.courseId})">
                    <i class="fa-solid fa-trash"></i> Delete
                </button>
            </td>
        </tr>
    `).join("");
}

function filterCourses() {
    const searchVal = (document.getElementById("courseSearchInput")?.value || "").toLowerCase();
    const statusVal = document.getElementById("courseStatusFilter")?.value || "";

    const filtered = allCoursesData.filter(c => {
        const matchesSearch = c.title.toLowerCase().includes(searchVal) || c.category.toLowerCase().includes(searchVal);
        const matchesStatus = !statusVal || c.status.toUpperCase() === statusVal.toUpperCase();
        return matchesSearch && matchesStatus;
    });

    renderAdminCourseTable(filtered);
}

async function toggleCourseStatus(id, currentStatus) {
    const newStatus = currentStatus === "DRAFT" ? "PUBLISHED" : "DRAFT";
    try {
        const res = await fetch(`${API_BASE}/courses/${id}/status`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ status: newStatus })
        });
        if (res.ok) {
            loadCourses();
            loadMetrics();
            loadActivityLogs();
        }
    } catch (err) {
        console.error("Error toggling course status:", err);
    }
}

async function deleteAdminCourse(id) {
    if (!confirm("Are you sure you want to delete this course? All modules and quizzes will be removed.")) return;
    try {
        const res = await fetch(`${API_BASE}/courses/${id}`, { method: "DELETE" });
        if (res.ok) {
            alert("Course deleted successfully!");
            loadCourses();
            loadMetrics();
            loadActivityLogs();
        }
    } catch (err) {
        console.error("Error deleting course:", err);
    }
}

// 4. Activity Logs
async function loadActivityLogs() {
    const tbody = document.getElementById("activityLogTableBody");
    if (!tbody) return;

    try {
        const res = await fetch(`${API_BASE}/activity-logs`);
        if (!res.ok) return;
        const logs = await res.json();

        tbody.innerHTML = logs.map(log => `
            <tr>
                <td>${new Date(log.timestamp).toLocaleString()}</td>
                <td><strong>${escapeHtml(log.userName)}</strong></td>
                <td><span class="badge badge-published" style="background:#4f46e5;">${escapeHtml(log.action)}</span></td>
                <td>${escapeHtml(log.affectedResource)}</td>
            </tr>
        `).join("");
    } catch (err) {
        console.error("Error loading activity logs:", err);
    }
}

// 5. System Settings
async function loadSettings() {
    const form = document.getElementById("systemSettingsForm");
    if (!form) return;

    try {
        const res = await fetch(`${API_BASE}/settings`);
        if (!res.ok) return;
        const settings = await res.json();

        if (document.getElementById("settingPlatformName")) document.getElementById("settingPlatformName").value = settings.platformName || "LMS Learning Management System";
        if (document.getElementById("settingDefaultRole")) document.getElementById("settingDefaultRole").value = settings.defaultRole || "Student";
        if (document.getElementById("settingEmailNotif")) document.getElementById("settingEmailNotif").value = settings.emailNotifications || "Enabled";
    } catch (err) {
        console.error("Error loading settings:", err);
    }
}

async function handleSaveSettings(e) {
    e.preventDefault();
    const platformName = document.getElementById("settingPlatformName").value;
    const defaultRole = document.getElementById("settingDefaultRole").value;
    const emailNotifications = document.getElementById("settingEmailNotif").value;

    try {
        const res = await fetch(`${API_BASE}/settings`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ platformName, defaultRole, emailNotifications })
        });

        if (res.ok) {
            alert("Platform settings saved successfully!");
            loadActivityLogs();
        }
    } catch (err) {
        console.error("Error saving settings:", err);
    }
}

// 6. Export Reports CSV
function downloadEnrollmentReport() {
    window.location.href = `${API_BASE}/reports/enrollments/csv`;
}

// 7. Admin Direct Enrollment
function openAdminEnrollModal() {
    const modal = document.getElementById("adminEnrollModal");
    const studentSelect = document.getElementById("enrollStudentSelect");
    const courseSelect = document.getElementById("enrollCourseSelect");
    if (!modal || !studentSelect || !courseSelect) return;

    const students = allUsersData.filter(u => u.role === "STUDENT");
    studentSelect.innerHTML = students.map(s => `<option value="${s.id}">${escapeHtml(s.name)} (#${s.id})</option>`).join("");

    courseSelect.innerHTML = allCoursesData.map(c => `<option value="${c.courseId}">${escapeHtml(c.title)} (#${c.courseId})</option>`).join("");

    modal.style.display = "flex";
}

function closeAdminEnrollModal() {
    const modal = document.getElementById("adminEnrollModal");
    if (modal) modal.style.display = "none";
}

async function handleAdminEnroll(e) {
    e.preventDefault();
    const studentId = Number(document.getElementById("enrollStudentSelect").value);
    const courseId = Number(document.getElementById("enrollCourseSelect").value);

    try {
        const res = await fetch(`${API_BASE}/enrollments`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ studentId, courseId })
        });
        if (res.ok) {
            alert("Student enrolled successfully!");
            closeAdminEnrollModal();
            loadCourses();
            loadMetrics();
            loadActivityLogs();
        } else {
            alert("Failed to enroll student.");
        }
    } catch (err) {
        console.error("Error creating enrollment:", err);
    }
}

function escapeHtml(text) {
    if (!text) return "";
    return String(text).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
