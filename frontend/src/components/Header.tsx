"use client";

import Image from "next/image";
import User from "../models/User";
import { usePathname } from 'next/navigation';

interface Props {
  user: User;
}

const Header: React.FC<Props> = ({ user }) => {
  function getCookie(name: string) {
    const match = document.cookie.match(
      new RegExp("(^| )" + name + "=([^;]+)")
    );
    return match ? decodeURIComponent(match[2]) : null;
  }

  const logout = async () => {
    const xsrfToken = getCookie("XSRF-TOKEN");
    await fetch("/api/logout", {
      method: "POST",
      credentials: "include",
      headers: xsrfToken ? { "X-XSRF-TOKEN": xsrfToken } : {},
    });
    window.location.href = "/login";
  };

  const donate = async () => {
    window.location.href = "/donate";
  };

    const toHome = async () => {
    window.location.href = "/";
  };

  const pathname = usePathname();

  return (
    <div className="header">
      <div className="user-info">
        <Image
          src={
            user.privateUserObject.images &&
            user.privateUserObject.images.length > 0
              ? user.privateUserObject.images[0].url
              : "/default-user-pfp.png"
          }
          alt={user.privateUserObject.displayName}
          width={48}
          height={48}
          className="artist-image"
        />
        <div>Logged in as {user.privateUserObject.displayName}</div>
      </div>
      <div>
        {pathname === "/" ? (
          <button onClick={donate}>
            Donate <span style={{ fontSize: "13px" }}>❤️</span>
          </button>
        ) : (
          <button onClick={toHome}>
            Home
          </button>
        )}
        
        <button onClick={logout}>Logout</button>
      </div>
    </div>
  );
};

export default Header;
