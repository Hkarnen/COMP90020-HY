package node;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;

/**
 * Parses incoming JSON strings into Message objects and delegates
 * to the appropriate manager based on message type.
 */
public class MessageHandler {
	private static final Logger logger = Logger.getLogger(MessageHandler.class.getName());
	private final Node node;
	
	public MessageHandler(Node node) {
		this.node = node;
	}
	
	/**
     * Entry point for raw JSON message strings from sockets.
     */
	public void handleMessage(String rawJson) {
		if (rawJson == null) return;
		
		logger.fine("Node " + node.getId() + " received: " + rawJson);
		
		Message msg;
		try {
			msg = Message.fromJson(rawJson);
		}
		catch (JSONException e) {
			logger.log(Level.WARNING, "Invalid JSON message: " + rawJson, e);
            return;
		}
		
		switch (msg.getType()) {
        	case ELECTION:
        		logger.fine("Node " + node.getId() + " handling ELECTION message from Node " + msg.getSenderId());
        		node.getElectionManager().handleElectionMessage(msg.getSenderId());
        		break;
            
        	case OK:
        		logger.fine("Node " + node.getId() + " handling OK message");
        		node.getElectionManager().handleOkMessage();
        		break;
            
        	case COORDINATOR:
        		logger.fine("Node " + node.getId() + " handling COORDINATOR message from Node " + msg.getSenderId());
        		node.getElectionManager().handleCoordinatorMessage(msg.getSenderId());
        		break;
            
        	case CHAT:
        		logger.fine("Node " + node.getId() + " handling CHAT message from Node " + msg.getSenderId());
        		node.getChatManager().handleIncomingChat(msg);
        		break;
            
        	case HEARTBEAT:
        		logger.fine("Node " + node.getId() + " handling HEARTBEAT message");
        		node.getHeartbeatManager().receivedHeartbeat(msg);
        		break;
            
        	case QUIT:
        	case PEER_DOWN:
        		logger.fine("Node " + node.getId() + " handling " + msg.getType() + " message");
        		node.getShutdownManager().handlePeerDown(msg);
        		break;
            
        	case JOIN:
        		logger.fine("Node " + node.getId() + " handling JOIN message from Node " + msg.getSenderId());
        		node.getMembershipManager().handleJoin(msg);
        		break;
            
        	case NEW_NODE:
        		logger.fine("Node " + node.getId() + " handling NEW_NODE message");
        		node.getMembershipManager().handleNewNode(msg);
        		break;
            
        	default:
        		logger.warning("Node " + node.getId() + " received unknown message type: " + msg.getType());
		}
	}
	
}
