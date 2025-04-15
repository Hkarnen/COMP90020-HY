package node;

public class MessageHandler {
	
	private final int myId;
	private final ElectionManager electionManager;
	
	public MessageHandler(int myId, ElectionManager manager) {
		this.myId = myId;
		this.electionManager = manager;
	}
	
	public void handleMessage(String msg) {
		if (msg == null) return;
		
		System.out.println("[Node " + myId + "] Received: " + msg);

        if (msg.startsWith("ELECTION:")) {
            int fromId = parseId(msg);
            electionManager.handleElectionMessage(fromId);
        }
        else if (msg.startsWith("OK:")) {
            electionManager.handleOkMessage();
        }
        else if (msg.startsWith("COORDINATOR:")) {
            electionManager.handleCoordinatorMessage();
        }
//        else {
//        	
//        }
	}
	
	private int parseId(String msg) {
		int colonIndex = msg.indexOf(':');
		if (colonIndex == -1) return -1;
		return Integer.parseInt(msg.substring(colonIndex + 1));
	}
	
}
