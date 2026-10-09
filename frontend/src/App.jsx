import {
  LayoutDashboard,
  Users as UsersIcon,
  BookOpen,
  Settings as SettingsIcon,
  Activity
} from  "lucide-react";

import {
  BrowserRouter,
  Routes,
  Route,
  Link,
  Navigate,
  useLocation
} from "react-router-dom";

import Dashboard from "./pages/Dashboard";
import Users from "./pages/Users";
import Courses from "./pages/Courses";
import Settings from "./pages/Settings";
import Login from "./pages/Login";
import ActivityLogs from "./pages/ActivityLogs";
import Enrollments from "./pages/Enrollments";

function ProtectedRoute({ children }) {

  const token = localStorage.getItem("token");

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
}


function Layout() {

  const location = useLocation();

  return (
    <div className="app">

      {/* Sidebar */}
      <aside className="sidebar">

        <div className="logo">
          <div className="logo-icon">L</div>
          <span>LMS</span>
        </div>

        <nav className="navigation">

          <Link
            to="/dashboard"
            className={`nav-item ${
              location.pathname === "/dashboard" ? "active" : ""
            }`}
          >
            <LayoutDashboard size={20} />
            <span>Dashboard</span>
          </Link>


          <Link
            to="/users"
            className={`nav-item ${
              location.pathname === "/users" ? "active" : ""
            }`}
          >
            <UsersIcon size={20} />
            <span>Users</span>
          </Link>


          <Link
            to="/courses"
            className={`nav-item ${
              location.pathname === "/courses" ? "active" : ""
            }`}
          >
            <BookOpen size={20} />
            <span>Courses</span>
          </Link>


          <Link
            to="/settings"
            className={`nav-item ${
              location.pathname === "/settings" ? "active" : ""
            }`}
          >
            <SettingsIcon size={20} />
            <span>Settings</span>
          </Link>
          <Link
  to="/activity-logs"
  className={`nav-item ${
    location.pathname === "/activity-logs" ? "active" : ""
  }`}
>
  <Activity size={20} />
  <span>Activity Logs</span>
</Link>

<Link
  to="/enrollments"
  className={`nav-item ${
    location.pathname === "/enrollments" ? "active" : ""
  }`}
>
  <BookOpen size={20} />
  <span>Enrollments</span>
</Link>

        </nav>

      </aside>


      {/* Main Content */}
      <main className="main-content">

        <header className="topbar">

          <div>
<h1>
  {location.pathname === "/dashboard"
    ? "Dashboard"
    : location.pathname === "/users"
    ? "Users"
    : location.pathname === "/courses"
    ? "Courses"
    : location.pathname === "/settings"
    ? "Settings"
    : location.pathname === "/activity-logs"
    ? "Activity Logs"
    : location.pathname === "/enrollments"
    ? "Enrollments"
    : "LMS"}
</h1>

            <p>Welcome back, Admin 👋</p>

          </div>


          <div className="profile">

            <div className="profile-avatar">
              A
            </div>

            <div>
              <strong>Admin</strong>
              <span>Administrator</span>
            </div>

          </div>

        </header>


        <Routes>

          {/* Login */}
          <Route
            path="/login"
            element={<Login />}
          />


          {/* Dashboard */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            }
          />


          {/* Users */}
          <Route
            path="/users"
            element={
              <ProtectedRoute>
                <Users />
              </ProtectedRoute>
            }
          />


          {/* Courses */}
          <Route
            path="/courses"
            element={
              <ProtectedRoute>
                <Courses />
              </ProtectedRoute>
            }
          />


          {/* Settings */}
          <Route
            path="/settings"
            element={
              <ProtectedRoute>
                <Settings />
              </ProtectedRoute>
            }
          />
          <Route
  path="/activity-logs"
  element={
    <ProtectedRoute>
      <ActivityLogs />
    </ProtectedRoute>
  }
/>
 
 <Route
  path="/enrollments"
  element={
    <ProtectedRoute>
      <Enrollments />
    </ProtectedRoute>
  }
/> 

          {/* Default */}
          <Route
            path="/"
            element={<Navigate to="/login" replace />}
          />


          {/* Unknown URL */}
          <Route
            path="*"
            element={<Navigate to="/login" replace />}
          />

        </Routes>

      </main>

    </div>
  );
}


function App() {

  return (
    <BrowserRouter>
      <Routes>

        {/* Login does NOT use sidebar */}
        <Route
          path="/login"
          element={<Login />}
        />

        {/* Everything else uses the LMS layout */}
        <Route
          path="/*"
          element={<Layout />}
        />

      </Routes>
    </BrowserRouter>
  );
}


export default App;