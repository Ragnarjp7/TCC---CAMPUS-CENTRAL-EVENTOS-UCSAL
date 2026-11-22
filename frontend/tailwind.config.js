/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./App.tsx", "./src/**/*.{ts,tsx}"],
  presets: [require("nativewind/preset")],
  theme: {
    extend: {
      colors: {
        // Troque aqui pelo azul oficial da UCSAL
        brand: { DEFAULT: "#0a3cd8", dark: "#082fa8", soft: "#eff6ff" },
      },
    },
  },
  plugins: [],
};
