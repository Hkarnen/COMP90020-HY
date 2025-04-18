package node;

/**
 * Implements the Fast Bully leader election protocol with two-phase timeout.
 */
public class ElectionManager {
	
	private static final int TIMEOUT = 3000;
	private static final int SHORT_TIMEOUT = 1000;
	
	private final Node node;
	private final PeerConfig peerConfig;
	private final Messenger messenger;
	
	// Election state flags
	private volatile boolean inElection = false;
	private volatile boolean receivedOk = false;
	private volatile boolean receivedCoordinator = false;
	
	public ElectionManager(Node node, PeerConfig peerConfig, Messenger messenger) {
		this.node = node;
		this.peerConfig = peerConfig;
		this.messenger = messenger;
	};
	
	/**
     * Starts a new election if one is not already in progress.
     */
	public synchronized void initiateElection() {
		System.out.println("[Election] Node " + node.getId() + " initiating election.");
        inElection = true;
        receivedOk = false;
        receivedCoordinator = false;

        boolean higherExists = false;
        
        for (int peerId : peerConfig.getPeerIds()) {
            if (peerId > node.getId()) {
                int peerPort = peerConfig.getPort(peerId);
                // Send ELECTION message
                Message msg = new Message(Message.Type.ELECTION, node.getId(), -1, "");
                messenger.sendMessage(peerPort, msg);
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
                        Thread.sleep(SHORT_TIMEOUT);
                        if (!receivedCoordinator) {
                        	System.out.println("[Election] No COORDINATOR after extra wait; restarting election.");
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
            int peerPort = peerConfig.getPort(fromId);
            Message okMsg = new Message(Message.Type.OK, node.getId(), -1, "");
            messenger.sendMessage(peerPort, okMsg);

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
        inElection = false;
        node.setLeader(node.getId());
        // Send COORDINATOR to lower-ID peers
        for (int peerId : peerConfig.getPeerIds()) {
        	if (peerId < node.getId()) {
        		int peerPort = peerConfig.getPort(peerId);
        		Message coordMsg = new Message(Message.Type.COORDINATOR, node.getId(), -1, "");
                messenger.sendMessage(peerPort, coordMsg);
        	}
        }
    }
	
	
}
