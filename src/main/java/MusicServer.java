import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A server that executes the Simple Bank Access Protocol.
 */
public class MusicServer {
	public static void main(String[] args) throws IOException {

		final int SBAP_PORT = 8889;
		ServerSocket server = new ServerSocket(SBAP_PORT);
		try {
			System.out.println("Waiting for clients to connect...");

			while (true) {
				Socket s = server.accept();
				System.out.println("Client connected.");
				DatabaseService service = new DatabaseService(s);
				Thread t = new Thread(service);
				t.start();
			}
		}
		finally {
			if(!server.isClosed())
				server.close();
		}
	}
}
