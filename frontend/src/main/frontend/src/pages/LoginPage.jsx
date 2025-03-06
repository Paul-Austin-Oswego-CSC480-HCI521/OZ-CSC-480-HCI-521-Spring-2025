import React,{useState, useEffect} from 'react';
import LoginBox from '../components/Login';


const LoginPage = ({ setIsAuthenticated, setIsGuest }) => {
  const [user, setUser] = useState(null)

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
    setIsGuest(true);
  };

  return (
    <div className="d-flex justify-content-center align-items-center vh-100">
      <LoginBox handleGoogleLogin={handleGoogleLogin} handleGuestLogin={handleGuestLogin} />
    </div>
  );
};

export default LoginPage;
