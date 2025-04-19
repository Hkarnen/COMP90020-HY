package node;

import java.util.function.Consumer;

public class MessageHandler {
	
	private final Node node;
	private final ElectionManager electionManager;
	private final ChatManager chatManager;
	private final Consumer<String> logger;
	
	public MessageHandler(Node node, ElectionManager electionManager, ChatManager chatManager, Consumer<String> logger) {
		this.node = node;
		this.electionManager = electionManager;
		this.chatManager = chatManager;
		this.logger = logger;
	}
	
	public void handleMessage(String msg) {
		if (msg == null) return;
		
		System.out.println("[Node " + node.getId() + "] Received: " + msg);
		logger.accept("[Node " + node.getId() + "] Received: " + msg);

        if (msg.startsWith("ELECTION:")) {
            int fromId = parseId(msg);
            electionManager.handleElectionMessage(fromId);
        }
        else if (msg.startsWith("OK:")) {
            electionManager.handleOkMessage();
        }
        else if (msg.startsWith("COORDINATOR:")) {
        	int fromId = parseId(msg);
            electionManager.handleCoordinatorMessage(fromId);
        }
        else if (msg.startsWith("CHAT:")) {
            // Delegate chat message handling
            chatManager.handleIncomingChat(msg);
        }
		else if (msg.startsWith("PEER_DOWN:")) {
			int dead = parseId(msg);
			electionManager.handlePeerDown(dead);
			logger.accept("[Node " + node.getId() + "] Peer " + dead + " down");
		}
	}
	
	private int parseId(String msg) {
		int colonIndex = msg.indexOf(':');
		if (colonIndex == -1) return -1;
		return Integer.parseInt(msg.substring(colonIndex + 1));
	}
	
}
