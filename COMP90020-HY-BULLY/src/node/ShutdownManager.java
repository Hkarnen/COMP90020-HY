package node;

import java.util.logging.Logger;

public class ShutdownManager {
	private static final Logger logger = Logger.getLogger(ShutdownManager.class.getName());
    private final Node node;

    public ShutdownManager(Node node) {
        this.node = node;
    }

    public void quit() {
    	logger.info("Node " + node.getId() + " initiating graceful shutdown");
        Message downMsg = new Message(
                Message.Type.QUIT,
                node.getId(),
                -1,
                "Node " + node.getId() + " is shutting down"
        );

        if (node.isLeader()) {
        	logger.info("Node " + node.getId() + " is leader, broadcasting shutdown to all peers");
            broadcastToAll(downMsg);
        } 
        else {
            int leaderId = node.getCurrentLeader();
            if (leaderId == -1) {
            	logger.info("Node " + node.getId() + " knows no leader, broadcasting shutdown to all peers");
                broadcastToAll(downMsg);
            } 
            else {
                int leaderPort = node.getPeerConfig().getPort(leaderId);
                logger.info("Node " + node.getId() + " sending shutdown notification to leader (Node " + leaderId + ")");
                node.getMessenger().sendMessage(leaderPort, downMsg);
            }
        }
    }

    private void broadcastToAll(Message msg) {
        for (int peerId : node.getPeerConfig().getPeerIds()) {
            int port = node.getPeerConfig().getPort(peerId);
            node.getMessenger().sendMessage(port, msg);
        }
        logger.fine("Node " + node.getId() + " shutdown broadcast complete");
    }

    public void handlePeerDown(Message msg) {
        int downId = msg.getSenderId();

        logger.info("Node " + node.getId() + " received notification that Node " + downId + " is down");
        
        node.getPeerConfig().removePeer(downId);
        if (node.getCurrentLeader() == downId) {
            logger.info("Current leader (Node " + downId + ") is down, reset leader to -1");
            node.setLeader(-1);
        }
        // Broadcast the node quitting to other nodes to let them know
        if (node.isLeader() && downId != node.getId()) {
            Message rebroadcast = new Message(Message.Type.PEER_DOWN, downId, -1, "Broadcasting node " + downId + " quitting");
            broadcastToAll(rebroadcast);
            logger.info("Node " + node.getId() + " (leader) rebroadcasting peer departure notification");
        }
    }
        
}
