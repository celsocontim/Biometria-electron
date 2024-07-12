package teste_biometria;

import com.griaulebiometrics.gbsfingerprint.*;
import com.griaulebiometrics.gbsfingerprint.event.DeviceAdapter;
import com.griaulebiometrics.gbsfingerprint.event.FingerAdapter;
import com.griaulebiometrics.gbsfingerprint.event.FrameAdapter;
import com.griaulebiometrics.gbsfingerprint.event.ImageAdapter;
import com.griaulebiometrics.gbsfingerprint.exception.GBSFingerprintException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
		boolean aguardandoGriaule = true;
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
						while (aguardandoGriaule){
							if (exit){
								System.out.println("Biometria encontrada");
								out.write("Biometria encontrada".getBytes());
								out.flush();
								aguardandoGriaule = false;
							}
						}
						aguardandoGriaule = true;
						exit = false;
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
				try {
					sdk.stopCapturing(string);
				} catch (GBSFingerprintException e) {
					LOG.log(Level.SEVERE, null, e);
				}
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
			try {
				System.out.println("Plugged device: " + device);
				sdk.startCapturing(device);
				System.out.println("Capture started on device: " + device);
			} catch (GBSFingerprintException ex) {
				Logger.getLogger(GriauleTeste.class.getName()).log(Level.SEVERE, null, ex);
			}
		}


	private void onUnplug(String device) {
		System.out.println("Unplugged device: " + device);
	}

	private void onFingerDown(String device) {
		System.out.println("Finger removed on device: " + device);
	}

	private void onFingerUp(String device){
		System.out.println("Finger detected on device: " + device);
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

		try {
			final Template tpt = sdk.extractTemplate(image, TemplateFormat.DEFAULT, TemplateEncoding.ASCII);
			System.out.println("Quality: " + tpt.getQuality());
			System.out.println(converteArrayByteParaString(tpt.getBuffer()));
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

	static volatile boolean exit = false;

	/**
	 * Converte um array de bytes para um String,
	 */
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

	private static final Logger LOG = Logger.getLogger(GriauleTeste.class.getName());

}
