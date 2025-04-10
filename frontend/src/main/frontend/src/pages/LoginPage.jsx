import LoginBox from '../components/Login';


const LoginPage = ({ setIsGuest }) => {

  return (
    <div 
      className="d-flex justify-content-center align-items-center vh-100"
    >
      <LoginBox/>
    </div>
  );
};

export default LoginPage;
