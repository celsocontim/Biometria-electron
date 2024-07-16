const net = require('net');

// Configurações do cliente
const client = new net.Socket();
const PORT = 2222;
const HOST = '127.0.0.1'; // Endereço do servidor

// Conecta ao servidor
client.connect(PORT, HOST, () => {
    console.log('Conectado ao servidor');
    // Envia a mensagem 'startCapture'
    client.write('startCapture\n');
});

// Manipula a resposta do servidor
client.on('data', (data) => {
    console.log('Servidor diz: ' + data.toString());

    // Aqui podemos enviar o próximo comando dependendo da resposta do servidor
    if (data.toString().includes('Oi aqui fala o Java')) {
        // Envia a mensagem para obter a quantidade de biometrias capturadas após um intervalo
        setTimeout(() => {
            client.write('getBiometryCount\n');
        }, 5000);
    } else if (data.toString().includes('Quantidade de biometrias diferentes:')) {
        // Envia a mensagem para comparar biometrias após um intervalo
        setTimeout(() => {
            client.write('compareBiometries\n');
        }, 5000);
    } else {
        // Encerra a conexão se não houver mais comandos a enviar
        client.end();
    }
});

// Manipula o fechamento da conexão
client.on('close', () => {
    console.log('Conexão fechada');
});

// Manipula erros
client.on('error', (err) => {
    console.error('Erro: ' + err.message);
});
