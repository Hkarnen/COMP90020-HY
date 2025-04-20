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

    public Node(int id, int port, PeerConfig peerConfig) {
        this.id = id;
        this.port = port;
        this.peerConfig = peerConfig;
        this.messenger = new Messenger();

        // Create managers
        this.electionManager = new ElectionManager(this);
        this.chatManager = new ChatManager(this);
        this.messageHandler = new MessageHandler(this);
        this.heartbeatManager = new HeartbeatManager(this);
        heartbeatManager.start();
    }

    public void setManagers(ElectionManager em, ChatManager cm, MessageHandler mh) {
        this.electionManager  = em;
        this.chatManager      = cm;
        this.messageHandler   = mh;
    }
    public void start() {
        new Thread(this::startServer).start();

        System.out.println("[Node " + id + "] Started on port " + port);
        try (Scanner scanner = new Scanner(System.in)) {
        	System.out.println("Type '/election' to trigger election, '/quit' to exit, or enter chat message:");
            while (true) {
                System.out.println("Node " + id + " > ");
                String cmd = scanner.nextLine().trim();
                switch (cmd) {
                    case "/election":
                        electionManager.initiateElection();
                        break;
                    case "/quit":
                        System.out.println("[Node " + id + "] Exiting...");
                        System.exit(0);;
                    default:
                        chatManager.sendChat(cmd);
                }
            }
        }
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
    
    public MessageHandler getMessageHandler() {
    	return messageHandler;
    }
    
    public Messenger getMessenger() {
    	return messenger;
    }
    
    public PeerConfig getPeerConfig() {
    	return peerConfig;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java node.Node <id> <port>");
            return;
        }
        int nodeId = Integer.parseInt(args[0]);
        int nodePort = Integer.parseInt(args[1]);
        
        PeerConfig config;
        try {
            config = PeerConfig.loadFromFile("../COMP90020-HY-BULLY/src/properties/config");
        } 
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        // Remove self from the peer list
        config.getPeerMap().remove(nodeId);

        Node node = new Node(nodeId, nodePort, config);
        node.start();
    }
}
