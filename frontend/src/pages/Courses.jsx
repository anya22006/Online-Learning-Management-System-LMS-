import { useEffect, useState } from "react";
import axios from "axios";

function Courses() {

  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [showForm, setShowForm] = useState(false);
  const [editingCourse, setEditingCourse] = useState(null);

 const [formData, setFormData] = useState({
  title: "",
  description: "",
  instructorId: "",
  status: "draft"
});
  const fetchCourses = async () => {

    try {

      const token = localStorage.getItem("token");

      const response = await axios.get(
        "http://localhost:8080/api/courses",
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      setCourses(response.data);

    } catch (error) {

      console.error("Error loading courses:", error);
      setError("Unable to load courses.");

    } finally {

      setLoading(false);

    }
  };

  useEffect(() => {
    fetchCourses();
  }, []);


  const handleChange = (e) => {

    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });

  };


  const handleAddCourse = async (e) => {

    e.preventDefault();

    try {

      const token = localStorage.getItem("token");

      await axios.post(
        "http://localhost:8080/api/courses",
        {
          ...formData,
          instructorId: Number(formData.instructorId)
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        }
      );

      alert("Course added successfully!");

      setFormData({
        title: "",
        description: "",
        instructorId: "",
        status: "draft"
      });

      setShowForm(false);

      fetchCourses();

    } catch (error) {

      console.error("Error adding course:", error);

      alert(
        error.response?.data?.message ||
        "Unable to add course."
      );

    }

  };


  const handleEditCourse = async (e) => {

    e.preventDefault();

    try {

      const token = localStorage.getItem("token");

      await axios.put(
        `http://localhost:8080/api/courses/${editingCourse.id}`,
        {
          title: editingCourse.title,
          description: editingCourse.description,
          instructorId: Number(editingCourse.instructorId),
          status: editingCourse.status
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        }
      );

      alert("Course updated successfully!");

      setEditingCourse(null);

      fetchCourses();

    } catch (error) {

      console.error("Error updating course:", error);

      alert(
        error.response?.data?.message ||
        "Unable to update course."
      );

    }

  };


  const handleDeleteCourse = async (id, title) => {

    const confirmed = window.confirm(
      `Are you sure you want to delete ${title}?`
    );

    if (!confirmed) {
      return;
    }

    try {

      const token = localStorage.getItem("token");

      await axios.delete(
        `http://localhost:8080/api/courses/${id}`,
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      alert("Course deleted successfully!");

      fetchCourses();

    } catch (error) {

      console.error("Error deleting course:", error);

      alert(
        error.response?.data?.message ||
        "Unable to delete course."
      );

    }

  };


  return (

    <section className="dashboard-content">

      <div className="page-header">

        <div>
          <h2>Course Management</h2>

          <p>
            Manage courses, instructors and learning content.
          </p>
        </div>

        <button
          className="primary-button"
          onClick={() => setShowForm(true)}
        >
          + Add Course
        </button>

      </div>


      {/* ADD COURSE FORM */}

      {showForm && (

        <div className="users-card add-user-card">

          <div className="table-header">

            <h3>Add New Course</h3>

            <button
              className="close-button"
              onClick={() => setShowForm(false)}
            >
              ✕
            </button>

          </div>


          <form
            className="user-form"
            onSubmit={handleAddCourse}
          >

            <div className="form-group">

              <label>Course Title</label>

              <input
                type="text"
                name="title"
                placeholder="Enter course title"
                value={formData.title}
                onChange={handleChange}
                required
              />

            </div>


            <div className="form-group">

              <label>Description</label>

              <textarea
                name="description"
                placeholder="Enter course description"
                value={formData.description}
                onChange={handleChange}
                rows="4"
                required
              />

            </div>


            <div className="form-row">

              <div className="form-group">

                <label>Instructor ID</label>

                <input
                  type="number"
                  name="instructorId"
                  placeholder="Enter instructor ID"
                  value={formData.instructorId}
                  onChange={handleChange}
                  required
                />

              </div>


              <div className="form-group">

                <label>Status</label>

                <select
                  name="status"
                  value={formData.status}
                  onChange={handleChange}
                >

                <option value="draft">Draft</option>
<option value="published">Published</option>
<option value="archived">Archived</option>

                </select>

              </div>

            </div>


            <div className="form-actions">

              <button
                type="button"
                className="secondary-button"
                onClick={() => setShowForm(false)}
              >
                Cancel
              </button>

              <button
                type="submit"
                className="primary-button"
              >
                Add Course
              </button>

            </div>

          </form>

        </div>

      )}


      {/* EDIT COURSE FORM */}

      {editingCourse && (

        <div className="users-card add-user-card">

          <div className="table-header">

            <h3>Edit Course</h3>

            <button
              className="close-button"
              onClick={() => setEditingCourse(null)}
            >
              ✕
            </button>

          </div>


          <form
            className="user-form"
            onSubmit={handleEditCourse}
          >

            <div className="form-group">

              <label>Course Title</label>

              <input
                type="text"
                value={editingCourse.title}
                onChange={(e) =>
                  setEditingCourse({
                    ...editingCourse,
                    title: e.target.value
                  })
                }
                required
              />

            </div>


            <div className="form-group">

              <label>Description</label>

              <textarea
                value={editingCourse.description || ""}
                onChange={(e) =>
                  setEditingCourse({
                    ...editingCourse,
                    description: e.target.value
                  })
                }
                rows="4"
                required
              />

            </div>


            <div className="form-row">

              <div className="form-group">

                <label>Instructor ID</label>

                <input
                  type="number"
                  value={editingCourse.instructorId}
                  onChange={(e) =>
                    setEditingCourse({
                      ...editingCourse,
                      instructorId: e.target.value
                    })
                  }
                  required
                />

              </div>


              <div className="form-group">

                <label>Status</label>

                <select
                  value={editingCourse.status}
                  onChange={(e) =>
                    setEditingCourse({
                      ...editingCourse,
                      status: e.target.value
                    })
                  }
                >

                 <option value="draft">Draft</option>
<option value="published">Published</option>
<option value="archived">Archived</option>

                </select>

              </div>

            </div>


            <div className="form-actions">

              <button
                type="button"
                className="secondary-button"
                onClick={() => setEditingCourse(null)}
              >
                Cancel
              </button>

              <button
                type="submit"
                className="primary-button"
              >
                Save Changes
              </button>

            </div>

          </form>

        </div>

      )}


      {/* COURSES TABLE */}

      <div className="users-card">

        <div className="table-header">

          <h3>All Courses</h3>

          <span>
            {courses.length} courses
          </span>

        </div>


        {loading && (
          <p className="loading-text">
            Loading courses...
          </p>
        )}


        {error && (
          <p className="error-text">
            {error}
          </p>
        )}


        {!loading && !error && (

          <div className="table-container">

            <table>

              <thead>

                <tr>
                  <th>ID</th>
                  <th>Course</th>
                  <th>Description</th>
                  <th>Instructor</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>

              </thead>


              <tbody>

                {courses.map((course) => (

                  <tr key={course.id}>

                    <td>
                      {course.id}
                    </td>

                    <td>
                      <strong>
                        {course.title}
                      </strong>
                    </td>

                    <td>
                      {course.description}
                    </td>

                    <td>
                      {course.instructorId}
                    </td>

                    <td>

                      <span
                        className={`status-${course.status}`}
                      >
                        {course.status}
                      </span>

                    </td>

                    <td>

                      <div className="action-buttons">

                        <button
                          className="edit-button"
                          onClick={() =>
                            setEditingCourse({
                              ...course
                            })
                          }
                        >
                          Edit
                        </button>

                        <button
                          className="delete-button"
                          onClick={() =>
                            handleDeleteCourse(
                              course.id,
                              course.title
                            )
                          }
                        >
                          Delete
                        </button>

                      </div>

                    </td>

                  </tr>

                ))}

              </tbody>

            </table>

          </div>

        )}

      </div>

    </section>
  );
}

export default Courses;