import { useEffect, useContext } from "react";
import { AlertContext } from "../lib/Contexts";

const AlertMessage = ({ autoDismiss = false, dismissTime = 3000 }) => {
  const [alert, setAlert] = useContext(AlertContext);

  useEffect(() => {
    if (autoDismiss) {
      const timer = setTimeout(() => setAlert(null), dismissTime);
      return () => clearTimeout(timer);
    }
  }, [autoDismiss, dismissTime]);

  return alert && 
    <div className={`alert ${alert.type == "success" ? "alert-success" : "alert-danger"} alert-dismissible fade show`} role="alert" onClick={(e) => e.stopPropagation()}>
      {alert.message}
      <button type="button" className="btn-close" onClick={() => setAlert(null)} aria-label="Close"></button>
    </div>
};

export default AlertMessage;
