package node;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class MembershipManager {
    private final Node node;
    private volatile boolean joinAck = false;
    private static final Logger logger = Logger.getLogger(MembershipManager.class.getName());
    
    public MembershipManager(Node node) {
        this.node = node;
    }

    /**
     * Attempts to join the cluster by contacting seed nodes from the config.
     * Will try each seed node until successful or all fail.
     */
    public void joinCluster() {
    	logger.info("Node " + node.getId() + " attempting to join cluster");
    	
        List<Integer> seeds = new ArrayList<>(node.getPeerConfig().getPeerIds());
        seeds.remove(Integer.valueOf(node.getId()));

        for (int seedId : seeds) {
            if (joinAck) break;

            int seedPort = node.getPeerConfig().getPort(seedId);
            logger.info("Node " + node.getId() + " sending JOIN to seed Node " 
                    + seedId + " on port " + seedPort);
            sendJoinRequest(seedPort);

            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

            if (!joinAck) {
            	logger.info("Node " + node.getId() + " received no response from Node "
                        + seedId + ", trying next seed...");
            }
        }

        if (joinAck) {
        	logger.info("Node " + node.getId() + " successfully joined the cluster");
        } else {
        	logger.warning("Node " + node.getId() + " failed to join the cluster - all seeds unreachable");
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
        
        logger.info("Node " + node.getId() + " handling JOIN request from Node " + newId);
        node.getPeerConfig().addPeer(newId, newPort);

        Message newNode = new Message(
                Message.Type.NEW_NODE, newId, -1,
                String.valueOf(newPort)
        );
        
        logger.fine("Node " + node.getId() + " notifying all peers about new Node " + newId);
        for (int peer : node.getPeerConfig().getPeerIds()) {
            node.getMessenger().sendMessage(node.getPeerConfig().getPort(peer), newNode);
        }
        
        // Inform new node about current leader
        int leaderId = node.getCurrentLeader();
        if (leaderId != -1) {
        	logger.info("Node " + node.getId() + " informing new Node " + newId
                    + " that current leader is Node " + leaderId);
            Message coord = new Message(
                    Message.Type.COORDINATOR,
                    leaderId,
                    -1,
                    ""
            );
            node.getMessenger().sendMessage(newPort, coord);
        }
        else{
        	logger.info("Node " + node.getId() + " informing new Node " + newId
                    + " that there is no current leader");
        }
        // Inform new node about all existing peers
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
            logger.fine("Node " + node.getId() + " informed new Node " + newId
                    + " about existing peer Node " + peerId);
        }

    }

    public void handleNewNode(Message msg) {
        int newId   = msg.getSenderId();
        int newPort = Integer.parseInt(msg.getContent());

        if (newId != node.getId()) {
        	// Message about another node
            if (!isReachable("localhost", newPort, 500)) {
                logger.warning("Node " + node.getId() + " skipping NEW_NODE for unreachable peer "
                        + newId + " on port " + newPort);
                return;
            }
            logger.info("Node " + node.getId() + " adding new peer: Node " + newId);
            node.getPeerConfig().addPeer(newId, newPort);
        } else {
        	joinAck = true;
            logger.info("Node " + node.getId() + " join request acknowledged by cluster");
        }
    }
    
    private boolean isReachable(String host, int port, int timeoutMillis) {
        try (Socket sock = new Socket()) {
            sock.connect(new InetSocketAddress(host, port), timeoutMillis);
            return true;
        }
        catch (IOException e) {
        	logger.fine("Failed to connect to " + host + ":" + port);
            return false;
        }
    }
}


