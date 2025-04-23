package node;

public class ShutdownManager {
    private final Node node;

    public ShutdownManager(Node node) {
        this.node = node;
    }

    public void quit() {
        Message downMsg = new Message(
                Message.Type.QUIT,
                node.getId(),
                -1,
                "Node " + node.getId() + " is shutting down"
        );

        if (node.isLeader()) {
            node.getMessenger().log("[ShutdownManager] I am leader, broadcast PEER_DOWN");
            broadcastToAll(downMsg);
        } else {
            int leaderId = node.getCurrentLeader();
            if (leaderId == -1) {
                node.getMessenger().log("[ShutdownManager] No leader known, broadcast to all");
                broadcastToAll(downMsg);
            } else {
                int leaderPort = node.getPeerConfig().getPort(leaderId);
                node.getMessenger().log(
                        "[ShutdownManager] Sending PEER_DOWN to leader Node " + leaderId
                );
                node.getMessenger().sendMessage(leaderPort, downMsg);
            }
        }
    }

    private void broadcastToAll(Message msg) {
        for (int peerId : node.getPeerConfig().getPeerIds()) {
            int port = node.getPeerConfig().getPort(peerId);
            node.getMessenger().sendMessage(port, msg);
        }
        node.getMessenger().log("[ShutdownManager] Broadcast done");
    }

    public void handlePeerDown(Message msg) {
        int downId = msg.getSenderId();
        node.getMessenger().log("[ShutdownManager] Peer " + downId + " down, update node list");
        node.getPeerConfig().removePeer(downId);
        if (downId == node.getCurrentLeader()) {
            node.getMessenger().log(
                    "[ShutdownManager] Leader down, begin a new election"
            );
            node.getElectionManager().initiateElection();
        }
    }
}
