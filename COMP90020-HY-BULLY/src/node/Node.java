package node;

import java.io.*;
import java.net.*;
import java.util.*;

public class Node {
	
	
	 private int id;
	 private int port;
	 private Map<Integer, Integer> peerPorts = new HashMap<>();
	 private boolean receivedOk = false;
	 private boolean receivedCoordinator = false;
	 private boolean inElection = false;

	 public Node(int id, int port, Map<Integer, Integer> peers) {
		 this.id = id;
	     this.port = port;
	     this.peerPorts = peers;
	 }
	 
	 public void start() {
		 new Thread(this::startServer).start();
		 
		 System.out.println("[Node " + id + "] Started on port " + port);
		 System.out.println("Press Enter to say hello to peers");
		 
		 try (Scanner scanner = new Scanner(System.in)) {
			 System.out.println("Commands: HELLO, ELECTION, or QUIT to exit.");
			 
			 while (true) {
				 System.out.println("Node " + id + " > ");
				 String cmd = scanner.nextLine().trim().toUpperCase();
				 if (cmd.equals("HELLO")) {				 
	                 sendHelloToPeers();
	             } 
				 else if (cmd.equals("ELECTION")) {
	                 initiateElection();
	             } 
				 else if (cmd.equals("QUIT")) {
					 System.out.println("[Node " + id + "] Exiting...");
	                 System.exit(0);;  // end the loop
				 }
				 else {
					 System.out.println("Unknown command. Type HELLO, ELECTION, or QUIT.");
				 }
	                 
			 }
		 }
	 }
	 
	 private void startServer() {
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
			 System.err.println("[Node " + id + "] Port " + port + " is already in use!");
			 System.exit(1);
		 }
		 catch (IOException e) {
			 e.printStackTrace();
			 System.exit(1);
		 }
	 }
	 
	 private void sendHelloToPeers() {
		 for (int peerId : peerPorts.keySet()) {
			 int peerPort = peerPorts.get(peerId);
			 sendMessage(peerPort, "HELLO from Node " + id);
		 }
	 }
	 
	 private void initiateElection() {
		 System.out.println("[Node " + id + "] Initiating election...");
		 inElection = true;
		 receivedOk = false;
		 receivedCoordinator = false;
		 
		 boolean higherExists = false;
		 
		 for (int peerId: peerPorts.keySet()) {
			 if (peerId > id) {
				 sendMessage(peerPorts.get(peerId), "ELECTION:" + id);
				 higherExists = true;
			 }
		 }
		 
		 if (!higherExists) {
			 // No higher ID node is alive, declare self leader
			 declareLeader();
		 }
		 else {
			 // Wait for a short time for OK or COORDINATOR messages
			 new Thread(() -> {
				 try {
					 Thread.sleep(3000); // Placeholder timeout T
					 if (!receivedOk || !receivedCoordinator) {
						 // No response from higher node, so self-promote
						 declareLeader();
					 }
				 }
				 catch (Exception e) {
					 e.printStackTrace();
				 }
			 }).start();
		 }
	 }
	 
	 private void handleMessage(String msg) {
		 if (msg == null) return;
		 System.out.println("[Node " + id + "] Received: " + msg);
		 
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
			 receivedCoordinator = true;
		 }
	 }
	 
	 private void declareLeader() {
		 System.out.println("[Node "+ id + "] I am the new leader!");
		 // Broadcast COORDINATOR
		 for (int peerId : peerPorts.keySet()) {
			 sendMessage(peerPorts.get(peerId),"COORDINATOR:" + id);
		 }
		 inElection = false;
	 }
	 
	 private int parseId(String msg) {
		 int colonIndex = msg.indexOf(':');
		 if (colonIndex == -1) return -1;
		 return Integer.parseInt(msg.substring(colonIndex+1));
	 }
	 
	 private void sendMessage(int targetPort, String message) {
		 try (Socket socket = new Socket("localhost", targetPort);
				 BufferedWriter writer = new BufferedWriter(
						 new OutputStreamWriter(socket.getOutputStream()))) {
			 writer.write(message);
			 writer.newLine();
			 writer.flush();
					 
		 }
		 catch (IOException e) {
			 System.out.println("[Node " + id + "] Could not connect/send to port " + targetPort);
			 e.printStackTrace();
		 }
			 
	 }
	 
	 
	 public static void main(String[] args) {
		 if (args.length < 2) {
			 System.out.println(args.length);
			 System.out.println("Usage: java node.Node <id> <port>");
			 return;
		 }
		 
		 if (args.length > 2) {
			 System.out.println(args.length);
			 System.out.println("Usage: java node.Node <id> <port>");
			 return;
		 }
		 
		 int id = Integer.parseInt(args[0]);
		 int port = Integer.parseInt(args[1]);
		 
		 Map<Integer, Integer> peers = new HashMap<>();
		 // For now just assume these 3 processes already know their peers
		 peers.put(1, 5001);
	     peers.put(2, 5002);
	     peers.put(3, 5003);
	     peers.remove(id); // Remove self from peer list

	     Node node = new Node(id, port, peers);
	     node.start();
		 
		 
	 }
	 
	
}

