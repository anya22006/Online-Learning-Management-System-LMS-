const API_BASE = '/api/instructor';
const currentUserId = '2'; // Instructor ID

document.addEventListener('DOMContentLoaded', () => {
    // Determine active page
    if (document.getElementById('courseTableBody') || document.getElementById('manageCourseTableBody')) {
        loadCourses();
    }
    if (document.getElementById('submissionTableBody')) {
        loadSubmissions();
    }
    if (document.getElementById('assignCourseSelect') || document.getElementById('assignmentCourseId') || document.getElementById('quizCourseId')) {
        populateCourseDropdowns();
        loadAssignmentsAndQuizzes();
    }
    if (document.getElementById('questionsContainer')) {
        const container = document.getElementById('questionsContainer');
        if (container.children.length === 0) {
            addQuestionField();
        }
    }
    const quizTotalMarksInput = document.getElementById('quizTotalMarks');
    if (quizTotalMarksInput) {
        quizTotalMarksInput.addEventListener('input', () => {
            quizTotalMarksInput.dataset.autoSync = "false";
        });
    }
    const createForm = document.getElementById('createCourseForm');
    if (createForm) {
        createForm.addEventListener('submit', handleCreateCourse);
    }
    const gradeForm = document.getElementById('gradeForm');
    if (gradeForm) {
        gradeForm.addEventListener('submit', handleGradeSubmission);
    }
    const editQuizGradeForm = document.getElementById('editQuizGradeForm');
    if (editQuizGradeForm) {
        editQuizGradeForm.addEventListener('submit', handleEditQuizGradeSubmission);
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
    const assignSelect = document.getElementById('assignmentCourseId') || document.getElementById('assignCourseSelect');
    const quizSelect = document.getElementById('quizCourseId') || document.getElementById('quizCourseSelect');
    
    if (assignSelect) assignSelect.innerHTML = '<option value="">Loading courses...</option>';
    if (quizSelect) quizSelect.innerHTML = '<option value="">Loading courses...</option>';

    try {
        const res = await fetch(`${API_BASE}/courses`, {
            headers: { 'X-User-Id': currentUserId }
        });
        
        if (!res.ok) {
            throw new Error(`Failed to load courses (HTTP ${res.status})`);
        }

        const courses = await res.json();
        const selectedCourseId = new URLSearchParams(window.location.search).get('courseId');
        
        if (!courses || courses.length === 0) {
            const emptyOption = '<option value="">No courses available - Create a course first</option>';
            if (assignSelect) assignSelect.innerHTML = emptyOption;
            if (quizSelect) quizSelect.innerHTML = emptyOption;
            return;
        }

        const defaultOption = '<option value="">Select Target Course...</option>';
        const optionsHtml = defaultOption + courses.map(c => `
            <option value="${c.courseId}" ${selectedCourseId && selectedCourseId == c.courseId ? 'selected' : ''}>
                ${escapeHtml(c.title)}
            </option>
        `).join('');
        
        if (assignSelect) assignSelect.innerHTML = optionsHtml;
        if (quizSelect) quizSelect.innerHTML = optionsHtml;
    } catch (err) {
        console.error('Failed to load courses for dropdown:', err);
        const errOption = '<option value="">Error loading courses from server</option>';
        if (assignSelect) assignSelect.innerHTML = errOption;
        if (quizSelect) quizSelect.innerHTML = errOption;
    }
}

// Load Courses (Handles Dashboard Overview and Manage Courses separately)
async function loadCourses() {
    try {
        const [coursesRes, assignRes, quizRes] = await Promise.all([
            fetch(`${API_BASE}/courses`, { headers: { 'X-User-Id': currentUserId } }),
            fetch(`${API_BASE}/assignments`).catch(() => null),
            fetch(`${API_BASE}/quizzes`).catch(() => null)
        ]);

        const courses = await coursesRes.json();
        const assignments = assignRes && assignRes.ok ? await assignRes.json() : [];
        const quizzes = quizRes && quizRes.ok ? await quizRes.json() : [];
        
        const countElem = document.getElementById('metricTotalCourses');
        if (countElem) countElem.innerText = courses.length;

        const activeElem = document.getElementById('metricActiveCourses');
        if (activeElem) {
            activeElem.innerText = courses.filter(c => c.status === 'PUBLISHED').length;
        }

        // Render Dashboard Overview Table (Clean 5-column layout without assessments)
        const overviewTbody = document.getElementById('courseTableBody');
        if (overviewTbody) {
            if (courses.length === 0) {
                overviewTbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color: var(--text-muted); padding: 16px;">No courses created yet. Click "Create Course" to get started.</td></tr>`;
            } else {
                overviewTbody.innerHTML = courses.map(course => `
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
                            <a href="create-assignment.html?courseId=${course.courseId}" class="btn btn-primary" style="font-size: 12px; padding: 6px 12px;"><i class="fa-solid fa-plus"></i> Quiz/Assignment</a>
                            <button class="btn ${course.status === 'DRAFT' ? 'btn-success' : 'btn-secondary'}" 
                                    onclick="toggleStatus(${course.courseId}, '${course.status}')">
                                ${course.status === 'DRAFT' ? '<i class="fa-solid fa-paper-plane"></i> Publish' : '<i class="fa-solid fa-box-archive"></i> Unpublish'}
                            </button>
                            <button class="btn btn-danger" onclick="deleteCourse(${course.courseId})"><i class="fa-solid fa-trash"></i> Delete</button>
                        </td>
                    </tr>
                `).join('');
            }
        }

        // Render Manage Courses Table (Detailed 6-column layout with Course Assessments)
        const manageTbody = document.getElementById('manageCourseTableBody');
        if (manageTbody) {
            if (courses.length === 0) {
                manageTbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-muted); padding: 24px;">No courses created yet. Click "Create Course" to get started.</td></tr>`;
            } else {
                manageTbody.innerHTML = courses.map(course => {
                    const cAssigns = assignments.filter(a => a.courseId == course.courseId);
                    const cQuizzes = quizzes.filter(q => q.courseId == course.courseId);

                    let assessmentsHtml = '';
                    
                    if (cAssigns.length === 0 && cQuizzes.length === 0) {
                        assessmentsHtml = `<div style="color: var(--text-muted); font-size: 0.85rem; font-style: italic;">No assignments or quizzes created yet.</div>`;
                    } else {
                        assessmentsHtml = '<div style="display: flex; flex-direction: column; gap: 8px;">';
                        
                        if (cAssigns.length > 0) {
                            assessmentsHtml += `
                                <div style="background: rgba(99, 102, 241, 0.1); border: 1px solid rgba(99, 102, 241, 0.25); border-radius: 6px; padding: 6px 10px;">
                                    <div style="font-size: 11px; font-weight: 700; color: #818cf8; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px;">
                                        <i class="fa-solid fa-file-pen"></i> Assignments (${cAssigns.length})
                                    </div>
                                    <div style="display: flex; flex-wrap: wrap; gap: 6px;">
                                        ${cAssigns.map(a => `
                                            <span style="background: #1e293b; color: #f8fafc; font-size: 12px; padding: 3px 8px; border-radius: 4px; border: 1px solid #334155; display: inline-flex; align-items: center; gap: 6px;">
                                                <strong>${escapeHtml(a.title)}</strong>
                                                <span style="color: #94a3b8; font-size: 11px;">(${a.maxScore || 100} pts)</span>
                                            </span>
                                        `).join('')}
                                    </div>
                                </div>
                            `;
                        }

                        if (cQuizzes.length > 0) {
                            assessmentsHtml += `
                                <div style="background: rgba(16, 185, 129, 0.1); border: 1px solid rgba(16, 185, 129, 0.25); border-radius: 6px; padding: 6px 10px;">
                                    <div style="font-size: 11px; font-weight: 700; color: #34d399; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 4px;">
                                        <i class="fa-solid fa-brain"></i> Quizzes (${cQuizzes.length})
                                    </div>
                                    <div style="display: flex; flex-wrap: wrap; gap: 6px;">
                                        ${cQuizzes.map(q => `
                                            <span style="background: #1e293b; color: #f8fafc; font-size: 12px; padding: 3px 8px; border-radius: 4px; border: 1px solid #334155; display: inline-flex; align-items: center; gap: 6px;">
                                                <strong>${escapeHtml(q.title)}</strong>
                                                <span style="color: #94a3b8; font-size: 11px;">(${q.totalMarks || 100} pts, ${q.timeLimitMinutes || 30}m)</span>
                                            </span>
                                        `).join('')}
                                    </div>
                                </div>
                            `;
                        }
                        
                        assessmentsHtml += '</div>';
                    }

                    return `
                        <tr>
                            <td>#${course.courseId}</td>
                            <td><strong>${escapeHtml(course.title)}</strong></td>
                            <td>${escapeHtml(course.category)}</td>
                            <td>
                                <span class="badge ${course.status === 'PUBLISHED' ? 'badge-published' : 'badge-draft'}">
                                    ${course.status}
                                </span>
                            </td>
                            <td>${assessmentsHtml}</td>
                            <td>
                                <div style="display: flex; gap: 6px; flex-wrap: wrap;">
                                    <a href="create-assignment.html?courseId=${course.courseId}" class="btn btn-primary" style="font-size: 12px; padding: 6px 10px;" title="Add Quiz or Assignment">
                                        <i class="fa-solid fa-plus"></i> Quiz/Assignment
                                    </a>
                                    <button class="btn ${course.status === 'DRAFT' ? 'btn-success' : 'btn-secondary'}" 
                                            style="font-size: 12px; padding: 6px 10px;"
                                            onclick="toggleStatus(${course.courseId}, '${course.status}')">
                                        ${course.status === 'DRAFT' ? '<i class="fa-solid fa-paper-plane"></i> Publish' : '<i class="fa-solid fa-box-archive"></i> Unpublish'}
                                    </button>
                                    <button class="btn btn-danger" style="font-size: 12px; padding: 6px 10px;" onclick="deleteCourse(${course.courseId})">
                                        <i class="fa-solid fa-trash"></i> Delete
                                    </button>
                                </div>
                            </td>
                        </tr>
                    `;
                }).join('');
            }
        }
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
    const courseSelect = document.getElementById('assignmentCourseId') || document.getElementById('assignCourseSelect');
    const courseId = courseSelect ? courseSelect.value : '';
    const titleInput = document.getElementById('assignmentTitle') || document.getElementById('assignTitle');
    const instructionsInput = document.getElementById('instructions') || document.getElementById('assignInstructions');
    const dueDateInput = document.getElementById('dueDate') || document.getElementById('assignDueDate');
    const maxScoreInput = document.getElementById('maxScore') || document.getElementById('assignMaxScore');

    if (!courseId) {
        alert('Please select a target course for the assignment.');
        if (courseSelect) courseSelect.focus();
        return;
    }

    const maxScore = parseInt(maxScoreInput ? maxScoreInput.value : 100);
    if (isNaN(maxScore) || maxScore <= 0) {
        alert('Maximum marks must be a positive number greater than 0.');
        return;
    }

    const submitBtn = e.target.querySelector('button[type="submit"]');
    if (submitBtn) submitBtn.disabled = true;

    const payload = {
        courseId: parseInt(courseId),
        title: titleInput ? titleInput.value.trim() : '',
        instructions: instructionsInput ? instructionsInput.value.trim() : '',
        dueDate: dueDateInput && dueDateInput.value ? dueDateInput.value : null,
        maxScore: maxScore
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
            const errData = await res.json().catch(() => null);
            alert(`Failed to create assignment: ${errData && errData.message ? errData.message : 'Server error'}`);
        }
    } catch (err) {
        console.error('Error creating assignment:', err);
        alert('An unexpected error occurred while saving the assignment.');
    } finally {
        if (submitBtn) submitBtn.disabled = false;
    }
}

// Calculate Quiz Total Marks
function calculateQuizTotalMarks() {
    const totalMarksInput = document.getElementById('quizTotalMarks');
    if (!totalMarksInput) return;
    let sum = 0;
    document.querySelectorAll('.q-marks').forEach(input => {
        const val = parseInt(input.value) || 0;
        if (val > 0) sum += val;
    });
    if (!totalMarksInput.value || totalMarksInput.value === "0" || totalMarksInput.dataset.autoSync === "true") {
        totalMarksInput.value = sum > 0 ? sum : 100;
        totalMarksInput.dataset.autoSync = "true";
    }
}

// Dynamic Quiz Question Management
function addQuestionField() {
    const container = document.getElementById('questionsContainer');
    if (!container) return;

    const count = container.querySelectorAll('.quiz-question-box').length + 1;
    const questionBox = document.createElement('div');
    questionBox.className = 'quiz-question-box';
    questionBox.style.cssText = 'background: #1e293b; padding: 20px; border-radius: 10px; border: 1px solid #334155; margin-bottom: 16px; position: relative;';
    questionBox.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; border-bottom: 1px solid #334155; padding-bottom: 10px;">
            <h4 style="margin: 0; font-size: 1rem; color: #f8fafc;"><i class="fa-solid fa-circle-question" style="color: #3b82f6;"></i> Question <span class="q-num">${count}</span></h4>
            <button type="button" class="btn btn-danger btn-sm" onclick="removeQuestionField(this)" style="padding: 4px 10px; font-size: 0.85rem;"><i class="fa-solid fa-trash"></i> Remove Question</button>
        </div>

        <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 16px; margin-bottom: 16px;">
            <div class="form-group" style="margin-bottom: 0;">
                <label style="font-weight: 500; font-size: 0.875rem;">Question Text *</label>
                <input type="text" class="form-control q-text" placeholder="e.g. Which keyword is used to define a class in Java?" required>
            </div>
            <div class="form-group" style="margin-bottom: 0;">
                <label style="font-weight: 500; font-size: 0.875rem;">Question Marks *</label>
                <input type="number" class="form-control q-marks" value="5" min="1" onchange="calculateQuizTotalMarks()" oninput="calculateQuizTotalMarks()" required>
            </div>
        </div>

        <div style="margin-bottom: 12px;">
            <label style="font-weight: 500; font-size: 0.875rem; color: var(--text-muted); display: block; margin-bottom: 8px;">Answer Options (Fill at least Options A & B)</label>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                <div>
                    <span style="font-size: 12px; color: #94a3b8;">Option A *</span>
                    <input type="text" class="form-control q-opt-a" placeholder="Option A text" required>
                </div>
                <div>
                    <span style="font-size: 12px; color: #94a3b8;">Option B *</span>
                    <input type="text" class="form-control q-opt-b" placeholder="Option B text" required>
                </div>
                <div>
                    <span style="font-size: 12px; color: #94a3b8;">Option C</span>
                    <input type="text" class="form-control q-opt-c" placeholder="Option C text (optional)">
                </div>
                <div>
                    <span style="font-size: 12px; color: #94a3b8;">Option D</span>
                    <input type="text" class="form-control q-opt-d" placeholder="Option D text (optional)">
                </div>
            </div>
        </div>

        <div class="form-group" style="margin-bottom: 0; margin-top: 12px;">
            <label style="font-weight: 500; font-size: 0.875rem;">Correct Answer Option *</label>
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
    calculateQuizTotalMarks();
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
        calculateQuizTotalMarks();
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
    const courseSelect = document.getElementById('quizCourseId') || document.getElementById('quizCourseSelect');
    const courseId = courseSelect ? courseSelect.value : '';
    const titleInput = document.getElementById('quizTitle');
    const instructionsInput = document.getElementById('quizInstructions');
    const timeLimitInput = document.getElementById('timeLimitMinutes') || document.getElementById('quizTimeLimit');
    const totalMarksInput = document.getElementById('quizTotalMarks');

    if (!courseId) {
        alert('Please select a target course for the quiz.');
        if (courseSelect) courseSelect.focus();
        return;
    }

    const questionBoxes = document.querySelectorAll('.quiz-question-box');
    if (questionBoxes.length === 0) {
        alert('Please add at least 1 question to the quiz.');
        return;
    }

    const questions = [];
    let isValid = true;

    questionBoxes.forEach((box, idx) => {
        if (!isValid) return;
        const qText = box.querySelector('.q-text').value.trim();
        const optA = box.querySelector('.q-opt-a').value.trim();
        const optB = box.querySelector('.q-opt-b').value.trim();
        const optC = box.querySelector('.q-opt-c').value.trim();
        const optD = box.querySelector('.q-opt-d').value.trim();
        const correct = box.querySelector('.q-correct').value;
        const marks = parseInt(box.querySelector('.q-marks').value || 5);

        if (!qText) {
            alert(`Question #${idx + 1} is missing question text.`);
            isValid = false;
            return;
        }
        if (!optA || !optB) {
            alert(`Question #${idx + 1} requires at least Option A and Option B.`);
            isValid = false;
            return;
        }
        if (isNaN(marks) || marks <= 0) {
            alert(`Question #${idx + 1} marks must be greater than 0.`);
            isValid = false;
            return;
        }

        questions.push({
            questionText: qText,
            optionA: optA,
            optionB: optB,
            optionC: optC,
            optionD: optD,
            correctOption: correct,
            marks: marks,
            questionType: 'MULTIPLE_CHOICE'
        });
    });

    if (!isValid) return;

    calculateQuizTotalMarks();
    const totalMarks = parseInt(totalMarksInput ? totalMarksInput.value : 0) || questions.reduce((a, b) => a + b.marks, 0);

    const submitBtn = document.getElementById('submitQuizBtn') || e.target.querySelector('button[type="submit"]');
    if (submitBtn) submitBtn.disabled = true;

    const payload = {
        courseId: parseInt(courseId),
        title: titleInput ? titleInput.value.trim() : '',
        instructions: instructionsInput ? instructionsInput.value.trim() : '',
        timeLimitMinutes: parseInt(timeLimitInput ? timeLimitInput.value : 30),
        totalMarks: totalMarks,
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
            alert('Quiz created and published successfully!');
            document.getElementById('createQuizForm').reset();
            // Reset questions list back to 1 box
            const container = document.getElementById('questionsContainer');
            if (container) {
                container.innerHTML = '';
                addQuestionField();
            }
            loadAssignmentsAndQuizzes();
        } else {
            const errData = await res.json().catch(() => null);
            alert(`Failed to create quiz: ${errData && errData.message ? errData.message : 'Server error'}`);
        }
    } catch (err) {
        console.error('Error creating quiz:', err);
        alert('An unexpected error occurred while saving the quiz.');
    } finally {
        if (submitBtn) submitBtn.disabled = false;
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
                    <div style="background: #0f172a; padding: 14px 18px; border-radius: 8px; margin-bottom: 10px; border: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <strong style="font-size: 15px;">${escapeHtml(a.title)}</strong>
                            <div style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">Max Score: ${a.maxScore} pts</div>
                        </div>
                        <button class="btn btn-danger btn-sm" style="padding: 4px 10px; font-size: 12px;" onclick="deleteAssignmentItem(${a.assignmentId})">
                            <i class="fa-solid fa-trash"></i> Delete
                        </button>
                    </div>
                `).join('');
        }

        if (quizContainer) {
            quizContainer.innerHTML = quizzes.length === 0
                ? '<p style="color: var(--text-muted);">No quizzes published yet.</p>'
                : quizzes.map(q => `
                    <div style="background: #0f172a; padding: 14px 18px; border-radius: 8px; margin-bottom: 10px; border: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center;">
                        <div>
                            <strong style="font-size: 15px;">${escapeHtml(q.title)}</strong>
                            <div style="font-size: 12px; color: var(--text-muted); margin-top: 4px;">Time Limit: ${q.timeLimitMinutes} min | Total Marks: ${q.totalMarks || 100} pts</div>
                        </div>
                        <button class="btn btn-danger btn-sm" style="padding: 4px 10px; font-size: 12px;" onclick="deleteQuizItem(${q.quizId})">
                            <i class="fa-solid fa-trash"></i> Delete
                        </button>
                    </div>
                `).join('');
        }
    } catch (err) {
        console.error('Error loading assessments:', err);
    }
}

// Delete Quiz
async function deleteQuizItem(quizId) {
    if (!confirm('Are you sure you want to delete this quiz? This cannot be undone.')) return;
    try {
        const res = await fetch(`${API_BASE}/quizzes/${quizId}`, {
            method: 'DELETE',
            headers: { 'X-User-Id': currentUserId }
        });
        if (res.ok || res.status === 204) {
            alert('Quiz deleted successfully.');
            loadAssignmentsAndQuizzes();
            if (typeof loadCourses === 'function') loadCourses();
        } else {
            alert('Failed to delete quiz.');
        }
    } catch (err) {
        console.error('Error deleting quiz:', err);
    }
}

// Delete Assignment
async function deleteAssignmentItem(assignmentId) {
    if (!confirm('Are you sure you want to delete this assignment? This cannot be undone.')) return;
    try {
        const res = await fetch(`${API_BASE}/assignments/${assignmentId}`, {
            method: 'DELETE',
            headers: { 'X-User-Id': currentUserId }
        });
        if (res.ok || res.status === 204) {
            alert('Assignment deleted successfully.');
            loadAssignmentsAndQuizzes();
            if (typeof loadCourses === 'function') loadCourses();
        } else {
            alert('Failed to delete assignment.');
        }
    } catch (err) {
        console.error('Error deleting assignment:', err);
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

// Load Submissions and Quiz Attempts
async function loadSubmissions() {
    try {
        const [subRes, quizAttemptRes] = await Promise.all([
            fetch(`${API_BASE}/submissions`),
            fetch(`${API_BASE}/quiz-attempts`).catch(() => null)
        ]);

        const submissions = await subRes.json();
        let quizAttempts = [];
        if (quizAttemptRes && quizAttemptRes.ok) {
            quizAttempts = await quizAttemptRes.json();
        }

        const pendingElem = document.getElementById('metricPendingSubmissions');
        if (pendingElem) pendingElem.innerText = submissions.length + quizAttempts.length;

        // Render Assignment Submissions
        const tbody = document.getElementById('submissionTableBody');
        if (tbody) {
            if (submissions.length === 0) {
                tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color: var(--text-muted);">No student submissions available for grading.</td></tr>`;
            } else {
                tbody.innerHTML = submissions.map(sub => `
                    <tr>
                        <td>#${sub.submissionId}</td>
                        <td><strong>${escapeHtml(sub.studentName || 'Student')}</strong></td>
                        <td>${escapeHtml(sub.content)}</td>
                        <td>${new Date(sub.submittedAt).toLocaleDateString()}</td>
                        <td>
                            <button class="btn btn-primary" onclick="openGradeModal(${sub.submissionId}, '${escapeHtml(sub.studentName || 'Student')}')">
                                <i class="fa-solid fa-pen"></i> Grade Work
                            </button>
                        </td>
                    </tr>
                `).join('');
            }
        }

        // Render Quiz Attempts
        const quizTbody = document.getElementById('quizAttemptTableBody');
        if (quizTbody) {
            if (quizAttempts.length === 0) {
                quizTbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color: var(--text-muted);">No student quiz attempts recorded yet.</td></tr>`;
            } else {
                quizTbody.innerHTML = quizAttempts.map(attempt => {
                    let totalM = attempt.totalMarks;
                    let earnedS = attempt.score;

                    // Auto-healing for legacy records where score was saved as 100 percentage
                    if (!totalM) {
                        if (earnedS > (attempt.totalQuestions || 5)) {
                            totalM = 100;
                        } else {
                            totalM = attempt.totalQuestions ? attempt.totalQuestions * 5 : 5;
                        }
                    }

                    let pct = attempt.percentage != null ? attempt.percentage : ((earnedS / totalM) * 100);

                    return `
                        <tr>
                            <td>#${attempt.id}</td>
                            <td><strong>${escapeHtml(attempt.studentName || 'Student')}</strong></td>
                            <td>Quiz #${attempt.quizId}</td>
                            <td><strong style="color: var(--accent-emerald);">${earnedS}/${totalM}</strong></td>
                            <td><span style="color: var(--accent-emerald); font-weight: 600;">${pct.toFixed(1)}%</span></td>
                            <td>${new Date(attempt.submittedAt).toLocaleDateString()}</td>
                            <td>
                                <button class="btn btn-primary" style="font-size: 12px; padding: 6px 12px;" onclick="openEditQuizModal(${attempt.id}, '${escapeHtml(attempt.studentName || 'Student')}', ${earnedS}, ${totalM})">
                                    <i class="fa-solid fa-pen-to-square"></i> Edit Marks
                                </button>
                            </td>
                        </tr>
                    `;
                }).join('');
            }
        }
    } catch (err) {
        console.error('Failed to load submissions or quiz attempts:', err);
    }
}

