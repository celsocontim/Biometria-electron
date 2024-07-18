const http = require('http');
const fs = require('fs');
const path = require('path');
const net = require('net');
const { parentPort } = require('electron');

const port = 8080;
const hostname = '127.0.0.1';
const javaSocketPort = 12345; // Porta que o Java estará ouvindo
const javaSocketHost = '127.0.0.1'; // Endereço do Java

const server = http.createServer((request, response) => {
    console.log('request ', request.url);

    // Set CORS headers
    response.setHeader('Access-Control-Allow-Origin', '*');
    response.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    response.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (request.method === 'POST' && request.url === '/biometria') {
        let body = '';

        request.on('data', chunk => {
            body = chunk.toString();
        });

        request.on('end', () => {
            console.log(body)
            if (body.includes('Capture')) {
                process.parentPort.postMessage(body);
                process.parentPort.on('message', (e) => {
                   console.log('Mensagem recebida do electron: ' + e.data);
                   //response.writeHead(200, { 'Content-Type': 'application/json' });
                   response.statusCode = 200;
                   response.end(JSON.stringify({ message: e.data}));
                })
            } else {
                response.writeHead(400, { 'Content-Type': 'application/json' });
                response.end(JSON.stringify({ message: 'Comando inválido' }));
            }
        });
    } else {
        let filePath = request.url === '/' ? 'resources/public/index.html' : 'resources/public' + request.url;
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
            '.ico': 'image/x-icon'
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
                response.end(content, 'utf-8');
            }
            else if (filePath !== '/biometria'){
                 response.writeHead(200, { 'Content-Type': contentType });
                 response.end(content, 'utf-8');
            //}
            }
         });
    }
});

/*
process.parentPort.on('message', (e) => {
   console.log('Mensagem recebida do electron: ' + e.data);
})
*/

server.listen(port, hostname, () => {
    console.log(`Server running at http://${hostname}:${port}/`);
});
