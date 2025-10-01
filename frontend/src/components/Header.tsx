"use client";

import Image from "next/image";
import { usePathname } from "next/navigation";
import User from "../models/User";

interface Props {
  user: User | null;
  loading?: boolean;
}

// TODO: check loading
const Header: React.FC<Props> = ({ user, loading = false }) => {
  function getCookie(name: string) {
    const match = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
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
  const isLoading = loading || !user;
  const displayName = user?.privateUserObject.displayName ?? "";
  const avatarUrl =
    user &&
    user.privateUserObject.images &&
    user.privateUserObject.images.length > 0
      ? user.privateUserObject.images[0].url
      : "/default-user-pfp.png";

  return (
    <div className="header">
      <div className={`user-info${isLoading ? " user-info--loading" : ""}`}>
        <div className="header-avatar-slot">
          {isLoading ? (
            <div className="skeleton-circle header-avatar-skeleton" />
          ) : (
            <Image
              src={avatarUrl}
              alt={displayName || "Spotify user"}
              width={48}
              height={48}
              className="artist-image"
            />
          )}
        </div>
        <div className="header-display-slot">
          {isLoading ? (
            <div className="skeleton-line header-name-skeleton" />
          ) : (
            <span>Logged in as {displayName}</span>
          )}
        </div>
      </div>
      <div className="header-actions">
        {pathname === "/" ? (
          <button onClick={donate}>
            Donate <span style={{ fontSize: "13px" }}>❤️</span>
          </button>
        ) : (
          <button onClick={toHome}>Home</button>
        )}

        <button onClick={logout}>Logout</button>
      </div>
    </div>
  );
};

export default Header;
