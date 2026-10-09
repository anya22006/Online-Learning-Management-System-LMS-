import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

function Login() {

  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);


  const handleLogin = async (e) => {

    e.preventDefault();

    setError("");
    setLoading(true);

    try {
        console.log("LOGIN DATA:", email, password);

      const response = await axios.post(
        "http://localhost:8080/api/auth/login",
        {
          email: email,
          password: password
        }
      );

      // Save JWT token
      localStorage.setItem(
        "token",
        response.data.token
      );

      // Save user information
      localStorage.setItem(
        "user",
        JSON.stringify(response.data)
      );

      // Go to dashboard
      navigate("/dashboard");

    }  catch (error) {

  console.error("LOGIN ERROR:", error);

  if (error.response) {
    setError(
      `Login failed: ${error.response.status} - ${
        error.response.data?.message || "Server error"
      }`
    );
  } else {
    setError(
      "Cannot connect to Java backend. Make sure Spring Boot is running."
    );
  }

} finally {

      setLoading(false);

    }
  };


  return (
    <div className="login-page">

      <div className="login-card">

        <div className="login-logo">
          <div className="logo-icon">L</div>
          <h1>LMS</h1>
        </div>


        <h2>Welcome back</h2>

        <p className="login-subtitle">
          Sign in to your LMS admin account
        </p>


        <form onSubmit={handleLogin}>

          <div className="form-group">

            <label>Email</label>

            <input
              type="email"
              placeholder="admin@lms.com"
              value={email}
              onChange={(e) =>
                setEmail(e.target.value)
              }
              required
            />

          </div>


          <div className="form-group">

            <label>Password</label>

            <input
              type="password"
              placeholder="Enter your password"
              value={password}
              onChange={(e) =>
                setPassword(e.target.value)
              }
              required
            />

          </div>


          {error && (
            <div className="login-error">
              {error}
            </div>
          )}


          <button
            type="submit"
            className="login-button"
            disabled={loading}
          >

            {loading ? "Signing in..." : "Sign In"}

          </button>

        </form>

      </div>

    </div>
  );
}

export default Login;