// Open Grade Modal / Form for Assignment
function openGradeModal(submissionId, studentName) {
    document.getElementById('modalSubmissionId').value = submissionId;
    document.getElementById('modalStudentName').innerText = studentName;
    if (document.getElementById('editQuizGradeSection')) {
        document.getElementById('editQuizGradeSection').style.display = 'none';
    }
    document.getElementById('gradeSection').style.display = 'block';
    window.scrollTo({ top: document.getElementById('gradeSection').offsetTop, behavior: 'smooth' });
}

// Open Edit Quiz Marks Modal / Form
function openEditQuizModal(attemptId, studentName, score, totalMarks) {
    document.getElementById('modalQuizAttemptId').value = attemptId;
    document.getElementById('modalQuizStudentName').innerText = studentName;
    document.getElementById('editQuizScore').value = score != null ? score : 0;
    document.getElementById('editQuizTotalMarks').value = totalMarks != null ? totalMarks : 100;
    if (document.getElementById('gradeSection')) {
        document.getElementById('gradeSection').style.display = 'none';
    }
    const editSec = document.getElementById('editQuizGradeSection');
    if (editSec) {
        editSec.style.display = 'block';
        window.scrollTo({ top: editSec.offsetTop, behavior: 'smooth' });
    }
}

// Grade Submission (Assignment)
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

