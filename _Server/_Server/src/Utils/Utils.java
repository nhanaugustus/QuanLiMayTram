package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    
    public static String getSocketHostname(Socket s) {
        return s.getInetAddress().getHostName();
    }
    
    public static String getSocketIP(Socket s) {
        return s.getInetAddress().getHostAddress();
    }

    public static String receiveMess(Socket sender) {
        try {
            BufferedReader inputServer = new BufferedReader(new InputStreamReader(sender.getInputStream()));
            String msg = inputServer.readLine();
            System.out.println("Receive "+getSocketIP(sender)+":"+sender.getPort()+" > "+ msg);
            return msg;
        } catch (IOException ex) {
            return "";
        }
    }

    public static boolean sendMess(Socket receiver, String msg) {
        PrintWriter outputToClient;
        try {
            outputToClient = new PrintWriter(receiver.getOutputStream(), true);
            outputToClient.println(msg);
            System.out.println("Send to "+getSocketIP(receiver)+":"+receiver.getPort()+" > "+msg);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    public static Object receiveObject(Socket sender) {
        try {
            ObjectInputStream ois = new ObjectInputStream(sender.getInputStream());
            return ois.readObject();
        } catch (ClassNotFoundException | IOException ex) {
        }
        return "";
    }

    public static boolean sendObject(Socket receiver, Object msg) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(receiver.getOutputStream());
            oos.writeObject(msg);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public static boolean isDisconnected(Socket socket){
        PrintWriter out;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            out.print(" ");
            return out.checkError();
        } catch (IOException ex) {
        }
        return true;
    }
    
    public static String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        return dateFormat.format(date);
    }
}
