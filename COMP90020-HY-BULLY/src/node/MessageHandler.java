package node;

public class MessageHandler {
	
	private final Node node;
	private final ElectionManager electionManager;
	private final ChatManager chatManager;
	
	public MessageHandler(Node node, ElectionManager electionManager, ChatManager chatManager) {
		this.node = node;
		this.electionManager = electionManager;
		this.chatManager = chatManager;
	}
	
	public void handleMessage(String msg) {
		if (msg == null) return;
		
		System.out.println("[Node " + node.getId() + "] Received: " + msg);

        if (msg.startsWith("ELECTION:")) {
            int fromId = parseId(msg);
            electionManager.handleElectionMessage(fromId);
        }
        else if (msg.startsWith("OK:")) {
            electionManager.handleOkMessage();
        }
        else if (msg.startsWith("COORDINATOR:")) {
        	int fromId = parseId(msg);
            electionManager.handleCoordinatorMessage(fromId);
        }
        else if (msg.startsWith("CHAT:")) {
            // Delegate chat message handling
            chatManager.handleIncomingChat(msg);
        }
	}
	
	private int parseId(String msg) {
		int colonIndex = msg.indexOf(':');
		if (colonIndex == -1) return -1;
		return Integer.parseInt(msg.substring(colonIndex + 1));
	}
	
}
