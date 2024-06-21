const {app, BrowserWindow, Notification, Tray} = require('electron');
const path = require('path');
const { autoUpdater } = require("electron-updater");
const fs = require ('fs');
const builder = require('builder-util-runtime')

let win;
let serverProcess;

app.allowRendererProcessReuse = true;

global.callElectronUiApi = function () {
   if (arguments){
      switch (arguments[0]) {
         //cada chamada será um case dentro do switch
         case 'maximize':
            if (!mainWindow.isMaximized()) {
               mainWindow.maximize();
            } else {
               mainWindow.unmaximize();
            }
            break;
      }
   }
}

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

function createWindow() {

    let platform = process.platform;
    console.log(platform)

    if (platform === 'win32') {
       /* const NOTIFICATION_TITLE = 'Notificação Teste'
        const NOTIFICATION_BODY = process.env.path
        new Notification({
          title: NOTIFICATION_TITLE,
          body: NOTIFICATION_BODY
        }).show();
      */
           serverProcess = require('child_process')
                    .spawn('cmd.exe', ['/c', 'demo.bat']
                           ,{
                               cwd: './demo/bin'
                           },{ shell: true })
                       .on('error', function( err ){ throw err });
    } else {
        serverProcess = require('child_process')
            .spawn(app.getAppPath() + '\\demo\\bin\\demo',
            { shell: true });
    }

    if (!serverProcess) {
        console.error('Unable to start server from ' + app.getAppPath());
        app.quit();
        return;
    }

    serverProcess.stdout.on('data', function (data) {
        console.log('Server: ' + data);
    });

    console.log("Server PID: " + serverProcess.pid);

    let appUrl = 'http://localhost:8080';

    const openWindow = function () {
        mainWindow = new BrowserWindow({
            title: 'Demo-app-biometria',
            width: 640,
            height: 480
        });

        mainWindow.loadURL(appUrl);
        tray = new Tray('icon.png');
        tray.setToolTip('Biometria UnimedBH')
        //mainWindow.minimize();
        tray.on('double-click', () => {
            mainWindow.isVisible()?mainWindow.hide():mainWindow.show();
        })
        mainWindow.hide();

        mainWindow.on('closed', function () {
            mainWindow = null;
        });

        mainWindow.on('close', function (e) {
            if (serverProcess) {
                e.preventDefault();

                // kill Java executable
                const kill = require('tree-kill');
                kill(serverProcess.pid, 'SIGTERM', function () {
                    console.log('Server process killed');
                    serverProcess = null;
                    mainWindow.close();
                });
            }
        });
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
            }, 1000);
        });
    };

    startUp();
}

app.on('ready', function() {
   createWindow();
   console.log(app.getVersion())

/*   autoUpdater.setFeedURL({
     provider: "github",
     owner: "celsocontim",
     repo: "Biometria-electron",
   });
*/
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

app.setLoginItemSettings({
    openAtLogin: true,
    path: app.getPath("exe")
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit()
    }
});

app.on('activate', () => {
    if (win === null) {
        createWindow()
    }
});