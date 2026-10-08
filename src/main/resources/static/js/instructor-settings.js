const INSTRUCTOR_PROFILE_URL = "http://localhost:8080/api/instructor/profile";

const profileForm = document.getElementById("instructorProfileForm");
const profileMessage = document.getElementById("profileMessage");
const saveBtn = document.getElementById("saveProfileBtn");

function showMessage(text, isSuccess) {
    profileMessage.textContent = text;
    profileMessage.style.display = "block";
    profileMessage.style.backgroundColor = isSuccess ? "#d1fae5" : "#fee2e2";
    profileMessage.style.color = isSuccess ? "#065f46" : "#991b1b";
    profileMessage.style.border = isSuccess ? "1px solid #a7f3d0" : "1px solid #fca5a5";
}

function displayInstructorProfile(profile) {
    document.getElementById("instructorName").value = profile.name || "";
    document.getElementById("instructorEmail").value = profile.email || "";
    document.getElementById("instructorDepartment").value = profile.department || "";
    document.getElementById("instructorRole").textContent = profile.role || "Instructor";
    document.getElementById("instructorStatus").textContent = profile.status || "Active";
}

async function loadInstructorProfile() {
    try {
        const response = await fetch(INSTRUCTOR_PROFILE_URL, {
            headers: { "X-User-Id": "2" }
        });
        if (!response.ok) {
            throw new Error(`Failed to load profile (HTTP ${response.status})`);
        }
        const profile = await response.json();
        displayInstructorProfile(profile);
    } catch (error) {
        showMessage(error.message || "Could not load instructor profile.", false);
    }
}

profileForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    saveBtn.disabled = true;
    saveBtn.textContent = "Saving...";

    const payload = {
        name: document.getElementById("instructorName").value.trim(),
        email: document.getElementById("instructorEmail").value.trim(),
        department: document.getElementById("instructorDepartment").value.trim()
    };

    try {
        const response = await fetch(INSTRUCTOR_PROFILE_URL, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "X-User-Id": "2"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(`Failed to update profile (HTTP ${response.status})`);
        }

        const updated = await response.json();
        displayInstructorProfile(updated);
        showMessage("Instructor profile updated successfully!", true);
    } catch (error) {
        showMessage(error.message || "Failed to update profile. Please try again.", false);
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = "Save Profile Changes";
    }
});

loadInstructorProfile();
