package node;

import org.json.JSONException;

/**
 * Parses incoming JSON strings into Message objects and delegates
 * to the appropriate manager based on message type.
 */
public class MessageHandler {
	
	private final Node node;
	private final ElectionManager electionManager;
	private final ChatManager chatManager;
	
	public MessageHandler(Node node, ElectionManager electionManager, ChatManager chatManager) {
		this.node = node;
		this.electionManager = electionManager;
		this.chatManager = chatManager;
	}
	
	/**
     * Entry point for raw JSON message strings from sockets.
     */
	public void handleMessage(String rawJson) {
		if (rawJson == null) return;
		
		System.out.println("[Node " + node.getId() + "] Received: " + rawJson);
		Message msg;
		try {
			msg = Message.fromJson(rawJson);
		}
		catch (JSONException e) {
			System.err.println("[MessageHandler] Invalid JSON message: " + rawJson);
            return;
		}

		switch (msg.getType()) {
        case ELECTION:
            electionManager.handleElectionMessage(msg.getSenderId());
            break;
        case OK:
            electionManager.handleOkMessage();
            break;
        case COORDINATOR:
            electionManager.handleCoordinatorMessage(msg.getSenderId());
            break;
        case CHAT:
            chatManager.handleIncomingChat(msg);
            break;
        case HEARTBEAT:
            // future failure detection handling
            break;
    }
	}
	
}
