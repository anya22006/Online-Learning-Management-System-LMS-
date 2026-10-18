let currentStudentId = 1;

const profileForm = document.getElementById("profile-form");
const profileMessage = document.getElementById("profile-message");
const saveButton = document.getElementById("save-profile");

function showProfileMessage(message, type) {
    if (!profileMessage) return;
    profileMessage.textContent = message;
    profileMessage.className = `message ${type}`;
    profileMessage.hidden = false;
}

function displayProfile(profile) {
    const nameEl = document.getElementById("profile-name");
    const emailEl = document.getElementById("profile-email");
    const roleEl = document.getElementById("profile-role");
    const statusEl = document.getElementById("profile-status");

    if (nameEl) nameEl.value = profile.name ?? "";
    if (emailEl) emailEl.value = profile.email ?? "";
    if (roleEl) roleEl.textContent = profile.role ?? "Student";
    if (statusEl) statusEl.textContent = profile.status ?? "Active";
}

async function loadProfile() {
    try {
        const currentRes = await fetch("/api/student/current");
        if (currentRes.ok) {
            const currentUser = await currentRes.json();
            if (currentUser && currentUser.id) {
                currentStudentId = currentUser.id;
            }
        }
    } catch (e) {
        console.warn("Could not fetch current student, fallback to ID:", currentStudentId);
    }

    try {
        const response = await fetch(`/api/student/profile/${currentStudentId}`);
        if (!response.ok) {
            throw new Error(`Could not load profile (HTTP ${response.status}).`);
        }
        displayProfile(await response.json());
    } catch (error) {
        showProfileMessage(error.message || "Could not load your profile. Please try again.", "error");
    }
}

if (profileForm) {
    profileForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (profileMessage) profileMessage.hidden = true;
        if (saveButton) saveButton.disabled = true;

        const request = {
            name: document.getElementById("profile-name").value.trim(),
            email: document.getElementById("profile-email").value.trim()
        };

        try {
            const response = await fetch(`/api/student/profile/${currentStudentId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(request)
            });

            if (!response.ok) {
                const problem = await response.json().catch(() => null);
                throw new Error(problem?.detail || problem?.message || `Could not save profile (HTTP ${response.status}).`);
            }

            const updated = await response.json();
            displayProfile(updated);
            sessionStorage.setItem("currentStudent", JSON.stringify({
                authenticated: true,
                id: currentStudentId,
                studentId: currentStudentId,
                name: updated.name,
                email: updated.email,
                role: updated.role
            }));
            showProfileMessage("Your profile was saved successfully.", "success");
        } catch (error) {
            showProfileMessage(error.message || "Could not save your profile. Please try again.", "error");
        } finally {
            if (saveButton) saveButton.disabled = false;
        }
    });
}

loadProfile();
