import React,{useState, useEffect} from 'react';
import LoginBox from '../components/Login';
import { useNavigate } from "react-router-dom";


const LoginPage = ({ setIsAuthenticated, setIsGuest }) => {
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
         fetch("http://localhost:9081/users/accounts/whoami")
             .then(response=> response.json())
             .then((data)=> {
               if(data){
                 setIsAuthenticated(true)
                 setUser(data)
               }
             })
             .catch(err => {
               console.log("User not found", err)
             })
  }, []);

  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:9081/users/auth/login';
  };

  const handleGuestLogin = () => {
    //setIsGuest(true);
    navigate('/');
  };

  return (
    <div className="d-flex justify-content-center align-items-center vh-100">
      <LoginBox handleGoogleLogin={handleGoogleLogin} handleGuestLogin={handleGuestLogin} />
    </div>
  );
};

export default LoginPage;
