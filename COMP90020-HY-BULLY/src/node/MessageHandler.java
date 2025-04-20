package node;

import java.util.function.Consumer;
import org.json.JSONException;

/**
 * Parses incoming JSON strings into Message objects and delegates
 * to the appropriate manager based on message type.
 */
public class MessageHandler {
	
	private final Node node;
	private final Consumer<String> logger;
	
	public MessageHandler(Node node, Consumer<String> logger) {
		this.node = node;
		this.logger = logger;
	}
	
	/**
     * Entry point for raw JSON message strings from sockets.
     */
	public void handleMessage(String rawJson) {
		if (rawJson == null) return;
		
		System.out.println("[Node " + node.getId() + "] Received: " + rawJson);
		logger.accept("[Node " + node.getId() + "] Received: " + msg);
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
    }
	}
	
}
