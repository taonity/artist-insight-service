"use client";

import Image from "next/image";
import { usePathname } from "next/navigation";
import User from "../models/User";

interface Props {
  user: User | null;
  loading?: boolean;
  visitorMode?: boolean;
}

// TODO: check loading
const Header: React.FC<Props> = ({ user, loading = false, visitorMode = false }) => {
  const toSettingsPage = async () => {
    window.location.href = "/settings";
  };

  const donate = async () => {
    window.location.href = "/donate";
  };

  const toHome = async () => {
    window.location.href = "/";
  };

  const login = async () => {
    window.location.href = "/login";
  };

  const pathname = usePathname();
  const isLoading = !visitorMode && (loading || !user);
  const displayName = visitorMode ? "Visitor" : (user?.privateUserObject.displayName ?? "");
  const avatarUrl = visitorMode 
    ? "/default-user-pfp.png"
    : (user &&
        user.privateUserObject.images &&
        user.privateUserObject.images.length > 0
          ? user.privateUserObject.images[0].url
          : "/default-user-pfp.png");

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
          ) : visitorMode ? (
            <span>Viewing as Visitor</span>
          ) : (
            <span>Logged in as {displayName}</span>
          )}
        </div>
      </div>
      <div className="header-actions">
        {visitorMode ? (
          <button onClick={login}>Log in</button>
        ) : (
          <>
            {pathname === "/" ? (
              <button onClick={donate}>
                Donate <span style={{ fontSize: "13px" }}>❤️</span>
              </button>
            ) : (
              <button onClick={toHome}>Home</button>
            )}
            <button onClick={toSettingsPage}>Settings</button>
          </>
        )}
      </div>
    </div>
  );
};

export default Header;
