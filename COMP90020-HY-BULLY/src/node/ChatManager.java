package node;

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
     * Sends a HELLO message via the leader.
     * If this node is the leader, broadcast the message.
     * Otherwise, forward the message to the leader.
     */
    public void sendHello() {
        String message = "HELLO from Node " + node.getId();
        
        if (node.isLeader()) {
            System.out.println("[ChatManager] Broadcasting HELLO message...");
            broadcastChat("HELLO");
        } 
        else {
            int leaderId = node.getCurrentLeader();
            if (leaderId == -1) {
                System.out.println("[ChatManager] No leader known. ");
                // Optionally, trigger an election instead of sending the message
                // electionManager.initiateElection(); -- but here we focus on chat
            } 
            else {
                System.out.println("[ChatManager] Forwarding HELLO to leader Node " + leaderId);
                int leaderPort = peerConfig.getPort(leaderId);
                messenger.sendMessage(leaderPort, message);
            }
        }
    }
    
    public void handleIncomingChat(String message) {
        if (node.isLeader()) {
            System.out.println("[ChatManager] (Leader) Broadcasting chat message: " + message);
            broadcastChat(message);
        } else {
            System.out.println("[ChatManager] Chat message received: " + message);
        }
    }
    
    /**
     * Broadcast the chat message to all peers.
     */
    private void broadcastChat(String message) {
        for (int peerId : peerConfig.getPeerIds()) {
            int port = peerConfig.getPort(peerId);
            messenger.sendMessage(port, message);
        }
    }
}
