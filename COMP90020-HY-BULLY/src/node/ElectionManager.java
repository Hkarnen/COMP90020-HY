package node;

public class ElectionManager {
	private final Node node;
	private final PeerConfig peerConfig;
	private final Messenger messenger;
	
	private boolean inElection = false;
	private boolean receivedOk = false;
	private boolean receivedCoordinator = false;
	
	public ElectionManager(Node node, PeerConfig peerConfig, Messenger messenger) {
		this.node = node;
		this.peerConfig = peerConfig;
		this.messenger = messenger;
	}
	
	public void initiateElection() {
		System.out.println("[Election] Node " + node.getId() + " initiating election.");
        inElection = true;
        receivedOk = false;
        receivedCoordinator = false;

        boolean higherExists = false;
        
        for (int peerId : peerConfig.getPeerIds()) {
            if (peerId > node.getId()) {
                int peerPort = peerConfig.getPort(peerId);
                messenger.sendMessage(peerPort, "ELECTION:" + node.getId());
                higherExists = true;
            }
        }

        if (!higherExists) {
            declareLeader();
        } 
        else {
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    if (!receivedOk && !receivedCoordinator) {
                        declareLeader();
                    }
                } 
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
	}
	
	public void handleElectionMessage(int fromId) {
        if (fromId < node.getId()) {
            int peerPort = peerConfig.getPort(fromId);
            messenger.sendMessage(peerPort, "OK:" + node.getId());

            if (!inElection) {
                inElection = true;
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
	
	private void declareLeader() {
        System.out.println("[Election] Node " + node.getId() + " is the new leader!");
        inElection = false;

        for (int peerId : peerConfig.getPeerIds()) {
            int peerPort = peerConfig.getPort(peerId);
            messenger.sendMessage(peerPort, "COORDINATOR:" + node.getId());
        }
    }
	
	
}
