const http = require ('http');
const fs = require('fs');
const path = require('path');
const kill = require('tree-kill');

const port = 8080;
const hostname = '127.0.0.1';

const server = http.createServer(function (request, response) {
        console.log('request ', request.url);
        let filePath = request.url;

        if (filePath == '/') {
            filePath = 'resources/public/index.html';
        }
        else if (filePath == '/kill'){
            console.log("Stopping Server...");
            server.close();
        }
        else if (filePath == '/biometria'){
            console.log("Biometria detectada...");
            const headers = {
                'Content-Type': 'text/event-stream',
                'Connection': 'keep-alive',
                'Cache-Control': 'no-cache'
            };
            response.writeHead(200, headers);
            response.write("data: Biometria detectada")
        }
        else {
            filePath = 'resources/public' + request.url;
        }

        let extname = String(path.extname(filePath)).toLowerCase();
        let mimeTypes = {
            '.html': 'text/html',
            '.js': 'text/javascript',
            '.css': 'text/css',
            '.json': 'application/json',
            '.png': 'image/png',
            '.jpg': 'image/jpg',
            '.gif': 'image/gif',
            '.svg': 'image/svg+xml',
            '.wav': 'audio/wav',
            '.mp4': 'video/mp4',
            '.woff': 'application/font-woff',
            '.ttf': 'application/font-ttf',
            '.eot': 'application/vnd.ms-fontobject',
            '.otf': 'application/font-otf',
            '.wasm': 'application/wasm',
            '.ico' : 'image/x-icon'
        };

        let contentType = mimeTypes[extname] || 'application/octet-stream';

        fs.readFile(filePath, function(error, content) {
           /*
           if (error) {
                if(error.code == 'ENOENT') {
                    fs.readFile('public/404.html', function(error, content) {
                        response.writeHead(404, { 'Content-Type': 'text/html' });
                        response.end(content, 'utf-8');
                    });
                }
                else {
                    response.writeHead(500);
                    response.end('Sorry, check with the site admin for error: '+error.code+' ..\n');
                }
            }

            else {
           */
           if (filePath == '/documentation') {
               response.setHeader('Content-Type', 'text/event-stream');
               response.setHeader('Cache-Control', 'no-cache');
               response.setHeader('Content-Encoding', 'none');
               response.setHeader('Connection', 'keep-alive');
               response.setHeader('Access-Control-Allow-Origin', '*');
               response.flushHeaders();
           }
           else if (filePath !== '/biometria'){
                response.writeHead(200, { 'Content-Type': contentType });
                response.end(content, 'utf-8');
           //}
           }
        });
    });

server.listen(port, hostname, () => {
  console.log(`Server running at http://${hostname}:${port}/`);
});

