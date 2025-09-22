import { useEffect, useState } from 'react'
import User from '../models/User'
import keysToCamel from '../utils/utils'

export function useUser(setErrorMessage: React.Dispatch<React.SetStateAction<string | null>>) {
  const [user, setUser] = useState<User | null>(null);
  

  const fetchUser = async () => {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 6000);
    try {
      const res = await fetch("/api/user", {
        credentials: "include",
        signal: controller.signal,
      });
      if (res.status === 401) {
        window.location.href = "/login";
        return;
      }
      if (res.status === 504) {
        setErrorMessage("Request timed out. Please try again.");
        return;
      }
      if (res.ok) {
        const snakeCasedUser = await res.json();
        const camelCasedUser = keysToCamel(snakeCasedUser);
        setUser(camelCasedUser);
      } else {
        window.location.href = "/login";
      }
    } catch (err) {
      if (err instanceof Error && err.name === "AbortError") {
        setErrorMessage("Request timed out. Please try again.");
      } else {
        setErrorMessage(
          "Unable to connect to the server. Please check your connection."
        );
      }
    } finally {
      clearTimeout(timeoutId);
    }
  };

  useEffect(() => {
    fetchUser();
  }, []);

  return user;
}
