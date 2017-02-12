package Client;

import Protocol.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;


public class Client {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
        //Create important variables to be sure that they exist
        boolean ipAddressCorrect = false;
        String calculation;
        Answer answer = null;
        Socket s = null;
        //Repeat is so lon until the IP Address is correct or the user presses cancel
        while(!ipAddressCorrect) {
            try {
                String serverAddress = JOptionPane.showInputDialog("IP Adresse des Servers auf Port 9090");
                if (serverAddress == null)
                    //If the user clicks cancel close Client
                    System.exit(0);
                //Connect to the server that is running on Port 9090
                s = new Socket(serverAddress, 9090);
                ipAddressCorrect = true;
            } catch (UnknownHostException e) {
                JOptionPane.showMessageDialog(null,
                        "Die IP Adresse ist ungültig",
                        "Error-Client",
                        JOptionPane.ERROR_MESSAGE);
            } catch (ConnectException e) {
                JOptionPane.showMessageDialog(null,
                        "Die Verbindung kann nicht aufgebaut werden (Ist der Server gestartet?)",
                        "Error-Client",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        //Create the ObjectOutputStream and ObjectInputStream
        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
        Request r = new Request();
        //Set username and password and check if the user is allowed to use the server
        do{
            r.setUsername(JOptionPane.showInputDialog("Benutzername eingeben"));
            if(r.getUsername() == null){
                //If the user clicks cancel close Client
                closeSockets(s, in, out);
                System.exit(0);
            }
            r.setPassword(JOptionPane.showInputDialog("Passwort eingeben"));
            if(r.getPassword() == null){
                //If the user clicks cancel close Client
                closeSockets(s, in, out);
                System.exit(0);
            }
            out.writeObject(r);
            answer = (Answer)in.readObject();
            //Print the error information if one exist and then redo the while loop
            if(answer.getStatus().equals("error")) {
                JOptionPane.showMessageDialog(null,
                        answer.getMessage(),
                        "Error-Client",
                        JOptionPane.ERROR_MESSAGE);
            }
        } while(answer.getStatus().equals("error"));
        //Read the calculation in and handle the answer
        do{
        	//Send a request to the server, which contains the calculation
            calculation = JOptionPane.showInputDialog("Rechnung eingeben [kill server = Server schließen]");
            if(calculation == null){
                //If the user clicks cancel close Client
                closeSockets(s, in, out);
                System.exit(0);
            }
            r.setMessage(calculation);
            out.writeObject(r);
            out.flush();
            try {
				answer = (Answer)in.readObject();
				//Print the error information if one exist and then redo the while loop
				if(answer.getStatus().equals("error")) {
                    JOptionPane.showMessageDialog(null,
                            answer.getMessage(),
                            "Error-Client",
                            JOptionPane.ERROR_MESSAGE);
                }
			} catch (ClassNotFoundException e) {
				JOptionPane.showMessageDialog(null,
					    "Kommunikation zwischen Server und Client unterbrochen, bitte Verbindung überprüfen",
					    "Error-Client",
					    JOptionPane.ERROR_MESSAGE);
			}
        }while(answer.getStatus().equals("error"));
        JOptionPane.showMessageDialog(null, answer.getMessage());
        closeSockets(s, in, out);
        System.exit(0);
    }

    private static void closeSockets(Socket s, ObjectInputStream in, ObjectOutputStream out){
        try {
            out.close();
            s.close();
            in.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Verbindung abgebrochen, Client wird beendet",
                    "Error-Client",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
