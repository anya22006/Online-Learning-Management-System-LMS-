import { useEffect, useState } from "react";
import axios from "axios";

function Dashboard() {

  const [stats, setStats] = useState({
    totalUsers: 0,
    totalCourses: 0,
    totalEnrollments: 0,
    totalAssignments: 0,
    totalQuizzes: 0,
    totalActivities: 0
  });

  useEffect(() => {

    const token = localStorage.getItem("token");

    axios
      .get("http://localhost:8080/api/dashboard/stats", {
        headers: {
          Authorization: `Bearer ${token}`
        }
      })
      .then((response) => {
        setStats(response.data);
      })
      .catch((error) => {
        console.error("Error loading dashboard:", error);
      });

  }, []);


  return (
    <section className="dashboard-content">

      <div className="welcome-card">
        <div>
          <h2>Welcome to your LMS</h2>

          <p>
            Manage users, courses and learning activities
            from one place.
          </p>
        </div>
      </div>


      <div className="stats-grid">

        <div className="stat-card">
          <span>Total Users</span>
          <strong>{stats.totalUsers}</strong>
        </div>


        <div className="stat-card">
          <span>Total Courses</span>
          <strong>{stats.totalCourses}</strong>
        </div>


        <div className="stat-card">
          <span>Enrollments</span>
          <strong>{stats.totalEnrollments}</strong>
        </div>


        <div className="stat-card">
          <span>Assignments</span>
          <strong>{stats.totalAssignments}</strong>
        </div>


        <div className="stat-card">
          <span>Quizzes</span>
          <strong>{stats.totalQuizzes}</strong>
        </div>


        <div className="stat-card">
          <span>Activities</span>
          <strong>{stats.totalActivities}</strong>
        </div>

      </div>

    </section>
  );
}

export default Dashboard;