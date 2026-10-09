const API_BASE = '/api/instructor';
const currentUserId = '2'; // Instructor ID

document.addEventListener('DOMContentLoaded', () => {
    // Determine active page
    if (document.getElementById('courseTableBody')) {
        loadCourses();
    }
    if (document.getElementById('submissionTableBody')) {
        loadSubmissions();
    }
    if (document.getElementById('assignCourseSelect')) {
        populateCourseDropdowns();
        loadAssignmentsAndQuizzes();
    }
    const createForm = document.getElementById('createCourseForm');
    if (createForm) {
        createForm.addEventListener('submit', handleCreateCourse);
    }
    const gradeForm = document.getElementById('gradeForm');
    if (gradeForm) {
        gradeForm.addEventListener('submit', handleGradeSubmission);
    }
    const assignForm = document.getElementById('createAssignmentForm');
    if (assignForm) {
        assignForm.addEventListener('submit', handleCreateAssignment);
    }
    const quizForm = document.getElementById('createQuizForm');
    if (quizForm) {
        quizForm.addEventListener('submit', handleCreateQuiz);
    }
});

// Populate Course Dropdowns in Forms
async function populateCourseDropdowns() {
    try {
        const res = await fetch(`${API_BASE}/courses`, {
            headers: { 'X-User-Id': currentUserId }
        });
        const courses = await res.json();
        
        const assignSelect = document.getElementById('assignCourseSelect');
        const quizSelect = document.getElementById('quizCourseSelect');
        
        const selectedCourseId = new URLSearchParams(window.location.search).get('courseId');
        
        const optionsHtml = courses.map(c => `
            <option value="${c.courseId}" ${selectedCourseId && selectedCourseId == c.courseId ? 'selected' : ''}>
                ${escapeHtml(c.title)}
            </option>
        `).join('');
        
        if (assignSelect) assignSelect.innerHTML = optionsHtml || '<option value="">No courses available</option>';
        if (quizSelect) quizSelect.innerHTML = optionsHtml || '<option value="">No courses available</option>';
    } catch (err) {
        console.error('Failed to load courses for dropdown:', err);
    }
}

// Load Courses
async function loadCourses() {
    try {
        const res = await fetch(`${API_BASE}/courses`, {
            headers: { 'X-User-Id': currentUserId }
        });
        const courses = await res.json();
        
        const countElem = document.getElementById('metricTotalCourses');
        if (countElem) countElem.innerText = courses.length;

        const activeElem = document.getElementById('metricActiveCourses');
        if (activeElem) {
            activeElem.innerText = courses.filter(c => c.status === 'PUBLISHED').length;
        }

        const tbody = document.getElementById('courseTableBody');
        if (!tbody) return;

        if (courses.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color: var(--text-muted);">No courses created yet. Click "Create Course" to get started.</td></tr>`;
            return;
        }

        tbody.innerHTML = courses.map(course => `
            <tr>
                <td><strong>${escapeHtml(course.title)}</strong></td>
                <td>${escapeHtml(course.category)}</td>
                <td>
                    <span class="badge ${course.status === 'PUBLISHED' ? 'badge-published' : 'badge-draft'}">
                        ${course.status}
                    </span>
                </td>
                <td>${new Date(course.createdAt).toLocaleDateString()}</td>
                <td style="display: flex; gap: 8px; flex-wrap: wrap;">
                    <a href="create-assignment.html?courseId=${course.courseId}" class="btn btn-primary" style="font-size: 12px; padding: 6px 12px;">+ Quiz/Assignment</a>
                    <button class="btn ${course.status === 'DRAFT' ? 'btn-success' : 'btn-secondary'}" 
                            onclick="toggleStatus(${course.courseId}, '${course.status}')">
                        ${course.status === 'DRAFT' ? 'Publish' : 'Unpublish'}
                    </button>
                    <button class="btn btn-danger" onclick="deleteCourse(${course.courseId})">Delete</button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Failed to load courses:', err);
    }
}

// Create Course
async function handleCreateCourse(e) {
    e.preventDefault();
    const payload = {
        title: document.getElementById('title').value,
        category: document.getElementById('category').value,
        description: document.getElementById('description').value,
        syllabus: document.getElementById('syllabus').value
    };

    try {
        const res = await fetch(`${API_BASE}/courses`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-Id': currentUserId
            },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            alert('Course created successfully!');
            window.location.href = 'manage-courses.html';
        } else {
            alert('Failed to create course');
        }
    } catch (err) {
        console.error('Error creating course:', err);
    }
}

// Create Assignment
async function handleCreateAssignment(e) {
    e.preventDefault();
    const courseId = document.getElementById('assignCourseSelect').value;
    const payload = {
        courseId: parseInt(courseId),
        title: document.getElementById('assignTitle').value,
        instructions: document.getElementById('assignInstructions').value,
        dueDate: document.getElementById('assignDueDate').value,
        maxScore: parseInt(document.getElementById('assignMaxScore').value || 100)
    };

    try {
        const res = await fetch(`${API_BASE}/assignments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-Id': currentUserId
            },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            alert('Assignment created and published successfully!');
            document.getElementById('createAssignmentForm').reset();
            loadAssignmentsAndQuizzes();
        } else {
            alert('Failed to create assignment');
        }
    } catch (err) {
        console.error('Error creating assignment:', err);
    }
}

