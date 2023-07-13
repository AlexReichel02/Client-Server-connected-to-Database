import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Executes Simple Bank Access Protocol commands from a socket.
 */
public class DatabaseService implements Runnable {

	private Socket s;
	private Scanner in;
	private PrintWriter out;
	private ReentrantLock lock;

	/**
	 * Constructs a service object that processes commands from a socket for a bank.
	 * 
	 * @param aSocket the socket
	 */
	public DatabaseService(Socket aSocket) {
		s = aSocket;
		lock = new ReentrantLock();
	}

	public void run() {
		try {
			try {
				in = new Scanner(s.getInputStream());
				out = new PrintWriter(s.getOutputStream());
				doService();
			} finally {
				s.close();
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Executes all commands until the QUIT command or the end of input.
	 */
	public void doService() throws IOException {
		String command = "";
		do {
			if (!in.hasNext())
				return;
			command = in.nextLine();
		} while (!executeCommand(command));

	}

	/**
	 * Executes a single command.
	 * 
	 * @param command the command to execute
	 */
	public boolean executeCommand(String command) {
		String results = "";
		System.out.println(command);
		String[] splited = command.split(" ");
		DatabaseManager db = null;
		try {
			db = new DatabaseManager();
		} catch (SQLException e) {
			System.err.println(e.getLocalizedMessage());
		}

		lock.lock();
		results = db.queryDatabase(splited[0], splited[1], splited[2], splited[3]);
		lock.unlock();

		out.println(results);
		out.flush();
		if (results != "") {
			return true;
		}

		return false;
	}

}
