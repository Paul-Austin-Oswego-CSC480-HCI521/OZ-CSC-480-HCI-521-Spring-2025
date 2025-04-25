import { createContext, useEffect, useState } from "react";
import { fetchMe } from "./api";

export const UserContext = createContext(null)

export const AlertContext = createContext(null)

export const UserProvider = (props) => {

  // Fetch user and return state in value
  const [user, setUser] = useState(null);
  useEffect(() => {
    (async () => {
      try {
        console.log("Fetching user..."); 
        const data = await fetchMe();
        if (data != null) {
          setUser(data);
        }
      } catch (err) {
        console.error("Error fetching user", err);
      } 
    })();
  }, []);

  return <UserContext.Provider value={[user, setUser]}>{props.children}</UserContext.Provider>
}

export const AlertProvider = (props) => {

  // Fetch user and return state in value
  const [alertMessage, sendAlert] = useState(null);

  return <AlertContext.Provider value={[alertMessage, sendAlert]}>{props.children}</AlertContext.Provider>
}