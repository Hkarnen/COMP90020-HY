package node;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Implements the Fast Bully leader election protocol with two-phase timeout.
 */
public class ElectionManager {
	
	private static final int TIMEOUT = 3000;
	private static final int SHORT_TIMEOUT = 1000;
	
	private final Node node;
    private final Consumer<String> logger;
	
	// Election state flags
	private volatile boolean inElection = false;
	private volatile boolean receivedOk = false;
	private volatile boolean receivedCoordinator = false;
	
	public ElectionManager(Node node, Consumer<String> logger) {
		this.node = node;
        this.logger = logger;
	};
	
	/**
     * Starts a new election if one is not already in progress.
     */
	public synchronized void initiateElection() {
        logger.accept("[Election] Node " + node.getId() + " initiating election.");
		System.out.println("[Election] Node " + node.getId() + " initiating election.");
        inElection = true;
        receivedOk = false;
        receivedCoordinator = false;

        boolean higherExists = false;
        
        for (int peerId : node.getPeerConfig().getPeerIds()) {
            if (peerId > node.getId()) {
                int peerPort = node.getPeerConfig().getPort(peerId);
                // Send ELECTION message
                Message msg = new Message(Message.Type.ELECTION, node.getId(), -1, "");
                node.getMessenger().sendMessage(peerPort, msg);
                higherExists = true;
            }
        }

        if (!higherExists) {
        	// No higher-ID alive -> self-promote
            declareLeader();
        } 
        else {
        	// Two-phase wait
            new Thread(() -> {
                try {
                    Thread.sleep(TIMEOUT);
                    if (receivedOk && !receivedCoordinator) {
                    	// got OK but no COORDINATOR -> wait SHORT_TIMEOUT
                    	System.out.println("[Election] OK received; waiting extra " + SHORT_TIMEOUT + "ms for COORDINATOR.");
                        logger.accept("[Election] OK received; waiting extra " + SHORT_TIMEOUT + "ms for COORDINATOR.");
                        Thread.sleep(SHORT_TIMEOUT);
                        if (!receivedCoordinator) {
                        	System.out.println("[Election] No COORDINATOR after extra wait; restarting election.");
                            logger.accept("[Election] No COORDINATOR after extra wait; restarting election.");
                            inElection = false;           // allow re-entry
                            initiateElection();
                        }
                    }
                    else if (!receivedOk && !receivedCoordinator) {
                    	// no response -> self-promote
                        declareLeader();
                    }
                 // Otherwise: receivedCoordinator==true → election over
                } 
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
	}
	
	/**
     * Handle an incoming ELECTION message: reply OK and possibly start own election.
     */
	public void handleElectionMessage(int fromId) {
        if (fromId < node.getId()) {
            int peerPort = node.getPeerConfig().getPort(fromId);
            Message okMsg = new Message(Message.Type.OK, node.getId(), -1, "");
            node.getMessenger().sendMessage(peerPort, okMsg);

            if (!inElection) {
                initiateElection();
            }
        }
    }
	
	public void handleOkMessage() {
        receivedOk = true;
    }

    public void handleCoordinatorMessage(int fromId) {
        receivedCoordinator = true;
        inElection = false;
        node.setLeader(fromId);
    }
	
    /**
     * Declare self as leader and broadcast COORDINATOR to lower-ID processes.
     */
	private void declareLeader() {
        System.out.println("[Election] Node " + node.getId() + " is the new leader!");
        logger.accept("[Election] Node " + node.getId() + " is the new leader!");
        node.setLeader(node.getId());
        inElection = false;
        node.setLeader(node.getId());
        // Send COORDINATOR to lower-ID peers
        for (int peerId : node.getPeerConfig().getPeerIds()) {
        	if (peerId < node.getId()) {
        		int peerPort = node.getPeerConfig().getPort(peerId);
        		Message coordMsg = new Message(Message.Type.COORDINATOR, node.getId(), -1, "");
                node.getMessenger().sendMessage(peerPort, coordMsg);
        	}
        }
    }
    public void handlePeerDown(int deadId) {
        peerConfig.getPeerMap().remove(deadId);
        logger.accept("[Election] Removed dead peer " + deadId);
        // broadcast removal
        for (int port : peerConfig.getPeerMap().values()) {
            try { messenger.sendRaw(port, "PEER_DOWN:" + deadId); } catch (IOException ignored) {}
        }
    }
	
}
