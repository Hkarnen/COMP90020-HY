package node;

import org.json.JSONException;

/**
 * Parses incoming JSON strings into Message objects and delegates
 * to the appropriate manager based on message type.
 */
public class MessageHandler {
	
	private final Node node;
	
	public MessageHandler(Node node) {
		this.node = node;
	}
	
	/**
     * Entry point for raw JSON message strings from sockets.
     */
	public void handleMessage(String rawJson) {
		if (rawJson == null) return;
		// Maybe move this down below msg
		node.getMessenger().log("[Node " + node.getId() + "] Received: " + rawJson);
		
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
        		node.getElectionManager().handleElectionMessage(msg.getSenderId());
        		break;
        	case OK:
        		node.getElectionManager().handleOkMessage();
        		break;
        	case COORDINATOR:
        		node.getElectionManager().handleCoordinatorMessage(msg.getSenderId());
        		break;
        	case CHAT:
        		node.getChatManager().handleIncomingChat(msg);
        		break;
        	case HEARTBEAT:
        		node.getHeartbeatManager().receivedHeartbeat();
        		break;
        	case QUIT:
				node.getShutdownManager().handlePeerDown(msg);
        		break;
		}
	}
	
}
