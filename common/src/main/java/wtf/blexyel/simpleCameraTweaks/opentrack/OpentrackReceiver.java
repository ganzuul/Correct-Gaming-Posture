package wtf.blexyel.simpleCameraTweaks.opentrack;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import wtf.blexyel.simpleCameraTweaks.SimpleCameraTweaks;

public class OpentrackReceiver implements Runnable {
    // Shared State - accessible from mixins
    public static volatile double x = 0;
    public static volatile double y = 0;
    public static volatile double z = 0;
    public static volatile double yaw = 0;
    public static volatile double pitch = 0;
    public static volatile double roll = 0;

    private static final int PORT = 4242;
    private volatile boolean running = false;
    private static OpentrackReceiver instance;
    private long lastLogTime = 0;
    private static final long LOG_INTERVAL_MS = 1000; // Log once per second

    public static synchronized OpentrackReceiver getInstance() {
        if (instance == null) {
            instance = new OpentrackReceiver();
        }
        return instance;
    }

    public static synchronized void start() {
        if (getInstance().running) {
            return;
        }
        getInstance().running = true;
        getInstance().lastLogTime = System.currentTimeMillis();
        Thread listenerThread = new Thread(getInstance(), "opentrack-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
        SimpleCameraTweaks.LOGGER.info("[Head Tracking] Starting OpenTrack UDP listener on port " + PORT);
    }

    public static synchronized void stop() {
        if (getInstance().running) {
            getInstance().running = false;
            SimpleCameraTweaks.LOGGER.info("[Head Tracking] Stopping OpenTrack UDP listener");
        }
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[48]; // 6 doubles * 8 bytes
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            SimpleCameraTweaks.LOGGER.info("[Head Tracking] 📡 Listening on UDP port " + PORT);

            boolean hasReceivedData = false;

            while (running) {
                socket.receive(packet);

                // Wrap buffer to read doubles (Little Endian is standard for Opentrack)
                ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, packet.getLength()).order(ByteOrder.LITTLE_ENDIAN);

                double rx = bb.getDouble();
                double ry = bb.getDouble();
                double rz = bb.getDouble();
                double ryaw = bb.getDouble();
                double rpitch = bb.getDouble();
                double rroll = bb.getDouble();

                // Update shared state (Scale adjustments: cm -> blocks)
                x = rx / 100.0;
                y = ry / 100.0;
                z = rz / 100.0;
                yaw = ryaw;
                pitch = rpitch;
                roll = rroll;

                // Log telemetry once per second
                long now = System.currentTimeMillis();
                if (now - lastLogTime > LOG_INTERVAL_MS) {
                    lastLogTime = now;
                    if (!hasReceivedData) {
                        SimpleCameraTweaks.LOGGER.info("[Head Tracking] 🎯 Circuit Closed! Receiving tracking data");
                        hasReceivedData = true;
                    }
                    SimpleCameraTweaks.LOGGER.info(String.format(
                        "[Head Tracking] Y:%.1f° P:%.1f° | Pos:(%.2f, %.2f, %.2f)",
                        yaw, pitch, x, y, z
                    ));
                }
            }
        } catch (java.net.BindException e) {
            SimpleCameraTweaks.LOGGER.error("[Head Tracking] ❌ Port " + PORT + " already in use! Is OpenTrack running?");
        } catch (Exception e) {
            if (running) {
                SimpleCameraTweaks.LOGGER.error("[Head Tracking] ❌ Listener error:", e);
            }
        }
    }
}
