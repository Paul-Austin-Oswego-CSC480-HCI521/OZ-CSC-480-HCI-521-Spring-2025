import LoginBox from "../components/Login";
import { FocusTrap } from "focus-trap-react";

const LoginOverlay = ({ setShowLogin, setIsLoggedIn }) => {
  const handleGoogleLogin = () => {
    setIsLoggedIn(true);
    window.location.href = "http://localhost:9081/users/auth/login";
  };

  const handleGuestLogin = () => {
    setIsLoggedIn(false);
    setShowLogin(false);
  };

  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) {
      setShowLogin(false);
    }
  };

  return (
    <FocusTrap>
      <div
        className="position-fixed top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center"
        style={{ backgroundColor: "rgba(0, 0, 0, 0.5)", zIndex: 1050 }}
        onClick={handleOverlayClick}
      >
        <div onClick={(e) => e.stopPropagation()}>
          <LoginBox handleGoogleLogin={handleGoogleLogin} handleGuestLogin={handleGuestLogin} setShowLogin={setShowLogin} />
        </div>
      </div>
    </FocusTrap>
  );
};

export default LoginOverlay;
