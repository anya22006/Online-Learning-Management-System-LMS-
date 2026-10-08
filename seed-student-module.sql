USE lms;

START TRANSACTION;

-- Seed the temporary student used by the frontend only when ID 1 is unused.
-- The password value is deliberately not a usable login credential.
INSERT INTO users (
    id,
    name,
    email,
    `password`,
    role,
    status,
    created_at,
    updated_at
)
SELECT
    1,
    'Student One',
    'student1@example.test',
    'SEED_ONLY_NO_LOGIN',
    'student',
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE id = 1
)
AND NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'student1@example.test'
);

-- ID 5 is used as the courseId in the existing lesson-page URL.
-- Do not replace a course that may already occupy this ID.
INSERT INTO courses (
    id,
    title,
    description,
    instructor_id,
    status,
    created_at,
    updated_at
)
SELECT
    5,
    'Foundations of Web Development',
    'Learn the building blocks of web pages, from semantic HTML to responsive CSS and basic JavaScript.',
    NULL,
    'active',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM courses
    WHERE id = 5
);

-- Create an enrollment so the seeded course also appears in My Courses.
INSERT INTO enrollments (
    student_id,
    course_id,
    progress,
    status,
    enrolled_at
)
SELECT
    1,
    5,
    0,
    'active',
    CURRENT_TIMESTAMP
FROM DUAL
WHERE EXISTS (
    SELECT 1
    FROM users
    WHERE id = 1
      AND email = 'student1@example.test'
      AND role = 'student'
)
AND EXISTS (
    SELECT 1
    FROM courses
    WHERE id = 5
      AND title = 'Foundations of Web Development'
)
AND NOT EXISTS (
    SELECT 1
    FROM enrollments
    WHERE student_id = 1
      AND course_id = 5
);

-- Module IDs are fixed so re-running this script does not create duplicates.
INSERT INTO modules (
    id,
    course_id,
    title,
    description,
    module_order,
    created_at
)
SELECT
    1,
    5,
    'HTML Foundations',
    'Understand document structure and the semantic elements used to create accessible web pages.',
    1,
    CURRENT_TIMESTAMP
FROM DUAL
WHERE EXISTS (
    SELECT 1
    FROM courses
    WHERE id = 5
      AND title = 'Foundations of Web Development'
)
AND NOT EXISTS (
    SELECT 1
    FROM modules
    WHERE id = 1
);

INSERT INTO modules (
    id,
    course_id,
    title,
    description,
    module_order,
    created_at
)
SELECT
    2,
    5,
    'CSS and Responsive Layouts',
    'Style web pages with CSS and adapt layouts for different screen sizes.',
    2,
    CURRENT_TIMESTAMP
FROM DUAL
WHERE EXISTS (
    SELECT 1
    FROM courses
    WHERE id = 5
      AND title = 'Foundations of Web Development'
)
AND NOT EXISTS (
    SELECT 1
    FROM modules
    WHERE id = 2
);

-- Lesson ID 22 is referenced by the existing lesson-page URL.
INSERT INTO lessons (
    id,
    module_id,
    title,
    content,
    lesson_order,
    created_at,
    updated_at
)
SELECT
    22,
    1,
    'Structure of an HTML Document',
    'An HTML document begins with <!DOCTYPE html>, followed by the html element. The head contains page metadata such as the character set and title. The body contains the content shown to visitors. Use meaningful elements such as header, main, section, and footer to describe the role of each part of the page.',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM DUAL
WHERE EXISTS (
    SELECT 1
    FROM modules
    WHERE id = 1
      AND course_id = 5
      AND title = 'HTML Foundations'
)
AND NOT EXISTS (
    SELECT 1
    FROM lessons
    WHERE id = 22
);

INSERT INTO lessons (
    id,
    module_id,
    title,
    content,
    lesson_order,
    created_at,
    updated_at
)
SELECT
    23,
    1,
    'Semantic HTML Elements',
    'Semantic elements communicate the meaning of page content. Use main for the primary page content, nav for navigation links, article for a self-contained item, and section to group related content. Clear structure helps people using assistive technology and makes pages easier to maintain.',
    2,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM DUAL
WHERE EXISTS (
    SELECT 1
    FROM modules
    WHERE id = 1
      AND course_id = 5
      AND title = 'HTML Foundations'
)
AND NOT EXISTS (
    SELECT 1
    FROM lessons
    WHERE id = 23
);

INSERT INTO lessons (
    id,
    module_id,
    title,
    content,
    lesson_order,
    created_at,
    updated_at
)
SELECT
    24,
    2,
    'CSS Selectors and the Box Model',
    'CSS selectors choose which HTML elements to style. Each element is laid out using the box model: content, padding, border, and margin. Use box-sizing: border-box when you want declared widths to include padding and borders, and keep spacing rules consistent across components.',
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM DUAL
WHERE EXISTS (
    SELECT 1
    FROM modules
    WHERE id = 2
      AND course_id = 5
      AND title = 'CSS and Responsive Layouts'
)
AND NOT EXISTS (
    SELECT 1
    FROM lessons
    WHERE id = 24
);

INSERT INTO lessons (
    id,
    module_id,
    title,
    content,
    lesson_order,
    created_at,
    updated_at
)
SELECT
    25,
    2,
    'Responsive Design Basics',
    'Responsive layouts adjust to the available screen size. Flexible widths, CSS Grid or Flexbox, and media queries help pages work on phones, tablets, and desktop screens. Test at narrow widths and make sure text, controls, and navigation remain usable.',
    2,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM DUAL
WHERE EXISTS (
    SELECT 1
    FROM modules
    WHERE id = 2
      AND course_id = 5
      AND title = 'CSS and Responsive Layouts'
)
AND NOT EXISTS (
    SELECT 1
    FROM lessons
    WHERE id = 25
);

COMMIT;
