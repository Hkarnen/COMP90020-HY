package node;

import node.Node;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class DiscoveryManager {
    private static final Logger logger = Logger.getLogger(DiscoveryManager.class.getName());
    private final Node node;

    private static final String BROADCAST_IP = "233.0.0.0";
    private static final int    BROADCAST_PORT = 22333;
    private static final int    ANNOUNCE_INTERVAL_MS = 3000;

    private MulticastSocket socket;
    private InetAddress     group;

    public DiscoveryManager(Node node) {
        this.node = node;
    }

    public void start() throws IOException {
        socket = new MulticastSocket(BROADCAST_PORT);
        group  = InetAddress.getByName(BROADCAST_IP);
        socket.joinGroup(group);

        Thread listener = new Thread(() -> {
            byte[] buf = new byte[256];
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    if (msg.startsWith("ANNOUNCE:")) {
                        String[] parts = msg.split(":");
                        int  otherId   = Integer.parseInt(parts[1]);
                        int  otherPort = Integer.parseInt(parts[2]);
                        if (otherId != node.getId()
                                && !node.getPeerConfig().getPeerMap().containsKey(otherId)) {
                            logger.info("Discovered Node " + otherId + " @ port " + otherPort);
                            node.getPeerConfig().addPeer(otherId, otherPort);
                        }
                    }
                } catch (Exception e) {
                    logger.warning("Discovery listener error: " + e.getMessage());
                }
            }
        });
        listener.setDaemon(true);
        listener.start();

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> {
            String announce = "ANNOUNCE:" + node.getId() + ":" + node.getPort();
            try {
                byte[] data = announce.getBytes();
                DatagramPacket packet =
                        new DatagramPacket(data, data.length, group, BROADCAST_PORT);
                socket.send(packet);
                logger.finest("Sent discovery announce");
            } catch (IOException e) {
                logger.warning("Discovery announce failed: " + e.getMessage());
            }
        }, 0, ANNOUNCE_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
}
