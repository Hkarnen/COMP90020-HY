package node;

import java.io.*;
import java.net.*;

import java.util.function.Consumer;

public class Messenger {

    private final Consumer<String> logger;
    private Node node;
    
    public Messenger(Consumer<String> logger) { 
    	this.logger = logger; 
    }
    
    // Sends a single-line message to the specified port
    public void sendMessage(int targetPort, String message) {
        try (Socket socket = new Socket("localhost", targetPort);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
             
            writer.write(message);
            writer.newLine();
            writer.flush();
        } 
        catch (IOException e) {
        	log("[Messenger] Could not send to port " + targetPort + " - " + e.getMessage());
        	if (node.isLeader()) {
        		Integer downId = node.getPeerConfig().getIdByPort(targetPort);
        		if (downId != null) {
        			node.getPeerConfig().removePeerByPort(targetPort);
        			Message down = new Message(Message.Type.PEER_DOWN, downId, -1, "");
        			peerBroadcast(down);
        			log("[Messenger] Broadcast PEER_DOWN for " + downId);
        		}
        	}
        }
    }
    
    public void sendMessage(int targetPort, Message message) {
    	sendMessage(targetPort, message.toJson());
    }
    
    // Central logging method
    public void log(String txt) {
        if (logger != null) {
            logger.accept(txt);
        } 
        // Only happens if we run Node on its own (without NodeUI)
        else {
        	
            System.out.println("else:" + txt);
        }
    }
    
    public void setNode(Node node) {
    	this.node = node;
    }
    
    private void peerBroadcast(Message m) {
    	for (int id : node.getPeerConfig().getPeerIds()) {
    		int port = node.getPeerConfig().getPort(id);
    		sendMessage(port, m);
    	}
    }
    
}
