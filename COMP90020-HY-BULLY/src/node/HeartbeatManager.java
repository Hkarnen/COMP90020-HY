package node;

import java.util.concurrent.*;

public class HeartbeatManager {

    private static final int HEARTBEAT_INTERVAL = 3000;  // ms
    private static final int HEARTBEAT_TIMEOUT  = 5000;  // ms

    private final Node node;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private volatile long lastHeartbeat = System.currentTimeMillis();

    public HeartbeatManager(Node node) {
        this.node = node;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            if (node.isLeader()) {
                // I am leader → send heartbeats to all
                Message hb = new Message(Message.Type.HEARTBEAT, node.getId(), -1, "");
                for (int peerId : node.getPeerConfig().getPeerIds()) {
                    node.getMessenger().sendMessage(node.getPeerConfig().getPort(peerId), hb);
                }
            } else {
                // I am follower → check heartbeat timeout
                long now = System.currentTimeMillis();
                if (now - lastHeartbeat > HEARTBEAT_TIMEOUT) {
                    System.out.println("[Heartbeat] Leader timeout detected! Triggering election...");
                    node.getElectionManager().initiateElection();
                    lastHeartbeat = now;  // prevent spamming multiple elections
                }
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    public void receivedHeartbeat() {
        lastHeartbeat = System.currentTimeMillis();
    }
}
