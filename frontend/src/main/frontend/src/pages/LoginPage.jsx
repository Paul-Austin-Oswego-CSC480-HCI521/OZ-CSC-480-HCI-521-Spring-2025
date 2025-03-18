import LoginBox from '../components/Login';


const LoginPage = ({ setIsGuest }) => {

  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:9081/users/auth/login';
  };

  const handleGuestLogin = () => {
    setIsGuest(true);
  };

  return (
    <div className="d-flex justify-content-center align-items-center vh-100">
      <LoginBox handleGoogleLogin={handleGoogleLogin} handleGuestLogin={handleGuestLogin} />
    </div>
  );
};

export default LoginPage;
