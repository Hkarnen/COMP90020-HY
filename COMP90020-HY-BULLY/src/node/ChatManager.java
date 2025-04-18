package node;

/**
 * ChatManager handles user chat commands and incoming chat messages,
 * routing them through the elected leader for broadcast.
 */
public class ChatManager {

    private final Node node;
    private final PeerConfig peerConfig;
    private final Messenger messenger;

    public ChatManager(Node node, PeerConfig peerConfig, Messenger messenger) {
        this.node = node;
        this.peerConfig = peerConfig;
        this.messenger = messenger;
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
            broadcastChat(message);
        } 
        else {
            int leaderId = node.getCurrentLeader();
            if (leaderId == -1) {
                System.out.println("[ChatManager] No leader known. ");
                // Optionally, trigger an election instead of sending the message
                // electionManager.initiateElection(); -- but here we focus on chat
            } 
            else {
                System.out.println("[ChatManager] Forwarding message to leader Node " + leaderId);
                int leaderPort = peerConfig.getPort(leaderId);
                messenger.sendMessage(leaderPort, message);
            }
        }
    }
    
    public void handleIncomingChat(Message message) {
        if (node.isLeader()) {
            System.out.println("[ChatManager] (Leader) Broadcasting chat message: " + message.getContent());
            broadcastChat(message);
        } else {
        	System.out.println("[ChatManager] Chat received from Node " + message.getSenderId() + ": " + message.getContent());
        }
    }
    
    /**
     * Broadcast the chat message to all peers.
     */
    private void broadcastChat(Message message) {
        for (int peerId : peerConfig.getPeerIds()) {
            int port = peerConfig.getPort(peerId);
            messenger.sendMessage(port, message);
        }
    }
}
