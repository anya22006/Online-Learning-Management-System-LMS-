const STUDENT_PROFILE_URL = "http://localhost:8080/api/student/profile/1";

const profileForm = document.getElementById("profile-form");
const profileMessage = document.getElementById("profile-message");
const saveButton = document.getElementById("save-profile");

function showProfileMessage(message, type) {
    profileMessage.textContent = message;
    profileMessage.className = `message ${type}`;
    profileMessage.hidden = false;
}

function displayProfile(profile) {
    document.getElementById("profile-name").value = profile.name ?? "";
    document.getElementById("profile-email").value = profile.email ?? "";
    document.getElementById("profile-role").textContent = profile.role ?? "Not available";
    document.getElementById("profile-status").textContent = profile.status ?? "Not available";
}

async function loadProfile() {
    try {
        const response = await fetch(STUDENT_PROFILE_URL);
        if (!response.ok) {
            throw new Error(`Could not load profile (HTTP ${response.status}).`);
        }
        displayProfile(await response.json());
    } catch (error) {
        showProfileMessage(error.message || "Could not load your profile. Please try again.", "error");
    }
}

profileForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    profileMessage.hidden = true;
    saveButton.disabled = true;

    const request = {
        name: document.getElementById("profile-name").value.trim(),
        email: document.getElementById("profile-email").value.trim()
    };

    try {
        const response = await fetch(STUDENT_PROFILE_URL, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(request)
        });

        if (!response.ok) {
            const problem = await response.json().catch(() => null);
            throw new Error(problem?.detail || problem?.message || `Could not save profile (HTTP ${response.status}).`);
        }

        displayProfile(await response.json());
        showProfileMessage("Your profile was saved successfully.", "success");
    } catch (error) {
        showProfileMessage(error.message || "Could not save your profile. Please try again.", "error");
    } finally {
        saveButton.disabled = false;
    }
});

loadProfile();
