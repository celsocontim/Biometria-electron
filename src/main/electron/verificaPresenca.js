const axios = require('axios');
const xml2js = require('xml2js');
const https = require('https');

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

callUnimedbhAutenticarBiometriaV2('8520971', 'POLEGAR_E', '-89 -1 1 30 -41 1 1 96 0 -71 0 -46 0 1 0 0 -119 0 11 1 1 62 0 -30 0 -66 0 2 -92 0 -105 0 74 1 2 102 0 -33 0 27 1 2 119 0 -104 0 -86 0 2 17 0 10 1 -31 0 2 -121 0 122 0 -42 0 1 40 0 112 0 58 1 1 90 0 -84 0 126 0 1 6 0 -105 0 102 0 1 6 0 107 0 110 1 1 96 0 12 1 0 1 1 -126 0 97 0 63 1 1 85 0 5 1 36 1 1 113 0 -92 0 86 0 1 -76 0 -60 0 58 1 2 20 1 -55 0 79 1 2 -19 0 -93 0 13 1 1 -31 0 -48 0 94 1 2 -31 0 -63 0 102 1 2 6 0 -42 0 107 1 1 34 0 -81 0 86 1 2 119 0 6 1 93 1 1 85 0 -102 0 108 1 1 124 0 -112 0 -122 1 1 -126 0 -30 0 -120 1 2 -47 0 -47 0 -122 1 2 17 0 -14 0 125 1 2 51 0 -88 0 22 20 14 9 21 20 27 28 20 18 27 29 17 0 11 16 22 21 18 17 21 18 21 23 19 2 23 18 23 4 0 5 28 22 26 25 25 23 18 0 27 22 22 18 13 7 10 11 29 22 28 29 25 4 20 23 23 17 28 21 15 13 29 24 20 17 25 21 15 5 28 20 17 5 10 16 20 0 4 9 21 17 26 12 22 23 29 20 1 3 27 20 12 25 27 21 4 17 12 14 6 10 0 15 18 4 22 24 21 4 22 17 12 9 1 6 23 0 5 13 8 6 9 2 7 3 29 21 20 24 22 0 21 0 4 14 2 8 25 18 17 19 25 20 28 18 27 24 18 5 26 23 24 15 12 4 26 21 28 23 25 22 26 4 28 25 27 18 29 18 5 19 4 19 24 0 19 1 24 18 8 1 4 2 25 17 25 9 26 28 0 19 14 2 28 24 15 7 6 11 9 19 20 5 23 9 19 8 17 15 21 24 27 23 12 23 5 7 25 14 18 15 23 19 0 13 17 2 2 1 26 20 26 22 29 0 24 5 3 6 27 25 13 3 20 15 23 14 26 9 5 1 27 26 7 1 14 19 23 2 3 10 1 10 26 14 6 16 12 21 4 5 17 13 24 13 5 3 13 1 0 7 2 6 12 18 9 8 19 6 19 3 8 10 12 2 17 1 8 3 14 8 19 7 17 7 1 11 3 11 8 11 4 8 3 16 5 8 1 16 7 6 8 16 7 10 19 10 2 11 19 11 7 16 2 16', '?', '', '00060504143262000', '')
  .then(response => {
    if (response) {
      console.log('Response resultado:', response.resultado);
      console.log('Response token:', response.token);
    }
  })
  .catch(error => {
    console.error('Error in call:', error);
  });
