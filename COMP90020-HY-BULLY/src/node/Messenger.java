package node;

import java.io.*;
import java.net.*;

public class Messenger {
  
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
        }
    }
}
