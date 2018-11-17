import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

/**
 * ChatClient doesn't correctly construct itself using parameters
 * ChatClient & Server only print one ChatMessage; they remain open after, but don't print anything
 *
 */


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String username, String port, String server) {
        this.server = server;
        this.port = Integer.parseInt(port);
        this.username = username;
    }

    private ChatClient(String username, String port) {
        this.server = "localhost";
        this.port = Integer.parseInt(port);
        this.username = username;
    }

    private ChatClient(String username) {
        this.server = "localhost";
        this.port = 1500;
        this.username = username;
    }

    private ChatClient() {
        this.server = "localhost";
        this.port = 1500;
        this.username = "Anonymous";
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ChatClient.ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        ChatClient client;
        if (args.length == 1) {
            client = new ChatClient(args[0]);
            client.start();
        }
        if (args.length == 2) {
            client = new ChatClient(args[0], args[1]);
            client.start();
        }
        if (args.length == 3) {
            client = new ChatClient(args[0], args[1], args[2]);
            client.start();
        } else {
            client = new ChatClient();
            client.start();
        }
        try { //reads user input, converts to String, checks for '/logout', and sends message
            Scanner s = new Scanner(System.in);
            while (true) {
                String message = s.nextLine();
                int type;
                if (!message.equalsIgnoreCase("/logout")) {
                    type = 0;
                    client.sendMessage(new ChatMessage(type, message)); //if message is general, send
                } else {
                    type = 1;
                    client.sendMessage(new ChatMessage(type, message)); //if message is logout, close everything
                    client.sInput.close();
                    client.sOutput.close();
                    client.socket.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */

    private final class ListenFromServer implements Runnable {
        public void run() {
            try {
                while (true) {
                    String format = "HH:mm:ss";
                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    String alex = sdf.toString();
                    String msg = sInput.readObject().toString();
                    System.out.println(alex + " " + username + ":" + msg);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

