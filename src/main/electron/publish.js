const { execSync } = require("child_process");
require("dotenv").config();

process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";

//console.log("GH_TOKEN:", process.env.GH_TOKEN);

execSync("electron-builder --publish always", {
  stdio: "inherit",
  env: process.env,
});
