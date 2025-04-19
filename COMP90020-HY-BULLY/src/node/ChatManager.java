package node;

import java.util.function.Consumer;

public class ChatManager {

    private final Node node;
    private final PeerConfig peerConfig;
    private final Messenger messenger;
    private final Consumer<String> logger;

    public ChatManager(Node node, PeerConfig peerConfig, Messenger messenger, Consumer<String> logger) {
        this.node = node;
        this.peerConfig = peerConfig;
        this.messenger = messenger;
        this.logger = logger;
    }
    
    /**
     * Sends a HELLO message via the leader.
     * If this node is the leader, broadcast the message.
     * Otherwise, forward the message to the leader.
     */
    public void sendHello() {
        String message = "CHAT:HELLO from Node " + node.getId();
        if (node.isLeader()) {
            System.out.println("[ChatManager] Broadcasting HELLO message...");
            logger.accept("[ChatManager] Broadcasting HELLO message...");
            broadcastChat("HELLO");
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
                System.out.println("[ChatManager] Forwarding HELLO to leader Node " + leaderId);
                logger.accept("[ChatManager] Forwarding HELLO to leader Node " + leaderId);
                int leaderPort = peerConfig.getPort(leaderId);
                messenger.sendMessage(leaderPort, message);
            }
        }
    }
    
    public void handleIncomingChat(String message) {
        if (node.isLeader()) {
            logger.accept("[ChatManager] Broadcasting HELLO message...");
            System.out.println("[ChatManager] (Leader) Broadcasting chat message: " + message);
            broadcastChat(message);
        } else {
            logger.accept("[ChatManager] Chat message received: " + message);
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
