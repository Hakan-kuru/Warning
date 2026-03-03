module.exports = {
  env: {
    es6: true,
    node: true,
  },
  parserOptions: {
    'ecmaVersion': 2018,
  },
  extends: [
    'eslint:recommended',
    'google',
  ],
  rules: {
    'linebreak-style': 0, // geçici olarak LF/CRLF hatalarını kapat
    // "no-restricted-globals": ["error", "name", "length"],
    // "prefer-arrow-callback": "error",
    // "quotes": ["error", "double", {"allowTemplateLiterals": true}],
  },
  overrides: [
    {
      files: ['**/*.spec.*'],
      env: {
        mocha: true,
      },
      rules: {},
    },
  ],
  globals: {},
};
