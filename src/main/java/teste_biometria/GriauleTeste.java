package teste_biometria;

import com.griaulebiometrics.gbsfingerprint.GBSFingerprint;
import com.griaulebiometrics.gbsfingerprint.Image;
import com.griaulebiometrics.gbsfingerprint.Template;
import com.griaulebiometrics.gbsfingerprint.TemplateEncoding;
import com.griaulebiometrics.gbsfingerprint.TemplateFormat;
import com.griaulebiometrics.gbsfingerprint.event.DeviceAdapter;
import com.griaulebiometrics.gbsfingerprint.event.FingerAdapter;
import com.griaulebiometrics.gbsfingerprint.event.FrameAdapter;
import com.griaulebiometrics.gbsfingerprint.event.ImageAdapter;
import com.griaulebiometrics.gbsfingerprint.handler.DeviceHandler;
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
		Thread griaule = new Thread(() -> {
            System.out.println("Comecando thread Griaule");
            System.out.println("ID: " + Thread.currentThread().getId());
            System.out.println("Nome: " + Thread.currentThread().getName());
            System.out.println("Prioridade: " + Thread.currentThread().getPriority());
            System.out.println("Estado: " + Thread.currentThread().getState());
            griauleAlo();
        });
		griaule.start();
		try {
			server = new ServerSocket(2222);
			System.out.println("Servidor ouvindo na porta 2222");
			client = server.accept();
			griaule.start();
			while (!exit){
				if (exit){
					griaule.interrupt();
					System.out.println("Thread Griaule encerrando");
				}
			}
			System.out.println("New client connected: " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			out.write("Oi aqui fala o Java".getBytes());
			out.flush();

				// Read message from client
			String message = in.readLine();
			System.out.println("Client says: " + message);

				// Close the client socket
			client.close();
				// Close the server socket
			server.close();
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

		sdk.addImageListener(new ImageAdapter() {
			@Override
			public void onCapture(String string, Image image) {
				self.onImage(string, image);
                try {
                    sdk.stopCapturing(string);
                } catch (GBSFingerprintException e) {
					LOG.log(Level.SEVERE, null, e);
                }
                sdk.finalize();
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


	private void griauleAlo () {
			sdk.addDeviceListener(new DeviceAdapter() {
				public void onPlug(String device) {
					try {
						System.out.println("Dispositivo encontrado: " + device);
						sdk.startCapturing(device);
					} catch (GBSFingerprintException e) {
						LOG.log(Level.SEVERE, null, e);
					}
				}

				public void onUnplug(String device) {
					try {
						System.out.println("Dispositivo desconectado: " + device);
						sdk.stopCapturing(device);
						sdk.finalize();

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
		System.out.println("Width: " + image.getWidth());
		System.out.println("Height: " + image.getHeight());
		System.out.println("Resolution: " + image.getResolution());

		try {
			final Template tpt = sdk.extractTemplate(image, TemplateFormat.DEFAULT, TemplateEncoding.ASCII);
			System.out.println("Quality: " + tpt.getQuality());

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

	private static final Logger LOG = Logger.getLogger(GriauleTeste.class.getName());

}
