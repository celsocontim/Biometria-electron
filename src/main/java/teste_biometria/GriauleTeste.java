package teste_biometria;

import com.griaulebiometrics.gbsfingerprint.*;
import com.griaulebiometrics.gbsfingerprint.event.DeviceAdapter;
import com.griaulebiometrics.gbsfingerprint.event.FingerAdapter;
import com.griaulebiometrics.gbsfingerprint.event.ImageAdapter;
import com.griaulebiometrics.gbsfingerprint.exception.GBSFingerprintException;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GriauleTeste {

    private final GBSFingerprint sdk = GBSFingerprint.getInstance();
    private final List<Template> templates = new ArrayList<>();
    private static final Logger LOG = Logger.getLogger(GriauleTeste.class.getName());
    private static volatile boolean exit = false;
    private static volatile boolean capturing = false;
    private static volatile boolean clientConnected = false;

    public static void main(String[] argv) {
        try {
            GriauleTeste main = new GriauleTeste();
            main.openServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openServer() {
        try (ServerSocket server = new ServerSocket(2222)) {
            System.out.println("Servidor ouvindo na porta 2222");

            Thread griauleThread = new Thread(this::chamaGriaule);
            griauleThread.start();

            while (true) {
                try (Socket client = server.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                     DataOutputStream out = new DataOutputStream(client.getOutputStream())) {

                    clientConnected = true;
                    System.out.println("New client connected: " + client.getInetAddress().getHostAddress() + ":" + client.getPort());
                    out.write("Oi aqui fala o Java".getBytes());
                    out.flush();

                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Client says: " + message);
                        processClientMessage(message, out);
                    }

                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Erro na comunicação com o cliente", e);
                } finally {
                    clientConnected = false;
                    capturing = false;
                }
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Erro ao iniciar o servidor", e);
        }
    }

    private void processClientMessage(String message, DataOutputStream out) throws IOException {
        switch (message) {
            case "startCapture":
                if (!capturing) {
                    capturing = true;
                    startCapture(out);
                } else {
                    out.write("Capture already in progress".getBytes());
                    out.flush();
                }
                break;
            case "exit":
                System.out.println("Servidor java encerrando");
                System.exit(0);
                break;
            default:
                System.out.println("Requisicao nao fez nada");
                break;
        }
    }

    private void startCapture(DataOutputStream out) throws IOException {
        exit = false;
        while (capturing) {
            if (exit) {
                System.out.println("Biometria encontrada");
                out.write("Biometria encontrada".getBytes());
                out.flush();
                capturing = false;
                break;
            }
        }
    }

    private void chamaGriaule() {
        System.out.println("Começando thread Griaule");
        setupSdkListeners();

        try {
            sdk.initialize();
            System.in.read();
        } catch (IOException | GBSFingerprintException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void setupSdkListeners() {
        sdk.addDeviceListener(new DeviceAdapter() {
            @Override
            public void onPlug(String device) {
                GriauleTeste.this.onPlug(device);
            }

            @Override
            public void onUnplug(String device) {
                GriauleTeste.this.onUnplug(device);
            }
        });

        sdk.addFingerListener(new FingerAdapter() {
            @Override
            public void onFingerDown(String device) {
                if (clientConnected && capturing) {
                    GriauleTeste.this.onFingerDown(device);
                }
            }

            @Override
            public void onFingerUp(String device) {
                if (clientConnected && capturing) {
                    GriauleTeste.this.onFingerUp(device);
                    exit = true;
                }
            }
        });

        sdk.addImageListener(new ImageAdapter() {
            @Override
            public void onCapture(String device, com.griaulebiometrics.gbsfingerprint.Image image) {
                if (clientConnected && capturing) {
                    GriauleTeste.this.onImage(device, image);
                    try {
                        sdk.stopCapturing(device);
                    } catch (GBSFingerprintException e) {
                        LOG.log(Level.SEVERE, null, e);
                    }
                }
            }
        });
    }

    private void onPlug(String device) {
        try {
            System.out.println("Plugged device: " + device);
            sdk.startCapturing(device);
            System.out.println("Capture started on device: " + device);
        } catch (GBSFingerprintException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void onUnplug(String device) {
        System.out.println("Unplugged device: " + device);
    }

    private void onFingerDown(String device) {
        System.out.println("Finger detected on device: " + device);
    }

    private void onFingerUp(String device) {
        System.out.println("Finger removed on device: " + device);
    }

    private void onImage(String device, com.griaulebiometrics.gbsfingerprint.Image image) {
        System.out.println("Image captured on device: " + device);
        System.out.println("Format: " + image.getFormat());
        System.out.println("Width: " + image.getWidth());
        System.out.println("Height: " + image.getHeight());
        System.out.println("Resolution: " + image.getResolution());

        try {
            Template tpt = sdk.extractTemplate(image, TemplateFormat.DEFAULT, TemplateEncoding.ASCII);
            System.out.println("Quality: " + tpt.getQuality());
            System.out.println(converteArrayByteParaString(tpt.getBuffer()));

            verifyTemplates(tpt);

            templates.add(tpt);
        } catch (GBSFingerprintException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void verifyTemplates(Template tpt) {
        if (!templates.isEmpty()) {
            int i = 0;
            for (Template reference : templates) {
                try {
                    System.out.println("Verification against template " + (i++) + " with score " + sdk.verify(tpt, reference).getScore());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public String converteArrayByteParaString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
