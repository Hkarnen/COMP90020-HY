package node;

import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Manages heartbeat messages for automatic failure detection.
 * Can be enabled/disabled for demonstration purposes.
 * When enabled:
 * - Leaders send regular heartbeats to all peers
 * - Followers monitor heartbeats and trigger elections on timeout
 */
public class HeartbeatManager {
	private static final Logger logger = Logger.getLogger(HeartbeatManager.class.getName());
	
    private static final int HEARTBEAT_INTERVAL = 2500;  // ms
    private static final int HEARTBEAT_TIMEOUT  = 5000;  // ms

    private final Node node;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private volatile long lastHeartbeat = System.currentTimeMillis();
    private volatile boolean enabled = false;
    
    /**
     * Enable or disable heartbeat mechanism
     */
    public void setEnabled(boolean on) {
    	if (on != enabled) {
            logger.info("Node " + node.getId() + " " + (on ? "enabling" : "disabling") + " heartbeat mechanism");
            enabled = on;
        }
    	// Reset heartbeat timer when enabling to prevent immediate false timeout
        if (on) {
            lastHeartbeat = System.currentTimeMillis();
        }
    }

    public HeartbeatManager(Node node) {
        this.node = node;
    }
    
    /**
     * Start the heartbeat scheduler.
     */
    public void start() {
    	logger.info("Node " + node.getId() + " starting heartbeat manager");
    	
        scheduler.scheduleAtFixedRate(() -> {
        	if (!enabled) return;
        	
            if (node.isLeader()) {
                // I am leader → send heartbeats to all
                sendHeartbeats();
            } 
            else {
                // I am follower → check heartbeat timeout
                checkLeaderTimeout();
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Send heartbeats to all peers (leader role).
     */
    private void sendHeartbeats() {
        logger.info("Node " + node.getId() + " (leader) sending heartbeats to all peers");
        
        Message hb = new Message(Message.Type.HEARTBEAT, node.getId(), -1, String.valueOf(node.getCurrentLeader()));
        for (int peerId : node.getPeerConfig().getPeerIds()) {
        	if (peerId == node.getId()) continue;
            try {
                int peerPort = node.getPeerConfig().getPort(peerId);
                node.getMessenger().sendMessage(peerPort, hb);
            } catch (Exception e) {
                logger.fine("Failed to send heartbeat to Node " + peerId);
            }
        }
    }

    /**
     * Check if leader has timed out (follower role).
     */
    private void checkLeaderTimeout() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastHeartbeat;
        
        if (elapsed > HEARTBEAT_TIMEOUT) {
            int leaderId = node.getCurrentLeader();
            logger.warning("Node " + node.getId() + " detected leader (Node " + leaderId + 
                    ") timeout after " + elapsed + "ms! Triggering election...");
            
            node.getElectionManager().initiateElection();
            lastHeartbeat = now; // prevent spamming multiple elections
        } else {
            logger.finest("Node " + node.getId() + " heartbeat check: " + elapsed +
                    "ms since last heartbeat (timeout: " + HEARTBEAT_TIMEOUT + "ms)");
        }
    }

    public void receivedHeartbeat(Message msg) {
        int hbLeader = msg.getSenderId();

        // Update only if we previously had no leader OR the ID changed
        if (node.getCurrentLeader() != hbLeader) {
            node.setLeader(hbLeader);          // one definitive log entry
        }
        lastHeartbeat = System.currentTimeMillis();
    }
}
