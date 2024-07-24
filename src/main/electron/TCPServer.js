var net = require('net');

var client = new net.Socket();
client.connect(2222, '127.0.0.1', function() {
	console.log('Connected');
	client.write('startCapture\n');
});

client.on('data', function(data) {
   console.log('Received: ' + data);
   var string = (data.toString());
   if (string.includes ("Captura Iniciada")){

      client.write('checkCapture\n');
   }
	//client.destroy(); // kill client after server's response
});

client.on('close', function() {
	console.log('Connection closed');
});