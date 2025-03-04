import LoginBox from "../components/Login";

const LoginOverlay = ({ setShowLogin, setIsLoggedIn }) => {
  const handleGoogleLogin = () => {
    setIsLoggedIn(true);
    window.location.href = "http://localhost:9081/users/auth/login";
  };

  const handleGuestLogin = () => {
    setIsLoggedIn(false);
    setShowLogin(false);
  };

  return (
    <div className="position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center" style={{ backgroundColor: "rgba(0, 0, 0, 0.5)", zIndex: 1050 }}>
      <div className="bg-white p-4">
        <LoginBox handleGoogleLogin={handleGoogleLogin} handleGuestLogin={handleGuestLogin} />
      </div>
    </div>
  );
};

export default LoginOverlay;
