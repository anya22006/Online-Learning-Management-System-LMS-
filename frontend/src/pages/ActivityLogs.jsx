import { useEffect, useState } from "react";
import axios from "axios";

function ActivityLogs() {

  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchActivities = async () => {

    try {

      const token = localStorage.getItem("token");

      const response = await axios.get(
        "http://localhost:8080/api/activity-logs",
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      setActivities(response.data);

    } catch (error) {

      console.error("Error loading activity logs:", error);

      setError("Unable to load activity logs.");

    } finally {

      setLoading(false);

    }
  };

  useEffect(() => {
    fetchActivities();
  }, []);

  return (
    <section className="dashboard-content">

      <div className="page-header">

        <div>
          <h2>Activity Logs</h2>

          <p>
            Monitor recent activities performed in the LMS.
          </p>
        </div>

      </div>

      <div className="users-card">

        <div className="table-header">

          <h3>Recent Activities</h3>

          <span>
            {activities.length} activities
          </span>

        </div>

        {loading && (
          <p className="loading-text">
            Loading activities...
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
                  <th>User ID</th>
                  <th>Action</th>
                  <th>Description</th>
                  <th>Entity</th>
                  <th>Entity ID</th>
                </tr>
              </thead>

              <tbody>

                {activities.length === 0 ? (

                  <tr>
                    <td colSpan="6" style={{ textAlign: "center" }}>
                      No activities found.
                    </td>
                  </tr>

                ) : (

                  activities.map((activity) => (

                    <tr key={activity.id}>

                      <td>{activity.id}</td>

                      <td>{activity.userId}</td>

                      <td>
                        <span className="role-badge">
                          {activity.action}
                        </span>
                      </td>

                      <td>
                        {activity.description}
                      </td>

                      <td>
                        {activity.entityType}
                      </td>

                      <td>
                        {activity.entityId}
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

export default ActivityLogs;