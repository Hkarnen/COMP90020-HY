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

    public Node(int id, int port, PeerConfig peerConfig) {
        this.id = id;
        this.port = port;
        this.peerConfig = peerConfig;
        this.messenger = new Messenger();

        // Create managers
        this.electionManager = new ElectionManager(this, peerConfig, messenger);
        this.chatManager = new ChatManager(this, peerConfig, messenger);
        this.messageHandler = new MessageHandler(this, electionManager, chatManager);
    }

    public void start() {
        new Thread(this::startServer).start();

        System.out.println("[Node " + id + "] Started on port " + port);
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Commands: HELLO, ELECTION, or QUIT");
            while (true) {
                System.out.print("Node " + id + " > ");
                String cmd = scanner.nextLine().trim().toUpperCase();
                switch (cmd) {
                    case "HELLO":
                        chatManager.sendHello();
                        break;
                    case "ELECTION":
                        electionManager.initiateElection();
                        break;
                    case "QUIT":
                        System.out.println("[Node " + id + "] Exiting...");
                        System.exit(1);;
                    default:
                        System.out.println("Unknown command.");
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
