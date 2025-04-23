package node;

import java.io.*;
import java.net.*;
import java.util.*;

public class Node {

    private int id;
    private int port;
    private PeerConfig peerConfig;
    
    private int currentLeader = -1;
    private boolean isLeader = false;

    // Injection of the managers
    private ElectionManager electionManager;
    private MessageHandler messageHandler;
    private ChatManager chatManager;
    private Messenger messenger;
    private HeartbeatManager heartbeatManager;
    private MembershipManager membershipManager;
    private final ShutdownManager shutdownManager;
    private final boolean isBootstrap;

    public Node(int id, int port, boolean isBootstrap, PeerConfig peerConfig, Messenger messenger) {
    	
        this.id = id;
        this.port = port;
        this.peerConfig = peerConfig;
        this.messenger = messenger;
        this.isBootstrap   = isBootstrap;
        // Create managers
        this.electionManager = new ElectionManager(this);
        this.chatManager = new ChatManager(this);
        this.messageHandler = new MessageHandler(this);
        this.heartbeatManager = new HeartbeatManager(this);
        this.shutdownManager = new ShutdownManager(this);
        this.membershipManager = new MembershipManager(this);
        heartbeatManager.start();
    }

    public void startServer() {
    	
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                String msg = reader.readLine();
                messageHandler.handleMessage(msg);
                client.close();
            }
        } 
        catch (BindException e) {
            System.err.println("[Node " + id + "] Port " + port + " in use!");
            System.exit(1);
        } 
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setLeader(int leaderId) {
    	
        this.currentLeader = leaderId;
        if (leaderId == id) {
            isLeader = true;
            System.out.println("[Node " + id + "] I am the new leader.");
        } 
        else {
            isLeader = false;
            System.out.println("[Node " + id + "] Updated leader to Node " + leaderId);
        }
    }
    
    public int getCurrentLeader() {
        return currentLeader;
    }
    public boolean isBootstrap() {
        return isBootstrap;
    }
    public boolean isLeader() {
        return isLeader;
    }
    
    public int getId() {
        return id;
    }
    
    public int getPort() {
    	return port;
    }
    
    public ChatManager getChatManager() {
    	return chatManager;
    }
    
    public ElectionManager getElectionManager() {
    	return electionManager;
    }
    
    public HeartbeatManager getHeartbeatManager() {
    	return heartbeatManager;
    }
    public ShutdownManager getShutdownManager() {
        return shutdownManager;
    }
    public MessageHandler getMessageHandler() {
    	return messageHandler;
    }
    public MembershipManager getMembershipManager() {
    	return membershipManager;
    }
    
    public Messenger getMessenger() {
    	return messenger;
    }
    
    public PeerConfig getPeerConfig() {
    	return peerConfig;
    }

}
