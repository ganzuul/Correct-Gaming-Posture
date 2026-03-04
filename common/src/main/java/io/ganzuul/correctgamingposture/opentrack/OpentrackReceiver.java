package io.ganzuul.correctgamingposture.opentrack;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import io.ganzuul.correctgamingposture.CorrectGamingPosture;

public class OpentrackReceiver implements Runnable {
    // Shared State - accessible from mixins
    public static volatile double x = 0;
    public static volatile double y = 0;
    public static volatile double z = 0;
    public static volatile double yaw = 0;
    public static volatile double pitch = 0;
    public static volatile double roll = 0;

    private static final int PORT = 4242;
    private static final int SOCKET_TIMEOUT_MS = 500;
    private static final long DISCONNECT_AFTER_MS = 2000;
    private volatile boolean running = false;
    private static OpentrackReceiver instance;
    private static volatile boolean connected = false;
    private static volatile long lastPacketTime = 0;

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
        Thread listenerThread = new Thread(getInstance(), "opentrack-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
        CorrectGamingPosture.LOGGER.info("[Head Tracking] Starting OpenTrack UDP listener on port " + PORT);
    }

    public static synchronized void stop() {
        if (getInstance().running) {
            getInstance().running = false;
            connected = false;
            CorrectGamingPosture.LOGGER.info("[Head Tracking] Stopping OpenTrack UDP listener");
        }
    }

    public static boolean isConnected() {
        return connected;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);
            byte[] buffer = new byte[48]; // 6 doubles * 8 bytes
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            CorrectGamingPosture.LOGGER.info("[Head Tracking] 📡 Listening on UDP port " + PORT);

            while (running) {
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException ignored) {
                    if (connected && (System.currentTimeMillis() - lastPacketTime) > DISCONNECT_AFTER_MS) {
                        connected = false;
                    }
                    continue;
                }

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
                connected = true;
                lastPacketTime = System.currentTimeMillis();
            }
        } catch (java.net.BindException e) {
            CorrectGamingPosture.LOGGER.error("[Head Tracking] ❌ Port " + PORT + " already in use! Is OpenTrack running?");
        } catch (Exception e) {
            if (running) {
                CorrectGamingPosture.LOGGER.error("[Head Tracking] ❌ Listener error:", e);
            }
        } finally {
            connected = false;
        }
    }
}
