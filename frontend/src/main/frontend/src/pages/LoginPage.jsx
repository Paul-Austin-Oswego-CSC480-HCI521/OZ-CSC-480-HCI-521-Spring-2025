import LoginBox from '../components/Login';
import { FocusTrap } from 'focus-trap-react';


const LoginPage = ({ setIsGuest }) => {

  return (
    <FocusTrap>
      <div 
        className="d-flex justify-content-center align-items-center vh-100"
      >
        <LoginBox/>
      </div>
    </FocusTrap>
  );
};

export default LoginPage;
