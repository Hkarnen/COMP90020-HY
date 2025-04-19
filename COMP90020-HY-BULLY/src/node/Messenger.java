package node;

import java.io.*;
import java.net.*;

import java.util.function.Consumer;

public class Messenger {

    private final Consumer<String> logger;
    public Messenger(Consumer<String> logger) { this.logger = logger; }
    // Sends a single-line message to the specified port
    public void sendMessage(int targetPort, String message) {
        try (Socket socket = new Socket("localhost", targetPort);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
             
            writer.write(message);
            writer.newLine();
            writer.flush();
        } 
        catch (IOException e) {
            System.out.println("[Messenger] Could not send to port " + targetPort);
            logger.accept("[Messenger] Failed to " + message + " -> port " + targetPort);
        }
    }
    public void sendRaw(int targetPort, String message) throws IOException {
        try (Socket s = new Socket("localhost", targetPort);
             BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            w.write(message); w.newLine(); w.flush();
        }
    }
}
