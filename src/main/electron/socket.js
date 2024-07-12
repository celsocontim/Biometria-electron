const fs = require('fs');
const path = require('path');
const kill = require('tree-kill');
const net = require('net');
const { parentPort } = require('electron');

const port = 3001;
const hostname = '127.0.0.1';

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

net.createServer(function(sock) {

    console.log('CONNECTED: ' + sock.remoteAddress +':'+ sock.remotePort);

    sock.on('data', function(data) {
        // dados foram recebidos no socket
        var string = (data.toString());
        console.log(string);
        sock.write(data);
        process.parentPort.postMessage(string);
    });

    sock.on('close', function(data) {
        // conexão fechada
        console.log('CLOSED: ' + sock.remoteAddress +' '+ sock.remotePort);
    });

}).listen(port, hostname , ()  => {
             console.log('Server listening on ' + hostname +':'+ port);
             global.javaProcess = iniciaJava();
   });
