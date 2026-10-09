import { useEffect, useState } from "react";
import axios from "axios";

function Users() {

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [showForm, setShowForm] = useState(false);
  const [editingUser, setEditingUser] = useState(null);

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    password: "",
    role: "student",
    status: "active"
  });

  const fetchUsers = async () => {
    try {
      const token = localStorage.getItem("token");

      const response = await axios.get(
        "http://localhost:8080/api/users",
        {
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      setUsers(response.data);

    } catch (error) {
      console.error("Error loading users:", error);
      setError("Unable to load users.");

    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleAddUser = async (e) => {

    e.preventDefault();

    try {

      const token = localStorage.getItem("token");

      await axios.post(
        "http://localhost:8080/api/users",
        formData,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        }
      );

      alert("User added successfully!");

      setFormData({
        name: "",
        email: "",
        password: "",
        role: "student",
        status: "active"
      });

      setShowForm(false);

      fetchUsers();

    } catch (error) {

      console.error("Error adding user:", error);

      alert(
        error.response?.data?.message ||
        "Unable to add user."
      );
    }
  };
  const handleEditUser = async (e) => {

  e.preventDefault();

  try {

    const token = localStorage.getItem("token");

    await axios.put(
      `http://localhost:8080/api/users/${editingUser.id}`,
      {
        name: editingUser.name,
        email: editingUser.email,
        role: editingUser.role,
        status: editingUser.status
      },
      {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json"
        }
      }
    );

    alert("User updated successfully!");

    setEditingUser(null);

    fetchUsers();

  } catch (error) {

    console.error("Error updating user:", error);

    alert(
      error.response?.data?.message ||
      "Unable to update user."
    );
  }
}; 

const handleDeleteUser = async (id, name) => {

  const confirmed = window.confirm(
    `Are you sure you want to delete ${name}?`
  );

  if (!confirmed) {
    return;
  }

  try {

    const token = localStorage.getItem("token");

    await axios.delete(
      `http://localhost:8080/api/users/${id}`,
      {
        headers: {
          Authorization: `Bearer ${token}`
        }
      }
    );

    alert("User deleted successfully!");

    fetchUsers();

  } catch (error) {

    console.error("Error deleting user:", error);

    alert(
      error.response?.data?.message ||
      "Unable to delete user."
    );
  }
};

  return (
    <section className="dashboard-content">

      <div className="page-header">

        <div>
          <h2>User Management</h2>
          <p>
            Manage students, instructors and administrators.
          </p>
        </div>

        <button
          className="primary-button"
          onClick={() => setShowForm(true)}
        >
          + Add User
        </button>

      </div>


      {/* ADD USER FORM */}

      {showForm && (

        <div className="users-card add-user-card">

          <div className="table-header">

            <h3>Add New User</h3>

            <button
              className="close-button"
              onClick={() => setShowForm(false)}
            >
              ✕
            </button>

          </div>


          <form
            className="user-form"
            onSubmit={handleAddUser}
          >

            <div className="form-row">

              <div className="form-group">
                <label>Name</label>

                <input
                  type="text"
                  name="name"
                  placeholder="Enter full name"
                  value={formData.name}
                  onChange={handleChange}
                  required
                />

              </div>


              <div className="form-group">
                <label>Email</label>

                <input
                  type="email"
                  name="email"
                  placeholder="Enter email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />

              </div>

            </div>


            <div className="form-row">

              <div className="form-group">
                <label>Password</label>

                <input
                  type="password"
                  name="password"
                  placeholder="Enter password"
                  value={formData.password}
                  onChange={handleChange}
                  required
                />

              </div>


              <div className="form-group">
                <label>Role</label>

                <select
                  name="role"
                  value={formData.role}
                  onChange={handleChange}
                >
                  <option value="student">
                    Student
                  </option>

                  <option value="instructor">
                    Instructor
                  </option>

                  <option value="admin">
                    Administrator
                  </option>

                </select>

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
                Add User
              </button>

            </div>

          </form>

        </div>

      )}
      {/* EDIT USER FORM */}

{editingUser && (

  <div className="users-card add-user-card">

    <div className="table-header">

      <h3>Edit User</h3>

      <button
        className="close-button"
        onClick={() => setEditingUser(null)}
      >
        ✕
      </button>

    </div>

    <form
      className="user-form"
      onSubmit={handleEditUser}
    >

      <div className="form-row">

        <div className="form-group">
          <label>Name</label>

          <input
            type="text"
            value={editingUser.name}
            onChange={(e) =>
              setEditingUser({
                ...editingUser,
                name: e.target.value
              })
            }
            required
          />
        </div>

        <div className="form-group">
          <label>Email</label>

          <input
            type="email"
            value={editingUser.email}
            onChange={(e) =>
              setEditingUser({
                ...editingUser,
                email: e.target.value
              })
            }
            required
          />
        </div>

      </div>


      <div className="form-row">

        <div className="form-group">
          <label>Role</label>

          <select
            value={editingUser.role}
            onChange={(e) =>
              setEditingUser({
                ...editingUser,
                role: e.target.value
              })
            }
          >
            <option value="student">
              Student
            </option>

            <option value="instructor">
              Instructor
            </option>

            <option value="admin">
              Administrator
            </option>

          </select>
        </div>


        <div className="form-group">
          <label>Status</label>

          <select
            value={editingUser.status}
            onChange={(e) =>
              setEditingUser({
                ...editingUser,
                status: e.target.value
              })
            }
          >

            <option value="active">
              Active
            </option>

            <option value="inactive">
              Inactive
            </option>

          </select>
        </div>

      </div>


      <div className="form-actions">

        <button
          type="button"
          className="secondary-button"
          onClick={() => setEditingUser(null)}
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


      {/* USERS TABLE */}

      <div className="users-card">

        <div className="table-header">

          <h3>All Users</h3>

          <span>
            {users.length} users
          </span>

        </div>


        {loading && (
          <p className="loading-text">
            Loading users...
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
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                  <th>Status</th>
<th>Actions</th>   
                </tr>

              </thead>


              <tbody>

                {users.map((user) => (

                  <tr key={user.id}>

                    <td>{user.id}</td>

                    <td>
                      <strong>
                        {user.name}
                      </strong>
                    </td>

                    <td>
                      {user.email}
                    </td>

                    <td>

                      <span className="role-badge">
                        {user.role}
                      </span>

                    </td>

                    <td>

                      <span>
                        className={
                          user.status === "active"
                            ? "status-active"
                            : "status-inactive"
                        }
                        
                
                        {user.status}
                      </span>

                    </td>
          <td>
  <div className="action-buttons">

    <button
      className="edit-button"
      onClick={() => setEditingUser({ ...user })}
    >
      Edit
    </button>

    <button
      className="delete-button"
      onClick={() =>
        handleDeleteUser(user.id, user.name)
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

export default Users;