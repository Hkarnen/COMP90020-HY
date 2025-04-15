package node;

public class ElectionManager {
	private final int myId;
	private final PeerConfig peerConfig;
	private final Sender sender;
	
	private boolean inElection = false;
	private boolean receivedOk = false;
	private boolean receivedCoordinator = false;
	
	public ElectionManager(int myId, PeerConfig peerConfig, Sender sender) {
		this.myId = myId;
		this.peerConfig = peerConfig;
		this.sender = sender;
	}
	
	public void initiateElection() {
		System.out.println("[Election] Node " + myId + " initiating election.");
        inElection = true;
        receivedOk = false;
        receivedCoordinator = false;

        boolean higherExists = false;
        
        for (int peerId : peerConfig.getPeerIds()) {
            if (peerId > myId) {
                int peerPort = peerConfig.getPort(peerId);
                sender.sendMessage(peerPort, "ELECTION:" + myId);
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
        if (fromId < myId) {
            int peerPort = peerConfig.getPort(fromId);
            sender.sendMessage(peerPort, "OK:" + myId);

            if (!inElection) {
                inElection = true;
                initiateElection();
            }
        }
    }
	
	public void handleOkMessage() {
        receivedOk = true;
    }

    public void handleCoordinatorMessage() {
        receivedCoordinator = true;
        inElection = false;
    }
	
	private void declareLeader() {
        System.out.println("[Election] Node " + myId + " is the new leader!");
        inElection = false;

        for (int peerId : peerConfig.getPeerIds()) {
            int peerPort = peerConfig.getPort(peerId);
            sender.sendMessage(peerPort, "COORDINATOR:" + myId);
        }
    }
	
	
}
