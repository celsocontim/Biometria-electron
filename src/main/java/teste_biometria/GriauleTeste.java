package teste_biometria;

import com.griaulebiometrics.gbsfingerprint.*;
import com.griaulebiometrics.gbsfingerprint.event.DeviceAdapter;
import com.griaulebiometrics.gbsfingerprint.event.FingerAdapter;
import com.griaulebiometrics.gbsfingerprint.event.FrameAdapter;
import com.griaulebiometrics.gbsfingerprint.event.ImageAdapter;
import com.griaulebiometrics.gbsfingerprint.exception.GBSFingerprintException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.net.*;
import java.net.Socket;
import java.net.UnknownHostException;


public class GriauleTeste {

	public static void main(String[] argv) throws Exception {
		final GriauleTeste main = new GriauleTeste();
		main.openServer();
		//main.run();
		System.out.flush();
	}

	private void openServer (){
		ServerSocket server;
		Socket client;
		boolean executa = true;
		//boolean aguardandoGriaule = true;
		Thread griaule = new Thread(() -> {
			System.out.println("Comecando thread Griaule");
			System.out.println("ID: " + Thread.currentThread().getId());
			System.out.println("Nome: " + Thread.currentThread().getName());
			System.out.println("Prioridade: " + Thread.currentThread().getPriority());
			System.out.println("Estado: " + Thread.currentThread().getState());
			chamaGriaule();
		});
		try {
			server = new ServerSocket(2222);
			System.out.println("Servidor ouvindo na porta 2222");
			griaule.start();
			while (executa){
				client = server.accept();
				System.out.println("New client connected: " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
				DataOutputStream out = new DataOutputStream(client.getOutputStream());
				// Read message from client
				String message = in.readLine();
				System.out.println("Client says: " + message);
				switch (message){
					case "startCapture":
						sdk.startCapturing(dispositivo);
						String mensagem = "Captura Iniciada";
						out.write(mensagem.getBytes());
						out.flush();
						break;
					case "checkCapture":
                        String retorno;
						//verifica se teve um dedo
                        if (!Objects.equals(ultimoDedo, "")){
                            retorno = "Dedo encontrado: " + converteArrayByteParaString(ultimaImagem);
                        	//retorno = "Dedo encontrado: " + ultimoDedo;
						}
						else {
                            retorno = "Dedo nao encontrado";
                        }
                        out.write(retorno.getBytes());
                        out.flush();
                        break;
					case "stopCapture":
						String volta = "Captura finalizada";
						out.write(volta.getBytes());
						out.flush();
						sdk.stopCapturing(dispositivo);
						ultimoDedo = "";
						ultimaImagem = "".getBytes();
						ultimoScore = 0;
						break;
					case "exit":
						System.out.println("Servidor java encerrando");
						// Close the client socket
						client.close();
						// Close the server socket
						server.close();
						executa = false;
					default:
						System.out.println("Requisicao nao fez nada");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void chamaGriaule() {
		final GriauleTeste self = this;

		sdk.addDeviceListener(new DeviceAdapter() {
			@Override
			public void onPlug(String device) {
				System.out.println(device);
				self.onPlug(device);
			}

			@Override
			public void onUnplug(String device) {
				self.onUnplug(device);
			}
		});

		sdk.addFingerListener(new FingerAdapter() {
			@Override
			public void onFingerDown(String string) {
				self.onFingerDown(string);
			}

			@Override
			public void onFingerUp(String string) {
				self.onFingerUp(string);
				exit = true;
			}
		});

		sdk.addImageListener(new ImageAdapter() {
			@Override
			public void onCapture(String string, Image image) {
				self.onImage(string, image);
				System.out.println(string);
			}
		});

		try {
			//sdk.setLicenseFolder("C:/Temp");
			sdk.initialize();
			System.in.read();
		} catch (IOException | GBSFingerprintException ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}

	private void onPlug (String device){
			//try {
				System.out.println("Plugged device: " + device);
				dispositivo = device;
				/*
				sdk.startCapturing(device);
				System.out.println("Capture started on device: " + device);
			} catch (GBSFingerprintException ex) {
				Logger.getLogger(GriauleTeste.class.getName()).log(Level.SEVERE, null, ex);
			}

				 */
		}


	private void onUnplug(String device) {
		System.out.println("Unplugged device: " + device);
	}

	private void onFingerDown(String device) {
		System.out.println("Finger detected on device: " + device);
	}

	private void onFingerUp(String device){
		System.out.println("Finger removed on device: " + device);
	}

	private void onFrame(String device, Image image) {
		System.out.println("Frame captured on device: " + device);
	}

	private void onImage(String device, Image image) {
		System.out.println("Image captured on device: " + device);
		System.out.println("Format: " + image.getFormat());
		System.out.println("Width: " + image.getWidth());
		System.out.println("Height: " + image.getHeight());
		System.out.println("Resolution: " + image.getResolution());
		ultimaImagem = convertJpgToBytesArray (image);
		/*
		Template teste1 = new Template(converteStringToArrayByte(dedoCelso), 57, TemplateFormat.DEFAULT, TemplateEncoding.ASCII);
		Template teste2 = new Template(converteStringToArrayByte(dedoEd), 57, TemplateFormat.DEFAULT, TemplateEncoding.ASCII);
		try {
			int score = sdk.verify(teste1, teste2).getScore();
			System.out.println("Score do verify dos templates do ed: " + score);
		} catch (GBSFingerprintException ex){
			Logger.getLogger(GriauleTeste.class.getName()).log(Level.SEVERE, null, ex);
		}
		*/
		try {
			final Template tpt = sdk.extractTemplate(image, TemplateFormat.DEFAULT, TemplateEncoding.ASCII);
			System.out.println("Quality: " + tpt.getQuality());
			ultimoDedo = converteArrayByteParaString(tpt.getBuffer());
			System.out.println(ultimoDedo);
			byte [] array = converteStringToArrayByte(dedoCelso);
			Template dedoCelsoTpt = new Template(array, 57, TemplateFormat.DEFAULT, TemplateEncoding.ASCII);
			ultimoScore = sdk.verify(tpt, dedoCelsoTpt).getScore();
			System.out.println("Score contra dedo do meio do Celso: " + ultimoScore);
			/*
			if (!this.templates.isEmpty()) {
				int i = 0;
				for (Template reference : this.templates) {
					try {
						System.out.println("Verification against template " + (i++) + " with score " + sdk.verify(tpt, reference).getScore());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			this.templates.add(tpt);

			 */
		} catch (GBSFingerprintException ex) {
			Logger.getLogger(GriauleTeste.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void run() throws NoSuchMethodException {
		final GriauleTeste self = this;

		sdk.addDeviceListener(new DeviceAdapter() {
			@Override
			public void onPlug(String device) {
				self.onPlug(device);
			}

			@Override
			public void onUnplug(String device) {
				self.onUnplug(device);
			}
		});

		sdk.addFingerListener(new FingerAdapter() {
			@Override
			public void onFingerDown(String string) {
				self.onFingerDown(string);
			}

			@Override
			public void onFingerUp(String string) {
                    self.onFingerUp(string);
            }
		});

		sdk.addFrameListener(new FrameAdapter() {
			@Override
			public void onCapture(String string, Image image) {
				self.onFrame(string, image);
			}
		});

		sdk.addImageListener(new ImageAdapter() {
			@Override
			public void onCapture(String string, Image image) {
				self.onImage(string, image);
			}
		});

		try {
			//sdk.setLicenseFolder("C:/Temp");
			sdk.initialize();
			System.in.read();
		} catch (IOException | GBSFingerprintException ex) {
			LOG.log(Level.SEVERE, null, ex);
		}
	}

	final GBSFingerprint sdk = GBSFingerprint.getInstance();

	private final List<Template> templates = new ArrayList<Template>();

	static volatile String dispositivo = "nada";

	public static volatile String ultimoDedo = "";

	public volatile int ultimoScore = 0;

	public volatile byte[] ultimaImagem = "".getBytes();

	static volatile boolean exit = false;

	public final String dedoEdAntigo = "-89 -1 1 30 51 1 -124 0 2 65 1 33 1 36 1 2 71 1 -26 0 116 0 2 71 1 -2 0 -6 0 2 88 1 -50 0 -111 0 1 88 1 -64 0 -26 0 2 0 0 27 1 57 1 2 71 1 -9 0 -53 0 2 -98 0 51 1 46 1 2 -115 0 -65 0 125 0 1 88 1 44 1 -25 0 2 71 1 -87 0 24 1 1 23 0 2 1 -100 0 1 71 1 -47 0 90 0 1 71 1 14 1 -53 0 1 76 1 -93 0 63 1 2 45 0 -44 0 56 0 2 59 1 -33 0 39 1 2 99 1 -67 0 48 1 2 29 0 -15 0 65 1 2 88 1 117 0 -56 0 1 -47 0 -128 0 -50 0 2 17 0 4 1 43 0 2 59 1 112 0 -70 0 1 -53 0 -107 0 84 0 1 93 1 112 0 -76 0 2 23 0 112 0 16 1 2 -25 0 -124 0 48 1 1 -25 0 -124 0 96 0 1 0 0 -4 0 84 1 2 82 1 -101 0 23 25 21 20 20 23 8 1 20 25 28 24 6 1 29 19 14 7 4 9 6 8 21 23 15 18 21 25 18 11 19 17 2 13 13 16 15 27 18 17 27 26 4 2 15 11 9 13 9 2 29 6 10 14 19 6 27 11 7 12 3 7 12 2 14 12 3 14 3 10 16 22 12 4 29 17 1 3 11 5 4 13 19 18 12 0 17 3 17 11 19 1 27 18 11 26 9 24 10 7 13 24 29 1 5 7 1 10 6 17 2 16 3 5 15 17 9 28 17 1 29 8 26 21 5 21 13 22 6 3 24 16 19 8 15 26 7 4 8 10 17 5 9 16 26 20 19 3 29 18 12 9 18 5 8 3 28 13 19 15 2 22 0 2 14 0 5 20 12 13 18 26 19 11 5 14 4 24 6 10 8 17 11 21 5 4 14 4 25 28 10 12 26 23 2 24 7 2 28 16 4 16 4 28 11 3 26 5 29 3 29 15 27 17 5 23 1 14 23 28 26 25 7 0 15 5 5 25 6 18 3 12 11 20 17 7 27 5 7 9 25 9 14 2 27 21 5 12 21 4 10 0 2 28 23 9 17 10 25 4 18 1 0 22 4 0 25 24 21 9 23 4 27 20 20 4 8 14 0 13 9 22 23 24 12 16 12 22 1 5 4 22 3 4 24 22 15 6 3 0 17 21 25 2 11 4 1 0 8 0";
	public final String dedoEd = "-89 -1 8 -95 1 96 1 125 1 0 86 -1 -1 40 0 113 0 31 1 1 123 0 100 37 0 -2 0 -98 0 1 -105 0 100 23 0 -12 0 118 0 1 -101 0 100 19 0 87 0 -29 0 1 66 0 100 29 0 92 0 -72 0 2 35 0 100 68 0 -39 0 -125 0 2 76 1 100 56 0 93 0 8 1 1 75 0 100 33 0 106 0 -121 0 2 4 0 100 61 0 -84 0 -103 0 1 94 1 100 66 -128 54 0 -44 0 1 -30 0 100 67 0 -88 0 60 0 1 84 1 100 8 -128 -76 0 41 1 1 62 1 100 80 0 113 0 51 0 1 94 1 100 50 -128 -94 0 -38 0 2 99 1 100 92 0 -79 0 -19 0 2 93 1 90 78 0 -76 0 107 0 2 85 1 90 55 0 -54 0 -45 0 2 73 1 90 91 0 112 0 -53 0 2 39 0 90 74 -128 -98 0 106 0 2 -92 0 90 57 -128 -111 0 -62 0 2 4 0 90 71 0 -105 0 25 1 2 -127 0 90 89 -128 95 0 46 1 1 118 0 70 40 0 -64 0 63 1 1 74 1 70 36 -128 60 0 12 1 2 65 0 60 86 0 20 1 -75 0 1 68 1 60 15 -128 120 0 16 1 2 113 0 60 88 0 82 0 34 1 2 77 0 60 91 -128 -35 0 35 0 2 66 1 50 44 0 52 0 57 1 1 -21 0 50 29 -128 -49 0 -67 0 2 73 1 40 72 0 -32 0 -56 0 2 -113 0 40 72 -128 -98 0 -11 0 2 -110 0 40 81 -128 -118 0 25 1 2 40 1 40 83 -128 29 0 -88 0 1 -5 0 40 60 0 30 0 -107 0 1 -44 0 40 20 0 -112 0 2 1 2 118 0 30 81 0 -115 0 23 0 2 65 1 30 46 0 -123 0 70 1 2 26 0 30 28 0 -126 0 9 1 2 116 0 30 82 0 30 0 -128 0 1 -15 0 30 15 0 0 0";
	public final String dedoCelso = "-89 -1 1 32 90 0 81 1 1 99 0 78 0 71 1 2 79 0 -29 0 -23 0 2 -117 0 -44 0 -35 0 2 65 1 111 0 64 1 1 103 0 118 0 48 1 2 93 0 -94 0 25 1 2 -112 0 -77 0 16 1 2 76 1 -82 0 -128 1 1 79 1 -73 0 108 1 1 71 1 -91 0 -7 0 2 97 1 92 0 -41 0 1 32 0 1 1 -66 0 1 -115 0 -51 0 -11 0 2 66 1 -92 0 -120 0 1 -89 0 -67 0 -125 0 1 83 1 55 0 -10 0 1 -20 0 93 0 41 1 1 75 0 -85 0 84 1 2 62 1 113 0 -23 0 1 36 0 -107 0 60 1 2 120 0 -109 0 -33 0 2 2 0 -38 0 -95 0 1 76 1 -8 0 -108 0 1 -109 0 89 0 3 1 1 58 0 -79 0 -74 0 1 91 1 109 0 -93 0 2 9 0 119 0 79 0 1 98 1 -82 0 87 0 1 81 1 111 0 124 1 2 65 1 92 0 119 1 1 79 1 54 0 47 1 1 68 0 -79 0 0 1 4 5 6 7 2 3 29 30 8 9 13 3 13 2 14 15 5 17 7 10 0 4 9 18 19 11 4 17 6 10 10 21 22 23 20 5 18 20 1 4 1 17 1 31 24 19 19 21 24 16 7 13 20 6 17 24 30 0 4 20 31 17 0 17 10 13 22 15 0 5 12 23 24 11 8 18 25 22 1 5 15 28 25 14 16 11 29 0 12 22 14 28 0 31 5 6 30 1 21 25 2 12 25 15 3 25 20 7 5 24 10 19 3 12 11 26 10 3 21 11 31 24 28 27 6 13 31 16 30 4 20 17 7 21 9 20 4 31 16 19 22 14 29 4 18 6 6 21 3 22 23 15 7 3 26 14 29 1 13 21 7 2 0 20 18 4 8 29 5 31 18 5 4 6 10 2 17 16 21 3 4 24 17 19 18 7 10 25 24 21 13 25 1 24 6 19 19 26 25 26 2 25 5 19 21 26 29 18 8 20 5 10 14 27 2 22 29 9 29 20 30 5 29 5 13 12 6 24 7 19 30 17 25 23 12 25 3 23 2 21 10 11 30 31 30 20 8 30 17 11 1 16 9 4 23 14 13 22 26 27 22 28 9 6 5 16 26 15 15 27 9 5 2 23 21 14 12 15 20 19 8 4 18 10 11 25 30 9 9 7 4 16 5 11 3 15 21 22 16 21 25 28 8 0 31 11 23 28 8 5 24 26 16 26 26 28 19 3 18 13 12 14 29 6 25 27 3 26 9 13 18 2 22 27 12 28 9 2 11 27 8 13 23 27 9 3 16 14 16 27 9 12";

	/**
	 * Converte um array de bytes para um String,
	 */

	public static byte[] convertJpgToBytesArray(BufferedImage image){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] imageInByte = null;
		try {
			ImageIO.write(image, "jpg", baos);
			baos.flush();
			imageInByte = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			LOG.log(Level.SEVERE, null, e);
		}
		return imageInByte;
	}
	public String converteArrayByteParaString(byte[] array) {

		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			if(i < array.length - 1) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	public byte[] converteStringToArrayByte(String texto) {

		StringTokenizer st = new StringTokenizer(texto);
		int i = 0;
		byte [] array = new byte [st.countTokens()];

		while (st.hasMoreTokens()) {
			array[i] = Byte.valueOf(st.nextToken()).byteValue();
			i++;
		}
		return array;
	}

		private static final Logger LOG = Logger.getLogger(GriauleTeste.class.getName());

}
