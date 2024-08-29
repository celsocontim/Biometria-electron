const http = require("http");
const fs = require("fs");
const path = require("path");
const { parentPort } = require("electron");

const port = 8000;
const hostname = "127.0.0.1";
var tokenAcesso = "";
var mensagemElectron = "";
var ultimoDedo = "";

const server = http.createServer((request, response) => {
  console.log("request ", request.url);

  // Set CORS headers
  response.setHeader("Access-Control-Allow-Origin", "*");
  response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  response.setHeader("Access-Control-Allow-Headers", "Content-Type");

  if (request.method === "POST" && request.url === "/biometria") {
    let body = "";

    request.on("data", (chunk) => {
      body = chunk.toString();
    });

    request.on("end", () => {
      console.log(body);
      chamaElectron(body, response);
    });
  } else {
    let filePath =
      request.url === "/"
        ? "resources/public/index.html"
        : "resources/public" + request.url;
    let extname = String(path.extname(filePath)).toLowerCase();
    let mimeTypes = {
      ".html": "text/html",
      ".js": "text/javascript",
      ".css": "text/css",
      ".json": "application/json",
      ".png": "image/png",
      ".jpg": "image/jpg",
      ".gif": "image/gif",
      ".svg": "image/svg+xml",
      ".wav": "audio/wav",
      ".mp4": "video/mp4",
      ".woff": "application/font-woff",
      ".ttf": "application/font-ttf",
      ".eot": "application/vnd.ms-fontobject",
      ".otf": "application/font-otf",
      ".wasm": "application/wasm",
      ".ico": "image/x-icon",
    };

    let contentType = mimeTypes[extname] || "application/octet-stream";

    fs.readFile(filePath, function (error, content) {
      if (filePath !== "/biometria") {
        response.writeHead(200, { "Content-Type": contentType });
        response.end(content, "utf-8");
      }
    });
  }
});

server.listen(port, hostname, () => {
  console.log(`Server running at http://${hostname}:${port}/`);
});

process.parentPort.on("message", (e) => {
  console.log("Mensagem recebida do electron: " + e.data);
  if (e.data.includes("Electron")) {
    //sinalizador que recebeu mensagem do Electron
    mensagemElectron = e.data;
  }
});

const fazerLogin = async () => {
  const myBody = {
    username: "servico.facial",
    password: "8if#PbS1",
  };

  try {
    const response = await fetch(
      "https://apihml.unimedbh.io/security/v1/login",
      {
        method: "POST",
        body: JSON.stringify(myBody),
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }

    const myJson = await response.json();
    console.log (myJson.access_token);
    return myJson.access_token;
  } catch (error) {
    console.error("Erro:", error);
  }
};

async function verificarDedoComRetry() {
  const maxTentativas = 3;
  let tentativas = 0;
  while (tentativas < maxTentativas) {
    try {
      let response = await verificaDedoTeste(ultimoDedo);

      if (response) {
        console.log("Response resultado:", response);
        return;
      }

      if (response.data === 401) {
        console.log("Token expirado ou inválido, realizando login...");
        tokenAcesso = await fazerLogin();
        tentativas++;
      } else {
        break; 
      }
    } catch (error) {
      console.error("Erro na chamada:", error);
      break; 
    }
  }
}

async function chamaElectron(body, response) {
  if (body.includes("Capture")) {
    if (body.includes("validate")) {
      var contador = 0;

      if (tokenAcesso === "") {
        tokenAcesso = await fazerLogin();
      }

      await verificarDedoComRetry();
    } else {
      var contador = 0;
      process.parentPort.postMessage(body);
      //nao pode colocar listener do parentPort aqui dentro
      //senao vai abrir um listener toda vez que chamar essa funcao
      //dando memory leak
      while (!mensagemElectron.includes("Electron")) {
        //verifica variavel sinalizadora a cada 100ms, 5 vezes
        //Quando contem "Electron", significa que chegou a resposta do Electron
        await sleep(100);
        contador++;
        if (contador > 5) {
          //Se nao chegar nenhuma mensagem do Electron em 500ms
          mensagemElectron = "Electron erro";
        }
      }
      if (mensagemElectron.includes("erro")) {
        response.statusCode = 400;
        response.end(JSON.stringify({ message: "Timeout no electron" }));
      } else {
        if (mensagemElectron.substring(10).includes(":")) {
          mensagemElectronVetor = mensagemElectron.substring(10).split(":");
          ultimoDedo = mensagemElectronVetor[1];
        }
        response.statusCode = 200;
        //remove o cabeçalho "Electron: " da mensagem sinalizadora
        response.end(
          JSON.stringify({ message: mensagemElectron.substring(10) })
        );
      }
      mensagemElectron = "";
    }
  } else if (body.includes("Version")) {
    var contador = 0;
    process.parentPort.postMessage(body);
    while (!mensagemElectron.includes("Electron")) {
      await sleep(200);
      contador++;
      if (contador > 5) {
        mensagemElectron = "Electron erro";
      }
    }
    if (mensagemElectron.includes("erro")) {
      response.statusCode = 400;
      response.end(JSON.stringify({ message: "Timeout no electron" }));
    } else {
      response.statusCode = 200;
      response.end(JSON.stringify({ message: mensagemElectron.substring(10) }));
    }
    mensagemElectron = "";
  } else {
    response.writeHead(400, { "Content-Type": "application/json" });
    response.end(JSON.stringify({ message: "Comando inválido" }));
    mensagemElectron = "";
  }
}

const sleep = (msec) =>
new Promise((resolve, _) => {
  setTimeout(resolve, msec);
});

async function verificaDedoTeste (strDigital) {
   try {
      const requestBody = {
         dedo: strDigital
      }
      const response = await fetch("http://localhost:8080/verifica-dedo", {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              "access_token": tokenAcesso
            },
            body: JSON.stringify(requestBody)
          });

          if (!response.ok) {
            console.log (response)
            throw new Error(`Erro na requisicaoo: ${response.statusText}`);
          }

          const data = await response.json();

          return data;
        } catch (error) {
          console.error("Error:", error);
          throw error;
        }
};

  