module.exports = {
  content: [
    './src/**/*',
  ],
  theme: {
    extend: {
      colors: {
        'primary': '#343a40',
        'accent': '#009b50',
        'accent-dark': '#008a3f',
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}
