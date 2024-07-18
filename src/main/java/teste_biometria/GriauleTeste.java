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
			System.out.println("Griaule interrompida: " + Thread.currentThread().isInterrupted());
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
				out.write("Oi aqui fala o Java".getBytes());
				out.flush();
				// Read message from client
				String message = in.readLine();
				System.out.println("Client says: " + message);
				switch (message){
					case "startCapture":
						sdk.startCapturing(dispositivo);
						String mensagem = "Captura Iniciada";
						out.write(mensagem.getBytes());
						out.flush();
						/*
						while (aguardandoGriaule){
							if (exit){
								String mensagem = "Biometria encontrada, score: " + ultimoScore;
								System.out.println(mensagem);
								out.write(mensagem.getBytes());
								out.flush();
								aguardandoGriaule = false;
								sdk.stopCapturing(dispositivo);
							}
						}
						aguardandoGriaule = true;
						exit = false;

						 */
						break;
					case "checkCapture":
                        String retorno;
						//verifica se teve um dedo
                        if (!Objects.equals(ultimoDedo, "")){
                            retorno = "Dedo encontrado: " + converteArrayByteParaString(ultimaImagem);
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
		System.out.println(converteArrayByteParaString(ultimaImagem));

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

	public final String dedoCelso = "-89 -1 8 -115 1 96 1 125 1 0 57 -1 -1 38 0 -35 0 -121 0 2 59 1 100 -112 0 -45 0 -67 0 2 53 1 90 125 0 -73 0 -76 0 2 81 1 90 127 -128 122 0 -68 0 2 48 0 90 -124 0 -51 0 -114 0 2 -123 0 90 -119 -128 -69 0 -85 0 2 -123 0 90 123 -128 1 1 -59 0 1 42 1 60 21 -128 -100 0 56 0 1 -88 0 60 99 0 -6 0 -45 0 1 51 1 50 32 -128 83 0 -86 0 2 46 0 50 122 -128 115 0 54 0 1 -67 0 40 97 0 -29 0 3 1 1 50 1 40 43 -128 119 0 -21 0 2 51 0 40 -100 -128 74 0 125 0 1 -25 0 40 22 -128 3 1 -123 0 1 68 1 40 100 -128 63 0 -66 0 1 -13 0 40 38 0 -122 0 -94 0 1 24 0 40 -116 -128 67 0 -51 0 1 -31 0 40 31 0 12 1 -92 0 1 37 1 40 15 -128 -77 0 68 0 2 84 1 40 100 0 -87 0 -32 0 1 86 1 40 45 -128 -97 0 -99 0 2 0 0 30 123 0 -104 0 -11 0 1 -118 0 30 -80 -128 11 1 -75 0 1 42 1 30 107 -128 -121 0 16 1 1 14 1 30 49 0 8 1 61 0 1 -79 0 30 10 -128 14 1 54 0 1 -56 0 30 5 0 47 0 -21 0 2 -5 0 30 -95 0 -2 0 15 1 1 28 1 30 48 -128 -29 0 -17 0 1 -126 0 30 -92 0 109 0 100 0 1 38 0 30 108 0 -59 0 -41 0 1 -128 0 30 43 0 93 0 -23 0 2 -23 0 30 51 -128 56 0 -86 0 1 45 0 30 37 0 92 0 109 0 2 -44 0 30 22 0 64 0 40 1 2 55 1 30 -76 0 -41 0 -36 0 1 51 1 20 127 0 -12 0 65 0 2 -112 0 20 103 -128 0 0";

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
