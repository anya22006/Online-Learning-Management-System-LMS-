import { useEffect, useState } from "react";
import axios from "axios";

function Settings() {

  const [settings, setSettings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [showForm, setShowForm] = useState(false);

  const [formData, setFormData] = useState({
  settingName: "",
  settingValue: ""
});

  const fetchSettings = async () => {

    try {

      const token = localStorage.getItem("token");

      const response = await axios.get(
        "http://localhost:8080/api/settings",
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      setSettings(response.data);

    } catch (error) {

      console.error("Error loading settings:", error);

      setError("Unable to load settings.");

    } finally {

      setLoading(false);

    }
  };

  useEffect(() => {
    fetchSettings();
  }, []);

  const handleChange = (e) => {

    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });

  };

  const handleAddSetting = async (e) => {

    e.preventDefault();

    try {

      const token = localStorage.getItem("token");

      await axios.post(
        "http://localhost:8080/api/settings",
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        }
      );

      alert("Setting added successfully!");

      setFormData({
        settingKey: "",
        settingValue: ""
      });

      setShowForm(false);

      fetchSettings();

    } catch (error) {

      console.error("Error adding setting:", error);

      alert(
        error.response?.data?.message ||
        "Unable to add setting."
      );

    }

  };

  return (

    <section className="dashboard-content">

      <div className="page-header">

        <div>

          <h2>System Settings</h2>

          <p>
            Manage LMS configuration and system preferences.
          </p>

        </div>

        <button
          className="primary-button"
          onClick={() => setShowForm(true)}
        >
          + Add Setting
        </button>

      </div>


      {showForm && (

        <div className="users-card add-user-card">

          <div className="table-header">

            <h3>Add New Setting</h3>

            <button
              className="close-button"
              onClick={() => setShowForm(false)}
            >
              ✕
            </button>

          </div>


          <form
            className="user-form"
            onSubmit={handleAddSetting}
          >

            <div className="form-group">

              <label>Setting Key</label>

              <input
                type="text"
                name="settingKey"
                placeholder="Example: site_name"
                value={formData.settingKey}
                onChange={handleChange}
                required
              />

            </div>


            <div className="form-group">

              <label>Setting Value</label>

              <input
                type="text"
                name="settingValue"
                placeholder="Example: My LMS"
                value={formData.settingValue}
                onChange={handleChange}
                required
              />

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
                Add Setting
              </button>

            </div>

          </form>

        </div>

      )}


      <div className="users-card">

        <div className="table-header">

          <h3>System Settings</h3>

          <span>
            {settings.length} settings
          </span>

        </div>


        {loading && (
          <p className="loading-text">
            Loading settings...
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

                  <th>Setting Key</th>

                  <th>Setting Value</th>

                </tr>

              </thead>


              <tbody>

                {settings.map((setting) => (

                  <tr key={setting.id}>

                    <td>
                      {setting.id}
                    </td>

                    <td>
                      <strong>
                        {setting.settingKey}
                      </strong>
                    </td>

                    <td>
                      {setting.settingValue}
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

export default Settings;