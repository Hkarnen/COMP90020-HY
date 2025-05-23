package node;

import java.io.*;
import java.net.*;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Messenger handles low-level communication between nodes with sending messages
 * Also displays messages to the UI
 */
public class Messenger {
    private static final Logger logger = Logger.getLogger(Messenger.class.getName());
    private final Consumer<Message> chatDisplayFunction;
    private Node node;
    
    public Messenger(Consumer<Message> chatDisplayFunction) { 
    	this.chatDisplayFunction = chatDisplayFunction;
    }
    
    /**
     * Sends a single message to the target port with TCP and closes connection
     * @param targetPort message destination
     * @param message - message to be sent
     */
    public void sendMessage(int targetPort, String message) {
        try (Socket socket = new Socket("localhost", targetPort);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
             
            writer.write(message);
            writer.newLine();
            writer.flush();
            
            logger.fine("Sent message to port " + targetPort);
        } 
        catch (IOException e) {
        	logger.log(Level.WARNING, "Failed to send message to port " + targetPort, e);
        	// If leader failed to send message to a node, it will tell other nodes that that node is down (remove it)
        	// If leader itself failed and a node tries to message the leader, nothing will happen
        	// That will be handled by manual or automatic election 
        	if (node.isLeader()) {
        		Integer downId = node.getPeerConfig().getIdByPort(targetPort);
        		if (downId != null) {
        			node.getPeerConfig().removePeerByPort(targetPort);
        			Message down = new Message(Message.Type.PEER_DOWN, downId, -1, "");
        			peerBroadcast(down);
        			logger.info("Removing peer " + downId + " from application");
        		}
        	}
        }
    }
    
    /**
     * Message send function overload
     * @param targetPort
     * @param message
     */
    public void sendMessage(int targetPort, Message message) {
    	sendMessage(targetPort, message.toJson());
    }
    
    /**
     * Displays chat messages to UI
     * @param message message to be displayed
     */
    public void displayChat(Message message) {
        if (chatDisplayFunction != null && message.getType() == Message.Type.CHAT) {
            chatDisplayFunction.accept(message);
        } 
        else {
            // Fallback for when UI isn't available (like during testing)
            logger.info("CHAT [Node " + message.getSenderId() + "]: " + message.getContent());
        }
    }
    
    /**
     * Broadcast message to other nodes - only for removing unreachable peers
     * @param m message
     */
    private void peerBroadcast(Message m) {
    	logger.info("MESSENGER: broadcast removing peer");
    	for (int id : node.getPeerConfig().getPeerIds()) {
    		// Skip myself
    		if (id == node.getId()) continue;
    		int port = node.getPeerConfig().getPort(id);
    		sendMessage(port, m);
    	}
    }
    
    public void setNode(Node node) {
    	this.node = node;
    }
    
    
    
}
