package Server;

import Protocol.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JOptionPane;

public class Server {

	//Close methods
	private static void closeServer(ServerSocket serverSocket, Socket client, ObjectInputStream in, ObjectOutputStream out) throws IOException {
		out.writeObject(new Answer("warning", "Server wird geschlossen"));
		closeClient(client, in, out);
		serverSocket.close();
		System.out.println("\tDer Client hat den Server geschlossen");
		System.exit(0);
	}

	private static void closeClient(Socket client, ObjectInputStream in, ObjectOutputStream out) throws IOException {
		in.close();
		out.close();
		client.close();
	}

	private static void handleConnection(ServerSocket serverSocket, Socket client, ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
		Request calculation = (Request) in.readObject();
		//Check if the received data is valid and calculate the result
		//Checks if the password and the username is right
		if (calculation != null) {
			if (calculation.getMessage().equals("exit")) {
				//Only close the server
				out.writeObject(new Answer("warning", "Der Cient wird nun beendet"));
				System.out.println("\tDer Client hat sich getrennt");
				closeClient(client, in, out);
			} else if (calculation.getMessage().equals("kill server")) {
				//Close the server and the client
				closeServer(serverSocket, client, in, out);
			} else {
				//The ScriptEngineManager cuts the string and calculates the result on its own
				ScriptEngineManager mgr = new ScriptEngineManager();
				ScriptEngine engine = mgr.getEngineByName("JavaScript");
				try {
					out.writeObject(new Answer("ok", engine.eval(calculation.getMessage()).toString()));
					System.out.println("\tBerechnung erfolgreich durchgeführt. Client wird geschlossen");
				} catch (ScriptException e) {
					JOptionPane.showMessageDialog(null,
							"Die Eingabe des Clients konnte nicht verarbeitet werden!",
							"Error-Server",
							JOptionPane.ERROR_MESSAGE);
					out.writeObject(new Answer("error", "Ungültige Eingabe, bitte wiederholen!"));
					System.out.println("\tBerechnung nicht erfolgreich durchgeführt. Client wird Daten erneut senden");
					handleConnection(serverSocket, client, out, in);
				}
			}

		}
	}

	private static void login(ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
		Request calculation = (Request) in.readObject();
		if(!calculation.getUsername().equals("Admin") && !calculation.getPassword().equals("ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f")){
				//Inform the client, that the username or the password is wrong
				out.writeObject(new Answer("error", "Authentifizierungsdaten sind nicht korrekt!"));
				System.out.println("\tClient verfügt nicht über die nötigen Berechtigungen");
				login(out, in);
		}else{
			out.writeObject(new Answer("ok", "Authentifizierungsdaten sind  korrekt!"));
			System.out.println("\tClient verfügt über die nötigen Berechtigungen");
		}
	}


	public static void main(String[] args) {
		//Create important variables to be sure that they exist
		ServerSocket serverSocket = null;
		Socket client;
		ObjectOutputStream out;
		ObjectInputStream in;
		try {
			//Create a new Server Socket ont the default 9090 Port
			serverSocket = new ServerSocket(9090);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,
					"Der Port ist bereits besetzt!",
					"Error-Server",
					JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		//Wait for a client (Only one can be handled)
		//When everything was successful and the client did not close the server, then the server waits for another client
		while (true) {
			try {
				System.out.println("Warte auf Client");
				client = serverSocket.accept();
				out = new ObjectOutputStream(client.getOutputStream());
				in = new ObjectInputStream(client.getInputStream());
				System.out.println("Client angenommen:");
				login(out, in);
				handleConnection(serverSocket, client, out, in);
				closeClient(client, in, out);
			} catch (IOException e) {
				System.out.println("\tDer Client hat die Verbindung verloren bzw. abgebrochen");
			} catch (ClassNotFoundException e) {
				JOptionPane.showMessageDialog(null,
						"Klasse nicht gefunden!",
						"Error-Server",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
