package node;

import java.util.logging.Logger;

/**
 * ChatManager handles user chat commands and incoming chat messages,
 * routing them through the elected leader for broadcast.
 */
public class ChatManager {
	private static final Logger logger = Logger.getLogger(ChatManager.class.getName());
    private final Node node;

    public ChatManager(Node node) {
        this.node = node;
    }
    
    /**
     * Sends a CHAT message via the leader.
     * If this node is the leader, broadcast the message.
     * Otherwise, forward the message to the leader.
     * @param msg message to be sent
     */
    public void sendChat(String msg) {
    	
        Message message = new Message(Message.Type.CHAT, node.getId(), -1, msg);
        
        if (node.isLeader()) {
        	// Leader displays its own message immediately
            node.getMessenger().displayChat(message);
        	logger.info("Broadcasting message from Node " + node.getId());
            broadcastChat(message);
        } 
        else {
            int leaderId = node.getCurrentLeader();
            if (leaderId == -1) {
            	logger.warning("No leader known. Message not sent from Node " + node.getId());
                // Optionally, trigger an election instead of sending the message
                // electionManager.initiateElection(); -- but here we focus on chat
            } 
            else {
            	logger.info("Forwarding message from Node " + node.getId() + " to leader Node " + leaderId);
                int leaderPort = node.getPeerConfig().getPort(leaderId);
                node.getMessenger().sendMessage(leaderPort, message);
            }
        }
    }
    
    /**
     * Handle an incoming chat message.
     * If this node is the leader, broadcast the message to all peers.
     * Also display the message in the UI.
     * @param message received message
     */
    public void handleIncomingChat(Message message) {
    	// Display the message in the UI
        node.getMessenger().displayChat(message);
    	
        if (node.isLeader()) {
        	logger.info("Node " + node.getId() + " broadcasting received message from Node " + message.getSenderId());
            broadcastChat(message);
        } 
        else {
        	logger.fine("Node " + node.getId() + " received chat from Node " + message.getSenderId());
        }
    }
    
    /**
     * Broadcast the chat message to all peers.
     */
    private void broadcastChat(Message message) {
    	logger.fine("Broadcasting message from Node " + message.getSenderId() + " to all peers");
    	
        for (int peerId : node.getPeerConfig().getPeerIds()) {
        	if (peerId != node.getId()) {
        		logger.fine("Broadcasting message from Node " + message.getSenderId() + " to all peers");
        		int port = node.getPeerConfig().getPort(peerId);
        		node.getMessenger().sendMessage(port, message);
        	}
        	
        }
    }
}
