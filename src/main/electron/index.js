const http = require('http');
const fs = require('fs');
const path = require('path');
const { parentPort } = require('electron');

const axios = require('axios');
const xml2js = require('xml2js');
const https = require('https');

const port = 8080;
const hostname = '127.0.0.1';
var mensagemElectron = '';
var retornoXML = '';
var ultimoDedo = '';

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
            console.log(body);
            chamaElectron(body, response);
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
            if (filePath !== '/biometria'){
                 response.writeHead(200, { 'Content-Type': contentType });
                 response.end(content, 'utf-8');
            //}
            }
         });
    }
});

server.listen(port, hostname, () => {
    console.log(`Server running at http://${hostname}:${port}/`);
});

process.parentPort.on('message', (e) => {
   console.log('Mensagem recebida do electron: ' + e.data);
   if (e.data.includes('Electron')){
       //sinalizador que recebeu mensagem do Electron
       mensagemElectron = e.data;
   }
})

async function chamaElectron(body, response) {
      if (body.includes('Capture')) {
         if (body.includes('validate')) {
            var contador = 0;
            callUnimedbhAutenticarBiometriaV2('8520971', 'POLEGAR_E', ultimoDedo, '?', '', '00060504143262000', '')
            .then(response => {
                if (response) {
                  console.log('Response resultado:', response.resultado);
                  console.log('Response token:', response.token);
                  responseXml = response.resultado;
                }
              })
              .catch(error => {
                console.error('Error in call:', error);
              });
            while (responseXml = ''){
               await sleep(200);
               contador++;
               if (contador > 5){
               //Se nao chegar nenhuma mensagem do SOAP em 1000ms
                   responseXml = 'Timeout no serviço SOAP'
               }
            }
            if (responseXml.includes('Timeout')){
               response.statusCode = 400;
               response.end(JSON.stringify({ message: 'Timeout no SOAP'}));
            } else {
                response.statusCode = 200;
                //remove o cabeçalho "Electron: " da mensagem sinalizadora
                response.end(JSON.stringify({ message: responseXml}));
            }
            ultimoDedo = '';
            responseXml = '';
         }
         else {
             var contador = 0;
             responseXml = '';
             process.parentPort.postMessage(body);
             //nao pode colocar listener do parentPort aqui dentro
             //senao vai abrir um listener toda vez que chamar essa funcao
             //dando memory leak
             while (!mensagemElectron.includes('Electron')){
                  //verifica variavel sinalizadora a cada 100ms, 5 vezes
                  //Quando contem "Electron", significa que chegou a resposta do Electron
                  await sleep(100);
                  contador++;
                  if (contador > 5){
                     //Se nao chegar nenhuma mensagem do Electron em 500ms
                     mensagemElectron = 'Electron erro'
                  }
             }
             if (mensagemElectron.includes('erro')){
                  response.statusCode = 400;
                  response.end(JSON.stringify({ message: 'Timeout no electron'}));
             } else {
                  if (mensagemElectron.substring(10).includes(':')){
                     mensagemElectronVetor = mensagemElectron.substring(10).split(':');
                     ultimoDedo = mensagemElectronVetor[1];
                  }
                  response.statusCode = 200;
                  //remove o cabeçalho "Electron: " da mensagem sinalizadora
                  response.end(JSON.stringify({ message: mensagemElectron.substring(10)}));
             }
             mensagemElectron = '';
          }
      } else if (body.includes('Version')){
         var contador = 0;
         responseXml = '';
         process.parentPort.postMessage(body);
         while (!mensagemElectron.includes('Electron')){
             await sleep(200);
             contador++;
             if (contador > 5){
                mensagemElectron = 'Electron erro'
             }
         }
          if (mensagemElectron.includes('erro')){
              response.statusCode = 400;
              response.end(JSON.stringify({ message: 'Timeout no electron'}));
          } else {
               response.statusCode = 200;
               response.end(JSON.stringify({ message: mensagemElectron.substring(10)}));
          }
          mensagemElectron = '';
      }
      else {
          response.writeHead(400, { 'Content-Type': 'application/json' });
          response.end(JSON.stringify({ message: 'Comando inválido' }));
          mensagemElectron = '';
      }
}

const sleep = (msec) => new Promise((resolve, _) => {
  setTimeout(resolve, msec);
});



async function callUnimedbhAutenticarBiometriaV2(idPessoa, codDedoBiometria, strDigital, codigoLocalCadastro, loginInclusao, numeroCarteira, codigoGrupo) {
  const soapRequest = `
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tip="http://www.unimedbh.com.br/ws/confirmacaopresenca/tipos">
      <soapenv:Header/>
      <soapenv:Body>
        <tip:unimedbhAutenticarBiometriaV2_Operation>
          <tip:dadosBiometriaAutenticacao>
            <idPessoa>${idPessoa}</idPessoa>
            <codDedoBiometria>${codDedoBiometria}</codDedoBiometria>
            <strDigital>${strDigital}</strDigital>
            <codigoLocalCadastro>${codigoLocalCadastro}</codigoLocalCadastro>
            <loginInclusao>${loginInclusao}</loginInclusao>
            <numeroCarteira>${numeroCarteira}</numeroCarteira>
            <codigoGrupo>${codigoGrupo}</codigoGrupo>
          </tip:dadosBiometriaAutenticacao>
        </tip:unimedbhAutenticarBiometriaV2_Operation>
      </soapenv:Body>
    </soapenv:Envelope>`;

  const config = {
    headers: {
      'Content-Type': 'text/xml; charset=utf-8',
      'Content-Length': Buffer.byteLength(soapRequest)
    },
    httpsAgent: new https.Agent({
      rejectUnauthorized: false
    })
  };

  try {
    const response = await axios.post('https://biometriawshml.unimedbh.com.br/ws-VerificaPresenca?wsdl', soapRequest, config);
    const responseXml = response.data;

    const parser = new xml2js.Parser({ explicitArray: false });
    const result = await parser.parseStringPromise(responseXml);

    const envelope = result['soap:Envelope'];
    const body = envelope && envelope['soap:Body'];
    const responseOperation = body && body['ns2:unimedbhAutenticarBiometriaV2_OperationResponse'];
    const respostaProcessaDadosBiometria = responseOperation && responseOperation['ns2:respostaProcessaDadosBiometria'];
    const resultado = respostaProcessaDadosBiometria && respostaProcessaDadosBiometria['resultado'];
    const token = respostaProcessaDadosBiometria && respostaProcessaDadosBiometria['token'];

    if (resultado) {
      return { resultado, token: token || null };
    } else {
      console.error('Unexpected response structure:', JSON.stringify(result, null, 2));
      return null;
    }
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
}