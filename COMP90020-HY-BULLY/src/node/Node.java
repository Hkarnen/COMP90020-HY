package node;

import java.io.*;
import java.net.*;
import java.util.*;

public class Node {

    private int id;
    private int port;
    private PeerConfig peerConfig;

    // Injection of the managers
    private ElectionManager electionManager;
    private MessageHandler messageHandler;

    public Node(int id, int port, PeerConfig peerConfig) {
        this.id = id;
        this.port = port;
        this.peerConfig = peerConfig;

        // Create managers
        this.electionManager = new ElectionManager(id, peerConfig, this::sendMessage);
        this.messageHandler = new MessageHandler(id, electionManager);
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
                        sendHelloToPeers();
                        break;
                    case "ELECTION":
                        electionManager.initiateElection();
                        break;
                    case "QUIT":
                        System.out.println("[Node " + id + "] Exiting...");
                        return;
                    default:
                        System.out.println("Unknown command.");
                }
            }
        }
    }

    private void startServer() {
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

    private void sendHelloToPeers() {
        for (int peerId : peerConfig.getPeerIds()) {
            sendMessage(peerConfig.getPort(peerId), "HELLO from Node " + id);
        }
    }

    // Generic send
    public void sendMessage(int targetPort, String msg) {
        try (Socket socket = new Socket("localhost", targetPort);
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream()))) {

            writer.write(msg);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.out.println("[Node " + id + "] Could not send to " + targetPort);
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java node.Node <id> <port>");
            return;
        }
        int nodeId = Integer.parseInt(args[0]);
        int nodePort = Integer.parseInt(args[1]);

        Map<Integer, Integer> pm = new HashMap<>();
        pm.put(1, 5001);
        pm.put(2, 5002);
        pm.put(3, 5003);
        pm.remove(nodeId);

        PeerConfig config = new PeerConfig(pm);

        Node node = new Node(nodeId, nodePort, config);
        node.start();
    }
}
