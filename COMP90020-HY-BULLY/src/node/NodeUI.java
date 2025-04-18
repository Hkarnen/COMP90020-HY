package node;
import java.io.*;
import java.net.*;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.function.Consumer;

import java.util.HashMap;
import java.util.Map;
public class NodeUI extends Application {
    private Node1 node;
    private TextArea logArea;
    private Button helloBtn;
    private Button electionBtn;
    private Button quitBtn;

    @Override
    public void start(Stage primaryStage) {
        // Create input fields and labels for node ID and port
        Label idLabel = new Label("Node ID:");
        TextField idField = new TextField();
        idField.setPrefWidth(60);

        Label portLabel = new Label("Port:");
        TextField portField = new TextField();
        portField.setPrefWidth(80);

        // Button to start the node
        Button startBtn = new Button("Start Node");
        startBtn.setOnAction(e -> initializeNode(idField.getText(), portField.getText()));

        // Text area to display logs
        logArea = new TextArea();
        logArea.setEditable(false);

        // Control buttons, disabled until node starts
        helloBtn = new Button("HELLO");
        helloBtn.setDisable(true);
        helloBtn.setOnAction(e -> node.sendHello());

        electionBtn = new Button("ELECTION");
        electionBtn.setDisable(true);
        electionBtn.setOnAction(e -> node.initiateElection());

        quitBtn = new Button("QUIT");
        quitBtn.setDisable(true);
        quitBtn.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        // Layout setup
        HBox topBar = new HBox(10, idLabel, idField, portLabel, portField, startBtn);
        topBar.setPadding(new Insets(10));

        HBox bottomBar = new HBox(10, helloBtn, electionBtn, quitBtn);
        bottomBar.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(logArea);
        root.setBottom(bottomBar);

        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Distributed Node UI");

        // Handle window close event to exit the application
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    private void initializeNode(String idText, String portText) {
        try {
            int id = Integer.parseInt(idText.trim());
            int port = Integer.parseInt(portText.trim());

            Map<Integer, Integer> peers = new HashMap<>();
            peers.put(1, 5001);
            peers.put(2, 5002);
            peers.put(3, 5003);
            peers.remove(id);

            node = new Node1(id, port, peers, this::appendLog);
            Thread serverThread = new Thread(node::startServer);
            serverThread.setDaemon(true);
            serverThread.start();

            appendLog("[Node " + id + "] Started on port " + port);

            // Enable control buttons
            helloBtn.setDisable(false);
            electionBtn.setDisable(false);
            quitBtn.setDisable(false);

        } catch (NumberFormatException ex) {
            appendLog("Invalid input: " + ex.getMessage());
        }
    }

    private void appendLog(String text) {
        Platform.runLater(() -> logArea.appendText(text + "\n"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
class Node1 {


    private int id;
    private int port;
    private Map<Integer, Integer> peerPorts = new HashMap<>();
    private boolean receivedOk = false;
    private boolean receivedCoordinator = false;
    private boolean inElection = false;
    private Consumer<String> logger;

    public Node1(int id, int port, Map<Integer, Integer> peers, Consumer<String> logger) {
        this.id = id;
        this.port = port;
        this.peerPorts = peers;
        this.logger = logger;
    }

//	 public void start() {
//		 new Thread(this::startServer).start();
//
//		 System.out.println("[Node " + id + "] Started on port " + port);
//		 System.out.println("Press Enter to say hello to peers");
//
//		 try (Scanner scanner = new Scanner(System.in)) {
//			 System.out.println("Commands: HELLO, ELECTION, or QUIT to exit.");
//
//			 while (true) {
//				 System.out.println("Node " + id + " > ");
//				 String cmd = scanner.nextLine().trim().toUpperCase();
//				 if (cmd.equals("HELLO")) {
//	                 sendHelloToPeers();
//	             }
//				 else if (cmd.equals("ELECTION")) {
//	                 initiateElection();
//	             }
//				 else if (cmd.equals("QUIT")) {
//					 System.out.println("[Node " + id + "] Exiting...");
//	                 System.exit(0);;  // end the loop
//				 }
//				 else {
//					 System.out.println("Unknown command. Type HELLO, ELECTION, or QUIT.");
//				 }
//
//			 }
//		 }
//	 }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String msg = reader.readLine();
                handleMessage(msg);
                client.close();
            }
        }
        catch (BindException e) {
            logger.accept("[Node " + id + "] Error: " + e.getMessage());
            System.err.println("[Node " + id + "] Port " + port + " is already in use!");
            System.exit(1);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void sendHello() {
        for (Map.Entry<Integer, Integer> entry : peerPorts.entrySet()) {
            Integer peerId   = entry.getKey();
            Integer peerPort = entry.getValue();
            sendMessage(peerPort, "HELLO from Node " + id);
        }
    }

//	public void initiateElection() {
//		 logger.accept("[Node " + id + "] Initiating election...");
//		 inElection = true;
//		 receivedOk = false;
//		 receivedCoordinator = false;
//
//		 boolean higherExists = false;
//		 Map<Integer,Integer> snapshot = new HashMap<>(peerPorts);
//		 for (Map.Entry<Integer,Integer> e : snapshot.entrySet()) {
//			int peerId = e.getKey();
//			int peerPort = e.getValue();
//			if (peerId > id) {
//				higherExists = true;
//				try {
//					sendRaw(peerPort, "ELECTION:" + id);
//				} catch (IOException ex) {
//					handlePeerDown(peerId);
//				}
//			}
//		}
//
//		 if (!higherExists) {
//			 // No higher ID node is alive, declare self leader
//			 declareLeader();
//		 }
//		 else {
//			 // Wait for a short time for OK or COORDINATOR messages
//			 new Thread(() -> {
//				 try {
//					 Thread.sleep(3000); // Placeholder timeout T
//					 if (!receivedOk || !receivedCoordinator) {
//						 // No response from higher node, so self-promote
//						 declareLeader();
//					 }
//				 }
//				 catch (Exception e) {
//					 e.printStackTrace();
//				 }
//			 }).start();
//		 }
//	 }

    public void initiateElection() {
        logger.accept("[Node " + id + "] Initiating election...");
        inElection = true;
        receivedOk = false;
        receivedCoordinator = false;

        // find the max ID
        OptionalInt optMaxPeer = peerPorts.keySet().stream().mapToInt(Integer::intValue).max();
        if (optMaxPeer.isEmpty() || optMaxPeer.getAsInt() < id) {
            declareLeader();
            return;
        }

        int maxPeerId   = optMaxPeer.getAsInt();
        int maxPeerPort = peerPorts.get(maxPeerId);

        try {
            sendRaw(maxPeerPort, "ELECTION:" + id);
        } catch (IOException ex) {
            handlePeerDown(maxPeerId);
            initiateElection();
            return;
        }

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {}

            if (!receivedOk && !receivedCoordinator) {
                declareLeader();
            }
        }).start();
    }

    public void handleMessage(String msg) {
        if (msg == null) return;
        System.out.println("[Node " + id + "] Received: " + msg);
        logger.accept("[Node " + id + "] Received: " + msg);
        //add an check to peer down
        if (msg.startsWith("PEER_DOWN:")) {
            int downId = parseId(msg);
            if (peerPorts.remove(downId) != null) {
                logger.accept("[Node " + id + "] Synchronized removal of dead peer " + downId);
            }
            return;
        }
        if (msg.startsWith("ELECTION:")) {
            int fromId = parseId(msg);
            if (fromId < id) {
                // Higher ID so send OK
                sendMessage(peerPorts.get(fromId), "OK:" + id);
                // Now start own election if not already in one
                if (!inElection) {
                    inElection = true;
                    initiateElection();
                }
            }
            // Ignore fromId > id
        }
        // Some higher node is alive
        else if (msg.startsWith("OK:")) {
            receivedOk = true;
        }
        else if (msg.startsWith("COORDINATOR:")) {
            int leaderId = parseId(msg);
            System.out.println("[Node " + id + "] Leader = " + leaderId);
            logger.accept("[Node " + id + "] Leader = " + leaderId);
            receivedCoordinator = true;
            inElection = false;
        }
    }
    private void sendRaw(int port, String message) throws IOException {
        try (Socket s = new Socket("localhost", port);
             BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            w.write(message);
            w.newLine();
            w.flush();
        }
    }

    public void declareLeader() {
        System.out.println("[Node "+ id + "] I am the new leader!");
        logger.accept("[Node " + id + "] I am the new leader!");
        // Broadcast COORDINATOR
        for (int peerId : peerPorts.keySet()) {
            sendMessage(peerPorts.get(peerId),"COORDINATOR:" + id);
        }
        inElection = false;
    }

    private void handlePeerDown(int deadId) {
        peerPorts.remove(deadId);
        logger.accept("[Node " + id + "] Peer " + deadId + " down, removed locally.");
        // broadcast removal on raw sockets
        for (int port : new ArrayList<>(peerPorts.values())) {
            try (Socket s = new Socket("localhost", port);
                 BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
                w.write("PEER_DOWN:" + deadId);
                w.newLine();
                w.flush();
            } catch (IOException ignored) {
            }
        }
    }
    public int parseId(String msg) {
        int colonIndex = msg.indexOf(':');
        if (colonIndex == -1) return -1;
        return Integer.parseInt(msg.substring(colonIndex+1));
    }

    public void sendMessage(int targetPort, String message) {
        try (Socket socket = new Socket("localhost", targetPort);
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream()))) {
            writer.write(message);
            writer.newLine();
            writer.flush();

        }
        catch (ConnectException ce) {
            // can not receive
            Integer deadPeer = peerPorts.entrySet().stream()
                    .filter(e -> e.getValue() == targetPort)
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);
            if (deadPeer != null) {
                peerPorts.remove(deadPeer);
                logger.accept("[Node " + id + "] Peer " + deadPeer + " down, removed locally.");
                // broadcast to other node
                peerPorts.values().forEach(port ->
                        sendMessage(port, "PEER_DOWN:" + deadPeer)
                );
            }
        }
        catch (IOException e) {
            logger.accept("[Node " + id + "]  error sending to port " + targetPort + ": " + e.getMessage());
            System.out.println("[Node " + id + "] Could not connect/send to port " + targetPort);
            e.printStackTrace();
        }

    }
}
