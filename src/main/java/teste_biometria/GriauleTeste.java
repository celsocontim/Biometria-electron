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
import com.griaulebiometrics.gbsfingerprint.exception.GBSFingerprintException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class GriauleTeste {

	private Socket socket = null;

	public static void main(String[] argv) throws Exception {
		final GriauleTeste main = new GriauleTeste();
		String ip = "127.0.0.1";
		int port = 3000;
		main.socketConnect(ip, port);
		String message = "Gol do Gabigol";
		System.out.println("Java Enviando: " + message);
		String retorno = main.echo(message);
		System.out.println("Java Recebendo: " + retorno);
		main.run();
		System.out.flush();
	}

	private void socketConnect(String ip, int port) throws UnknownHostException, IOException {
		System.out.println("[Conectando socket...]");
		this.socket = new Socket(ip, port);
	}

	public String echo(String message) {
		try {
			// out & in
			PrintWriter out = new PrintWriter(getSocket().getOutputStream(), true);
			BufferedReader in = new BufferedReader
					(new InputStreamReader(getSocket().getInputStream()));

			// escreve str no socket e lêr
			out.println(message);
			String retorno = in.readLine();
			return retorno;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// obtem instância do socket
	private Socket getSocket() {
		return socket;
	}

	private void onPlug(String device) {
		try {
			System.out.println("Plugged device: " + device);
			sdk.startCapturing(device);
			System.out.println("Capture started on device: " + device);
			String retorno = echo("Plugged device: " + device);
			System.out.println("Java Recebendo: " + retorno);
		} catch (GBSFingerprintException ex) {
			Logger.getLogger(GriauleTeste.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void onUnplug(String device) {
		System.out.println("Unplugged device: " + device);
	}

	private void onFingerDown(String device) {
		System.out.println("Finger removed on device: " + device);
		String retorno = echo("Finger removed on device: " + device);
		System.out.println("Java Recebendo: " + retorno);
	}

	private void onFingerUp(String device) {
		System.out.println("Finger detected on device: " + device);
		String retorno = echo("Finger detected on device: " + device);
		System.out.println("Java Recebendo: " + retorno);
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

	private static final Logger LOG = Logger.getLogger(GriauleTeste.class.getName());

}
