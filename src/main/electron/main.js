const {app, utilityProcess, BrowserWindow, Notification, Tray, Menu, ipcMain, globalShortcut} = require('electron');
const path = require('path');
const { autoUpdater } = require("electron-updater");
const fs = require ('fs');
const net = require('net');

let win;
let serverProcess;
var cadastroSucesso = 0;
var score;

app.allowRendererProcessReuse = true;

const logDirectory = path.join(app.getPath("userData"), "logs");
if (!fs.existsSync(logDirectory)) {
  fs.mkdirSync(logDirectory);
}

const logFilePath = path.join(logDirectory, "notifications.log");

function logNotification(message) {
  const logMessage = `${new Date().toISOString()} - ${message}\n`;
  fs.appendFileSync(logFilePath, logMessage, "utf8");
}

function showNotification(title, body) {
  const notificationMessage = `${title}: ${body}`;
  new Notification({ title: title, body: body }).show();
  logNotification(notificationMessage);
}

function iniciaJava() {

    let platform = process.platform;

    if (platform === 'win32') {
           serverProcess = require('child_process')
                    .spawn('cmd.exe', ['/c', 'demo.bat']
                           ,{
                               cwd: './resources/bin'
                           },{ shell: true })
                       .on('error', function( err ){ throw err });
    } else {
        serverProcess = require('child_process')
            .spawn(app.getAppPath() + '\\resources\\bin\\demo',
            { shell: true });
    }

    if (!serverProcess) {
        console.error('Unable to start java from ' + app.getAppPath());
        app.quit();
        return;
    }

    serverProcess.stdout.on('data', function (data) {
        console.log('Java process: ' + data);
    });

    console.log("Java PID: " + serverProcess.pid);
    return serverProcess;
}


function mataJava(serverProcess) {
     if (serverProcess) {
         const kill = require('tree-kill');
         kill(serverProcess.pid, 'SIGTERM', function () {
            console.log('Server process killed');
         });
         serverProcess = null;
         console.log('Server process killed');
     }
};

function createWindow() {

    let platform = process.platform;
    console.log(cadastroSucesso);

    const HTTPServer = utilityProcess.fork(path.join(__dirname, 'index.js'));
    HTTPServer.on('spawn', function (){
      console.log("Servidor HTTP iniciado! Process ID: ", HTTPServer.pid);
    });

    serverProcess = new iniciaJava();

    let appUrl = 'http://localhost:8000/';

    const openWindow = function () {
        mainWindow = new BrowserWindow({
            title: 'Demo-app-biometria',
            width: 640,
            height: 480
        });

        mainWindow.loadURL(appUrl + "/cadastro.html");

        mainWindow.on('closed', function () {
            mainWindow = null;
        });

        mainWindow.on('minimize',function(event){
            event.preventDefault();
            mainWindow.hide();
        });

        mainWindow.on('close', function (e) {
           console.log("Matando processos...");
           mataJava(serverProcess);
           HTTPServer.kill();
        });

        HTTPServer.on('message', (data) => {
          console.log('Mensagem recebida do processo filho: ', data);
          var string = (data.toString());
          if (string.includes("Capture")){
               var client = new net.Socket();
               client.connect(2222, '127.0.0.1', function() {
               	console.log('Connected');
               	client.write(string + '\n');
                  client.on('data', function(retorno) {
                     console.log('Received: ' + retorno);
                     var stringRetorno = (retorno.toString());
                     HTTPServer.postMessage('Electron: ' + stringRetorno)
                     client.destroy();
                  });
               });

          }
          if (string.includes("getVersion")){
               HTTPServer.postMessage('Electron: ' + app.getVersion());
          }
        })

        let trayIcon = null
        if(!app.isPackaged) {
                  trayIcon = 'resources/icon.ico'; // when in dev mode
        } else {
                  trayIcon = './resources/icon.ico';
        }
        tray = new Tray(trayIcon);
        tray.setToolTip('Biometria UnimedBH')
                //mainWindow.minimize();
        tray.setContextMenu(Menu.buildFromTemplate([
          {
            label: 'Abrir', click: function () {
              mainWindow.show();
            }
          },
          {
            label: 'Sair', click: function () {
              isQuiting = true;
              mainWindow.close();
              app.quit();
            }
          }
        ]));
        tray.on('double-click', () => {
                    mainWindow.isVisible()?mainWindow.hide():mainWindow.show();
        })
        mainWindow.hide();
    };

    const startUp = function () {
        const requestPromise = require('minimal-request-promise');

        requestPromise.get(appUrl).then(function (response) {
            console.log('Server started!');
            openWindow();
        }, function (response) {
            console.log('Waiting for the server start...');
            setTimeout(function () {
                startUp();
            }, 5000);
        });
    };

    startUp();
}

app.on('ready', function() {
   createWindow();
   console.log(app.getVersion());

   autoUpdater.checkForUpdatesAndNotify();

   autoUpdater.on("checking-for-update", () => {
       const message = "Checking for update...";
       console.log(message);
       showNotification("Checking for Update", message);
   });
   autoUpdater.on("update-available", (info) => {
       const message = "Update available.";
       console.log(message, info);
       showNotification("Update Available", message);
     });
   autoUpdater.on("update-not-available", (info) => {
       const message = "Update not available, current version: " + app.getVersion();
       console.log(message, info);
       showNotification("Update Not Available", message);
   });
     autoUpdater.on("error", (err) => {
       const message = `Error in auto-updater: ${err}`;
       console.log(message);
       showNotification("Error", message);
   });
   autoUpdater.on("download-progress", (progressObj) => {
         let log_message = "Download speed: " + progressObj.bytesPerSecond;
         log_message = log_message + " - Downloaded " + progressObj.percent + "%";
         log_message =
           log_message +
           " (" +
           progressObj.transferred +
           "/" +
           progressObj.total +
           ")";
         console.log(log_message);
         showNotification("Download Progress", log_message);
     });
     autoUpdater.on("update-downloaded", (info) => {
         const message = "Update downloaded; will install in 5 seconds";
         console.log(message, info);
         showNotification("Update Downloaded", message);
         setTimeout(function () {
           autoUpdater.quitAndInstall();
         }, 5000);
     });
});


app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        globalShortcut.unregisterAll();
        app.quit();
    }
});

app.on('activate', () => {
    if (win === null) {
        createWindow()
    }
});