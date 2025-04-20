package node;

import java.util.function.Consumer;

import java.util.function.Consumer;

/**
 * ChatManager handles user chat commands and incoming chat messages,
 * routing them through the elected leader for broadcast.
 */
public class ChatManager {

    private final Node node;
    private final Consumer<String> logger;

    public ChatManager(Node node, Consumer<String> logger) {
        this.node = node;
        this.logger = logger;
    }
    
    /**
     * Sends a CHAT message via the leader.
     * If this node is the leader, broadcast the message.
     * Otherwise, forward the message to the leader.
     */
    public void sendChat(String msg) {
        Message message = new Message(Message.Type.CHAT, node.getId(), -1, msg);
        
        if (node.isLeader()) {
            System.out.println("[ChatManager] Broadcasting message...");
            logger.accept("[ChatManager] Broadcasting HELLO message...");
            broadcastChat(message);
        } 
        else {
            int leaderId = node.getCurrentLeader();
            if (leaderId == -1) {
                System.out.println("[ChatManager] No leader known. ");
                logger.accept("[ChatManager] No leader known. Message not sent.");
                // Optionally, trigger an election instead of sending the message
                // electionManager.initiateElection(); -- but here we focus on chat
            } 
            else {
                System.out.println("[ChatManager] Forwarding message to leader Node " + leaderId);
                logger.accept("[ChatManager] Forwarding HELLO to leader Node " + leaderId);
                int leaderPort = node.getPeerConfig().getPort(leaderId);
                node.getMessenger().sendMessage(leaderPort, message);
            }
        }
    }
    
    public void handleIncomingChat(Message message) {
        if (node.isLeader()) {
            logger.accept("[ChatManager] Broadcasting HELLO message...");
            System.out.println("[ChatManager] (Leader) Broadcasting chat message: " + message.getContent());
            broadcastChat(message);
        } else {
            logger.accept("[ChatManager] Chat message received: " + message);
        	System.out.println("[ChatManager] Chat received from Node " + message.getSenderId() + ": " + message.getContent());
        }
    }
    
    /**
     * Broadcast the chat message to all peers.
     */
    private void broadcastChat(Message message) {
        for (int peerId : node.getPeerConfig().getPeerIds()) {
            int port = node.getPeerConfig().getPort(peerId);
            node.getMessenger().sendMessage(port, message);
        }
    }
}
