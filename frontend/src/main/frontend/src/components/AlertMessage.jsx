import { useState, useEffect } from "react";

const AlertMessage = ({ type = "success", message, autoDismiss = false, dismissTime = 3000 }) => {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    if (autoDismiss) {
      const timer = setTimeout(() => setVisible(false), dismissTime);
      return () => clearTimeout(timer);
    }
  }, [autoDismiss, dismissTime]);

  if (!visible) return null;

  const alertClass = type === "success" ? "alert-success" : "alert-danger";

  return (
    <div className={`alert ${alertClass} alert-dismissible fade show`} role="alert">
      {message}
      <button type="button" className="btn-close" onClick={() => setVisible(false)} aria-label="Close"></button>
    </div>
  );
};

export default AlertMessage;