// Dynamic Quiz Question Management
function addQuestionField() {
    const container = document.getElementById('questionsContainer');
    if (!container) return;

    const questionBox = document.createElement('div');
    questionBox.className = 'quiz-question-box';
    questionBox.style.cssText = 'background: #0f172a; padding: 20px; border-radius: 8px; margin-bottom: 16px; border: 1px solid var(--border-color); position: relative;';
    questionBox.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
            <label style="font-weight: 600; font-size: 1.05rem;">Question <span class="q-num">${document.querySelectorAll('.quiz-question-box').length + 1}</span> Text</label>
            <button type="button" class="btn btn-danger btn-sm" onclick="removeQuestionField(this)" style="padding: 4px 10px; font-size: 0.85rem; background: #ef4444; color: #fff; border: none; border-radius: 4px; cursor: pointer;">🗑️ Remove</button>
        </div>
        <div class="form-group">
            <input type="text" class="form-control q-text" placeholder="e.g. Enter question text here..." required>
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
            <input type="text" class="form-control q-opt-a" placeholder="Option A" required>
            <input type="text" class="form-control q-opt-b" placeholder="Option B" required>
            <input type="text" class="form-control q-opt-c" placeholder="Option C">
            <input type="text" class="form-control q-opt-d" placeholder="Option D">
        </div>
        <div class="form-group" style="margin-top: 12px;">
            <label>Correct Option</label>
            <select class="form-control q-correct" required>
                <option value="A">Option A</option>
                <option value="B">Option B</option>
                <option value="C">Option C</option>
                <option value="D">Option D</option>
            </select>
        </div>
    `;
    container.appendChild(questionBox);
    updateQuestionNumbers();
}

function removeQuestionField(btn) {
    const boxes = document.querySelectorAll('.quiz-question-box');
    if (boxes.length <= 1) {
        alert('A quiz must have at least 1 question.');
        return;
    }
    const box = btn.closest('.quiz-question-box');
    if (box) {
        box.remove();
        updateQuestionNumbers();
    }
}

function updateQuestionNumbers() {
    const boxes = document.querySelectorAll('.quiz-question-box');
    boxes.forEach((box, index) => {
        const numSpan = box.querySelector('.q-num');
        if (numSpan) {
            numSpan.textContent = index + 1;
        }
    });
}

// Create Quiz
async function handleCreateQuiz(e) {
    e.preventDefault();
    const courseId = document.getElementById('quizCourseSelect').value;

    const questions = [];
    document.querySelectorAll('.quiz-question-box').forEach(box => {
        questions.push({
            questionText: box.querySelector('.q-text').value,
            optionA: box.querySelector('.q-opt-a').value,
            optionB: box.querySelector('.q-opt-b').value,
            optionC: box.querySelector('.q-opt-c').value,
            optionD: box.querySelector('.q-opt-d').value,
            correctOption: box.querySelector('.q-correct').value
        });
    });

    const payload = {
        courseId: parseInt(courseId),
        title: document.getElementById('quizTitle').value,
        instructions: document.getElementById('quizInstructions').value,
        timeLimitMinutes: parseInt(document.getElementById('quizTimeLimit').value || 30),
        questions: questions
    };

    try {
        const res = await fetch(`${API_BASE}/quizzes`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-Id': currentUserId
            },
            body: JSON.stringify(payload)
        });

        if (res.ok) {
            alert('Quiz created successfully!');
            document.getElementById('createQuizForm').reset();
            // Reset questions list back to 1 box
            const container = document.getElementById('questionsContainer');
            if (container) {
                const boxes = container.querySelectorAll('.quiz-question-box');
                for (let i = 1; i < boxes.length; i++) {
                    boxes[i].remove();
                }
                updateQuestionNumbers();
            }
            loadAssignmentsAndQuizzes();
        } else {
            alert('Failed to create quiz');
        }
    } catch (err) {
        console.error('Error creating quiz:', err);
    }
}

// Load Assignments & Quizzes List
async function loadAssignmentsAndQuizzes() {
    try {
        const [assignRes, quizRes] = await Promise.all([
            fetch(`${API_BASE}/assignments`),
            fetch(`${API_BASE}/quizzes`)
        ]);

        const assignments = await assignRes.json();
        const quizzes = await quizRes.json();

        const assignContainer = document.getElementById('assignmentsListContainer');
        const quizContainer = document.getElementById('quizzesListContainer');

        if (assignContainer) {
            assignContainer.innerHTML = assignments.length === 0 
                ? '<p style="color: var(--text-muted);">No assignments published yet.</p>'
                : assignments.map(a => `
                    <div style="background: #0f172a; padding: 14px 18px; border-radius: 8px; margin-bottom: 10px; border: 1px solid var(--border-color);">
                        <strong style="font-size: 15px;">${escapeHtml(a.title)}</strong>
                        <div style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">Max Score: ${a.maxScore} pts</div>
                    </div>
                `).join('');
        }

        if (quizContainer) {
            quizContainer.innerHTML = quizzes.length === 0
                ? '<p style="color: var(--text-muted);">No quizzes published yet.</p>'
                : quizzes.map(q => `
                    <div style="background: #0f172a; padding: 14px 18px; border-radius: 8px; margin-bottom: 10px; border: 1px solid var(--border-color);">
                        <strong style="font-size: 15px;">${escapeHtml(q.title)}</strong>
                        <div style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">Time Limit: ${q.timeLimitMinutes} min</div>
                    </div>
                `).join('');
        }
    } catch (err) {
        console.error('Error loading assessments:', err);
    }
}

// Toggle Course Status (Draft <-> Published)
async function toggleStatus(courseId, currentStatus) {
    const newStatus = currentStatus === 'DRAFT' ? 'PUBLISHED' : 'DRAFT';
    try {
        const res = await fetch(`${API_BASE}/courses/${courseId}/status?status=${newStatus}`, {
            method: 'PATCH',
            headers: { 'X-User-Id': currentUserId }
        });
        if (res.ok) {
            loadCourses();
        }
    } catch (err) {
        console.error('Error updating status:', err);
    }
}

// Delete Course
async function deleteCourse(courseId) {
    if (!confirm('Are you sure you want to delete this course?')) return;
    try {
        const res = await fetch(`${API_BASE}/courses/${courseId}`, {
            method: 'DELETE',
            headers: { 'X-User-Id': currentUserId }
        });
        if (res.ok) {
            loadCourses();
        }
    } catch (err) {
        console.error('Error deleting course:', err);
    }
}

// Load Submissions
async function loadSubmissions() {
    try {
        const res = await fetch(`${API_BASE}/submissions`);
        const submissions = await res.json();

        const pendingElem = document.getElementById('metricPendingSubmissions');
        if (pendingElem) pendingElem.innerText = submissions.length;

        const tbody = document.getElementById('submissionTableBody');
        if (!tbody) return;

        if (submissions.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color: var(--text-muted);">No student submissions available for grading.</td></tr>`;
            return;
        }

        tbody.innerHTML = submissions.map(sub => `
            <tr>
                <td>#${sub.submissionId}</td>
                <td><strong>${escapeHtml(sub.studentName || 'Student')}</strong></td>
                <td>${escapeHtml(sub.content)}</td>
                <td>${new Date(sub.submittedAt).toLocaleDateString()}</td>
                <td>
                    <button class="btn btn-primary" onclick="openGradeModal(${sub.submissionId}, '${escapeHtml(sub.studentName || 'Student')}')">
                        Grade Work
                    </button>
                </td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Failed to load submissions:', err);
    }
}

// Open Grade Modal / Form
function openGradeModal(submissionId, studentName) {
    document.getElementById('modalSubmissionId').value = submissionId;
    document.getElementById('modalStudentName').innerText = studentName;
    document.getElementById('gradeSection').style.display = 'block';
    window.scrollTo({ top: document.getElementById('gradeSection').offsetTop, behavior: 'smooth' });
}

// Grade Submission
async function handleGradeSubmission(e) {
    e.preventDefault();
    const submissionId = document.getElementById('modalSubmissionId').value;
    const score = parseInt(document.getElementById('score').value);
    const maxScore = parseInt(document.getElementById('maxScore').value || 100);
    const feedback = document.getElementById('feedback').value;

    if (score < 0 || score > maxScore) {
        alert(`Score must be between 0 and maximum marks (${maxScore})`);
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/submissions/${submissionId}/grade`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-Id': currentUserId
            },
            body: JSON.stringify({ submissionId, score, maxScore, feedback })
        });

        if (res.ok) {
            alert('Grade and feedback saved successfully!');
            document.getElementById('gradeSection').style.display = 'none';
            loadSubmissions();
        } else {
            alert('Failed to save grade');
        }
    } catch (err) {
        console.error('Error grading submission:', err);
    }
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
