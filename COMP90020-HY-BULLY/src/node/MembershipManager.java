package node;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
public class MembershipManager {
    private final Node node;
    private volatile boolean joinAck = false;

    public MembershipManager(Node node) {
        this.node = node;
    }

    public void joinCluster() {
        List<Integer> seeds = new ArrayList<>(node.getPeerConfig().getPeerIds());
        seeds.remove(Integer.valueOf(node.getId()));

        for (int seedId : seeds) {
            if (joinAck) break;

            int seedPort = node.getPeerConfig().getPort(seedId);
            node.getMessenger().log("[Membership] Sending JOIN to seed Node "
                    + seedId + " on port " + seedPort);
            sendJoinRequest(seedPort);

            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

            if (!joinAck) {
                node.getMessenger().log("[Membership] No response from Node "
                        + seedId + ", trying next...");
            }
        }

        if (joinAck) {
            node.getMessenger().log("[Membership] Successfully joined the cluster");
        } else {
            node.getMessenger().log("[Membership] Failed to join any seed");
        }
    }

    public void sendJoinRequest(int bootstrapPort) {
        Message join = new Message(
                Message.Type.JOIN,
                node.getId(), -1,
                String.valueOf(node.getPort())
        );
        node.getMessenger().sendMessage(bootstrapPort, join);
    }

    public void handleJoin(Message msg) {
        int newId   = msg.getSenderId();
        int newPort = Integer.parseInt(msg.getContent());
        node.getMessenger().log("[Membership] Handling JOIN from Node " + newId);
        node.getPeerConfig().addPeer(newId, newPort);

        Message newNode = new Message(
                Message.Type.NEW_NODE, newId, -1,
                String.valueOf(newPort)
        );
        for (int peer : node.getPeerConfig().getPeerIds()) {
            node.getMessenger().sendMessage(node.getPeerConfig().getPort(peer), newNode);
        }
        int leaderId = node.getCurrentLeader();
        if (leaderId != -1) {
            node.getMessenger().log(
                    "[Membership] Informing Node " + newId
                            + " that current Leader is Node " + leaderId
            );
            Message coord = new Message(
                    Message.Type.COORDINATOR,
                    leaderId,
                    -1,
                    ""
            );
            node.getMessenger().sendMessage(newPort, coord);
        }
        else{
            node.getMessenger().log(
                    "[Membership] Informing Node " + newId
                            + " that no Leader now "
            );
        }
        for (int peerId : node.getPeerConfig().getPeerIds()) {
            if (peerId == newId) continue;

            int peerPort = node.getPeerConfig().getPort(peerId);
            Message notify = new Message(
                    Message.Type.NEW_NODE,
                    peerId,
                    -1,
                    String.valueOf(peerPort)
            );
            node.getMessenger().sendMessage(newPort, notify);
            node.getMessenger().log(
                    "[Membership] Informed new Node " + newId
                            + " about existing peer " + peerId
            );
        }

    }

    public void handleNewNode(Message msg) {
        int newId   = msg.getSenderId();
        int newPort = Integer.parseInt(msg.getContent());

        if (newId != node.getId()) {
            if (!isReachable("localhost", newPort, 500)) {
                node.getMessenger().log(
                        "[Membership] Skipping NEW_NODE for offline peer "
                                + newId + "@" + newPort
                );
                return;
            }
            node.getMessenger().log("[Membership] New node added: " + newId);
            node.getPeerConfig().addPeer(newId, newPort);
        } else {
            joinAck = true;
            node.getMessenger().log("[Membership] Join acknowledged by cluster");
        }
    }
    private boolean isReachable(String host, int port, int timeoutMillis) {
        try (Socket sock = new Socket()) {
            sock.connect(new InetSocketAddress(host, port), timeoutMillis);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }
}