// Edit Quiz Attempt Grade
async function handleEditQuizGradeSubmission(e) {
    e.preventDefault();
    const attemptId = document.getElementById('modalQuizAttemptId').value;
    const score = parseInt(document.getElementById('editQuizScore').value);
    const totalMarks = parseInt(document.getElementById('editQuizTotalMarks').value || 100);

    if (isNaN(score) || score < 0) {
        alert('Please enter a valid score (0 or greater).');
        return;
    }
    if (isNaN(totalMarks) || totalMarks <= 0) {
        alert('Total marks must be greater than 0.');
        return;
    }
    if (score > totalMarks) {
        alert(`Score obtained (${score}) cannot exceed total marks (${totalMarks}).`);
        return;
    }

    const percentage = Math.round(((score / totalMarks) * 100.0) * 10.0) / 10.0;

    try {
        const res = await fetch(`${API_BASE}/quiz-attempts/${attemptId}/grade`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-Id': currentUserId
            },
            body: JSON.stringify({ score, totalMarks, percentage })
        });

        if (res.ok) {
            alert('Quiz marks updated successfully!');
            document.getElementById('editQuizGradeSection').style.display = 'none';
            loadSubmissions();
        } else {
            alert('Failed to update quiz marks.');
        }
    } catch (err) {
        console.error('Error updating quiz attempt marks:', err);
    }
}

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
