import { useEffect, useState } from "react";
import axios from "axios";

function Enrollments() {

  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [showForm, setShowForm] = useState(false);

  const [formData, setFormData] = useState({
    studentId: "",
    courseId: "",
    status: "active"
  });

  const fetchEnrollments = async () => {

    try {

      const token = localStorage.getItem("token");

      const response = await axios.get(
        "http://localhost:8080/api/enrollments",
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      setEnrollments(response.data);

    }catch (error) {
    console.error("ERROR STATUS:", error.response?.status);
    console.error("ERROR DATA:", error.response?.data);
    console.error("FULL ERROR:", error);

    setError(
        error.response?.data?.message ||
        `Unable to add enrollment. Status: ${error.response?.status || "unknown"}`
    );
} finally {

      setLoading(false);

    }
  };

  useEffect(() => {
    fetchEnrollments();
  }, []);

  const handleChange = (e) => {

    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });

  };

  const handleAddEnrollment = async (e) => {

    e.preventDefault();

    try {

      const token = localStorage.getItem("token");

      await axios.post(
        "http://localhost:8080/api/enrollments",
        {
          studentId: Number(formData.studentId),
          courseId: Number(formData.courseId),
          status: formData.status
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        }
      );

      alert("Student enrolled successfully!");

      setFormData({
        studentId: "",
        courseId: "",
        status: "active"
      });

      setShowForm(false);

      fetchEnrollments();

    } catch (error) {

      console.error("Error adding enrollment:", error);

      alert(
        error.response?.data?.message ||
        "Unable to create enrollment."
      );

    }
  };

  return (
    <section className="dashboard-content">

      <div className="page-header">

        <div>

          <h2>Enrollment Management</h2>

          <p>
            Manage student course enrollments.
          </p>

        </div>

        <button
          className="primary-button"
          onClick={() => setShowForm(true)}
        >
          + Enroll Student
        </button>

      </div>


      {showForm && (

        <div className="users-card add-user-card">

          <div className="table-header">

            <h3>Enroll Student</h3>

            <button
              className="close-button"
              onClick={() => setShowForm(false)}
            >
              ✕
            </button>

          </div>


          <form
            className="user-form"
            onSubmit={handleAddEnrollment}
          >

            <div className="form-row">

              <div className="form-group">

                <label>Student ID</label>

                <input
                  type="number"
                  name="studentId"
                  placeholder="Enter student ID"
                  value={formData.studentId}
                  onChange={handleChange}
                  required
                />

              </div>


              <div className="form-group">

                <label>Course ID</label>

                <input
                  type="number"
                  name="courseId"
                  placeholder="Enter course ID"
                  value={formData.courseId}
                  onChange={handleChange}
                  required
                />

              </div>

            </div>


            <div className="form-group">

              <label>Status</label>

              <select
                name="status"
                value={formData.status}
                onChange={handleChange}
              >

                <option value="active">
                  Active
                </option>

                <option value="inactive">
                  Inactive
                </option>

              </select>

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
                Enroll Student
              </button>

            </div>

          </form>

        </div>

      )}


      <div className="users-card">

        <div className="table-header">

          <h3>All Enrollments</h3>

          <span>
            {enrollments.length} enrollments
          </span>

        </div>


        {loading && (
          <p className="loading-text">
            Loading enrollments...
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
                  <th>Student ID</th>
                  <th>Course ID</th>
                  <th>Status</th>

                </tr>

              </thead>


              <tbody>

                {enrollments.length === 0 ? (

                  <tr>

                    <td
                      colSpan="4"
                      style={{ textAlign: "center" }}
                    >
                      No enrollments found.
                    </td>

                  </tr>

                ) : (

                  enrollments.map((enrollment) => (

                    <tr key={enrollment.id}>

                      <td>
                        {enrollment.id}
                      </td>

                      <td>
                        {enrollment.studentId}
                      </td>

                      <td>
                        {enrollment.courseId}
                      </td>

                      <td>

                        <span
                          className={
                            enrollment.status === "active"
                              ? "status-active"
                              : "status-inactive"
                          }
                        >
                          {enrollment.status}
                        </span>

                      </td>

                    </tr>

                  ))

                )}

              </tbody>

            </table>

          </div>

        )}

      </div>

    </section>
  );
}

export default Enrollments;