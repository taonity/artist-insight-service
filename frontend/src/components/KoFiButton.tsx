"use client";

import '../styles/kofi-button.scss'

interface Props {
    username: string
    label: string
    title: string
    preset?: string
    backgroundColor?: ColorName
    animation?: string
}

// TODO: use ts
// and [true, false, "true", "false", "on_hover"] for animation
const known_presets = ["", "default", "thin", "skinny", "circle", "no_background"];

const colors = {
  kofiWhite: "#FFF",
  kofiBlack: "#000",
  kofiRed: "#FF5E5B",
  kofiBlue: "#13C3FF",
  kofiYellow: "#FBAA19",
  kofiGrey: "#434B57",
  no_background: "transparent"
} as const;

type ColorName = keyof typeof colors;

function kofiColors(color: ColorName): string {
  return colors[color] ? colors[color] : color;
}

const NEXT_PUBLIC_KOFI_URL = process.env.NEXT_PUBLIC_KOFI_URL

const KoFiButton: React.FC<Props> = ({ 
  username, 
  label = "Support Me on Ko-fi", 
  title = "", 
  preset, 
  backgroundColor = "no_background", 
  animation = true
}) => {

  var profile_url = NEXT_PUBLIC_KOFI_URL + "/" + username;

  if (preset && !known_presets.includes(preset)) {
    console.warn("Unknown preset \"".concat(preset, "\", reverting to default"));
    preset = "";
  }

  if (preset === "default") {
    preset = "";
  }

  return (
    <div className="KofiContainer" style={{ backgroundColor: kofiColors(backgroundColor) }}>
      <a
        className={`KofiButton ${preset}`}
        href={profile_url}
        target="_blank"
        rel="noreferrer noopener external"
        title={title}
      >
        <figure className="KofiImageContainer">
          <img className={`KofiImage animation_${animation}`} alt="" />
        </figure>
        {label && <span className="KofiText">{label}</span>}
      </a>
    </div>
  );
}

export default KoFiButton