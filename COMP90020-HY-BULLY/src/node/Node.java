package node;

import java.io.*;
import java.net.*;
import java.util.*;

public class Node {
	
	
	 private int id;
	 private int port;
	 private Map<Integer, Integer> peerPorts = new HashMap<>();

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
			 scanner.nextLine();
			 sendHelloToPeers();
		 }
	 }
	 
	 private void startServer() {
		 try (ServerSocket serverSocket = new ServerSocket(port)) {
			 while (true) {
				 Socket client = serverSocket.accept();
				 BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
				 String msg = reader.readLine();
				 System.out.println("[Node " + id + "] Received: " + msg);
				 client.close();
			 }
		 }
		 catch (IOException e) {
			 e.printStackTrace();
		 }
	 }
	 
	 private void sendHelloToPeers() {
		 for (int peerId : peerPorts.keySet()) {
			 int peerPort = peerPorts.get(peerId);
			 try (Socket socket = new Socket("localhost", peerPort)) {
				 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				 writer.write("HELLO from Node " + id);
				 writer.newLine();
				 writer.flush();
				 System.out.println("[Node " + id + "] Sent HELLO to Node " + peerId);
			 }
			 catch (IOException e) {
				 e.printStackTrace();
				 System.out.println("[Node " + id + "] Could not connect to Node " + peerId);
			 }
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
