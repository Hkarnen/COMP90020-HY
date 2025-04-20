package node;

/**
 * ChatManager handles user chat commands and incoming chat messages,
 * routing them through the elected leader for broadcast.
 */
public class ChatManager {

    private final Node node;

    public ChatManager(Node node) {
        this.node = node;
    }
    
    /**
     * Sends a CHAT message via the leader.
     * If this node is the leader, broadcast the message.
     * Otherwise, forward the message to the leader.
     */
    public void sendChat(String msg) {
    	
        Message message = new Message(Message.Type.CHAT, node.getId(), -1, msg);
        
        if (node.isLeader()) {
        	node.getMessenger().log("[ChatManager] Broadcasting message...");
            broadcastChat(message);
        } 
        else {
            int leaderId = node.getCurrentLeader();
            if (leaderId == -1) {
            	node.getMessenger().log("[ChatManager] No leader known. Message not sent.");
                // Optionally, trigger an election instead of sending the message
                // electionManager.initiateElection(); -- but here we focus on chat
            } 
            else {
            	node.getMessenger().log("[ChatManager] Forwarding message to leader Node " + leaderId);
                int leaderPort = node.getPeerConfig().getPort(leaderId);
                node.getMessenger().sendMessage(leaderPort, message);
            }
        }
    }
    
    public void handleIncomingChat(Message message) {
    	
        if (node.isLeader()) {
        	node.getMessenger().log("[ChatManager] Broadcasting received message...");
            broadcastChat(message);
        } 
        else {
        	node.getMessenger().log("[ChatManager] Chat received from Node " + message.getSenderId() + ": " + message.getContent());
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
