const {app, BrowserWindow, Notification} = require('electron');
const path = require('path');
//const { autoUpdater } = require("electron-updater");

let win;
let serverProcess;

app.allowRendererProcessReuse = true;

global.callElectronUiApi = function () {
   console.log(app.getVersion())
}

function createWindow() {

    let platform = process.platform;
    console.log(platform)

    if (platform === 'win32') {

        const NOTIFICATION_TITLE = 'Basic Notification'
        const NOTIFICATION_BODY = process.env.path
        new Notification({
          title: NOTIFICATION_TITLE,
          body: NOTIFICATION_BODY
        }).show();

        //if (app.isPackaged) {
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
            title: 'Demo',
            width: 640,
            height: 480
        });

        mainWindow.loadURL(appUrl);

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
            }, 200);
        });
    };

    startUp();
}

app.on('ready', createWindow);

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