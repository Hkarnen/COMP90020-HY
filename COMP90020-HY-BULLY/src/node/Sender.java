package node;
@FunctionalInterface
public interface Sender {
	void sendMessage(int port, String msg);
}
