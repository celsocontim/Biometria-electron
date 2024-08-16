const dotenv = require ("dotenv");
dotenv.config();

process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";

const { execSync } = require("child_process");

execSync("electron-builder", {
  stdio: "inherit",
  env: process.env,
});
