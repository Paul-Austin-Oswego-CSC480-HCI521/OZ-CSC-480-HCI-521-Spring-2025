import React,{useState} from 'react';
import LoginBox from '../components/Login';


const LoginPage = ({ setIsAuthenticated, setIsGuest }) => {
  const [jwtToken, setJwtToken] = useState("");
  const [email, setEmail] = useState("")

  // useEffect(() => {
  //   fetch("localhost://9080")
  //       .then(
  //         setJwtToken(sessionStorage.getItem("quotableToken"));
  //         setIsAuthenticated(true);
  //       )
  //       .then()
  // }, []);

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
