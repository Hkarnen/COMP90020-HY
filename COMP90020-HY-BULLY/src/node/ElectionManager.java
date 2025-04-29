package node;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the Fast Bully leader election protocol with two-phase timeout.
 */
public class ElectionManager {
	private static final Logger logger = Logger.getLogger(ElectionManager.class.getName());
	private static final int TIMEOUT = 3000;
	private static final int SHORT_TIMEOUT = 1000;
	
	private final Node node;
	
	// Election state flags
	private volatile boolean inElection = false;
	private volatile boolean receivedOk = false;
	private volatile boolean receivedCoordinator = false;
	
	public ElectionManager(Node node) {
		this.node = node;
	};
	
	/**
     * Starts a new election if one is not already in progress.
     */
	public synchronized void initiateElection() {
        if (inElection) {
        	logger.fine("Node " + node.getId() + " already in election, skipping initiation");
            return;
        }
        
        logger.info("Node " + node.getId() + " initiating election");
        inElection = true;
        receivedOk = false;
        receivedCoordinator = false;

        boolean higherExists = false;
        
        for (int peerId : node.getPeerConfig().getPeerIds()) {
            if (peerId > node.getId()) {
                int peerPort = node.getPeerConfig().getPort(peerId);
                // Send ELECTION message
                Message msg = new Message(Message.Type.ELECTION, node.getId(), -1, "");
                logger.fine("Node " + node.getId() + " sending ELECTION message to Node " + peerId);
                node.getMessenger().sendMessage(peerPort, msg);
                higherExists = true;
            }
        }

        if (!higherExists) {
        	// No higher-ID alive -> self-promote
        	logger.info("Node " + node.getId() + " has no peers with higher IDs, declaring self as leader");
            declareLeader();
        } 
        else {
        	// Two-phase wait
            new Thread(() -> {
                try {
                	logger.fine("Node " + node.getId() + " waiting " + TIMEOUT + "ms for responses");
                    Thread.sleep(TIMEOUT);
                    if (receivedOk && !receivedCoordinator) {
                    	// got OK but no COORDINATOR -> wait SHORT_TIMEOUT
                    	logger.info("Node " + node.getId() + " received OK but no COORDINATOR, waiting extra " 
                                + SHORT_TIMEOUT + "ms for COORDINATOR");
                        Thread.sleep(SHORT_TIMEOUT);
                        
                        if (!receivedCoordinator) {
                        	logger.info("Node " + node.getId() + " got no COORDINATOR after extra wait, restarting election");
                            inElection = false;           // allow re-entry
                            initiateElection();
                        }
                    }
                    else if (!receivedOk && !receivedCoordinator) {
                    	// no response -> self-promote
                    	logger.info("Node " + node.getId() + " received no responses, declaring self as leader");
                        declareLeader();
                    }
                    // Otherwise: receivedCoordinator==true → election over
                    else if (receivedCoordinator) {
                        logger.fine("Node " + node.getId() + " received COORDINATOR, election complete");
                    }
                } 
                catch (Exception e) {
                    logger.warning(e.toString());
                }
            }).start();
        }
	}
	
	/**
     * Handle an incoming ELECTION message: reply OK and possibly start own election.
     */
	public void handleElectionMessage(int fromId) {
		logger.info("Node " + node.getId() + " received ELECTION from Node " + fromId);
		
        if (fromId < node.getId()) {
            int peerPort = node.getPeerConfig().getPort(fromId);
            Message okMsg = new Message(Message.Type.OK, node.getId(), -1, "");
            node.getMessenger().sendMessage(peerPort, okMsg);

            if (!inElection) {
            	logger.fine("Node " + node.getId() + " starting own election after ELECTION message from lower ID");
                initiateElection();
            }
        }
    }
	
	public void handleOkMessage() {
		logger.fine("Node " + node.getId() + " received OK message");
        receivedOk = true;
    }

    public void handleCoordinatorMessage(int fromId) {
    	logger.fine("Node " + node.getId() + " received COORDINATOR message from Node " + fromId);
        receivedCoordinator = true;
        inElection = false;
        node.setLeader(fromId);
    }
	
    /**
     * Declare self as leader and broadcast COORDINATOR to lower-ID processes.
     */
	private void declareLeader() {
		logger.info("Node " + node.getId() + " is declaring itself as the new leader");
        node.setLeader(node.getId());
        inElection = false;
        // Send COORDINATOR to lower-ID peers
        for (int peerId : node.getPeerConfig().getPeerIds()) {
        	if (peerId < node.getId()) {
        		int peerPort = node.getPeerConfig().getPort(peerId);
        		Message coordMsg = new Message(Message.Type.COORDINATOR, node.getId(), -1, "");
                node.getMessenger().sendMessage(peerPort, coordMsg);
                logger.fine("Node " + node.getId() + " sending COORDINATOR message to Node " + peerId);
        	}
        }
    }
	
}
