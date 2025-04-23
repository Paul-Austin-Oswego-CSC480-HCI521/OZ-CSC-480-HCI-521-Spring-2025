import LoginBox from "../components/Login";
import { FocusTrap } from "focus-trap-react";

const LoginOverlay = ({ setShowLogin, setIsLoggedIn }) => {
  const handleGoogleLogin = () => {
    setIsLoggedIn(true);
    const PROXY_URL = import.meta.env.VITE_PROXY_URL || "http://localhost:9083"; 
    window.location.href = `${PROXY_URL}/users/auth/login`;
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
          <LoginBox handleGoogleLogin={handleGoogleLogin} handleGuestLogin={handleGuestLogin} setShowLogin={setShowLogin}  setIsLoggedIn={setIsLoggedIn}/>
        </div>
      </div>
    </FocusTrap>
  );
};

export default LoginOverlay;
