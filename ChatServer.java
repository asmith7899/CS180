import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
final class ChatServer {
    private static int uniqueId = 0;
    private final List<ChatServer.ClientThread> clients = new ArrayList<>();
    private final int port;


    private ChatServer(int port) {
        this.port = port;
    }

    private ChatServer() {
        this.port = 1500;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                Runnable r = new ChatServer.ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ChatServer.ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            ChatServer server = new ChatServer();
            server.start();
        }
        if (args.length == 1) {
            ChatServer server = new ChatServer(Integer.parseInt(args[0]));
            server.start();
        }
    }


    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            try {
                cm = (ChatMessage) sInput.readObject();
                if (cm.getType() == 0) {
                    broadcast(this.username + ": " + cm.getMessage());
                } else {
                    broadcast(this.username + "has logged out.");
                    remove(this.id);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }


            // Send message back to the client
            try {
                sOutput.writeObject(cm.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private synchronized void broadcast(String message) { //prints message to terminal of every user, plus server terminal
            for (ClientThread client : clients) {
                client.writeMessage(message);
            }
            System.out.println(message);
        }

        public boolean writeMessage(String message) { //writes message to user's terminal
            if (socket.isConnected()) {
//                try {
//                  //  this.sOutput.writeObject(message);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                return true;
            }
            return false;
        }




        public synchronized void remove(int id) { //removes clients from arraylist
            clients.remove(this.id);
        }

        private void close() {
            for (ClientThread cl : clients) {
                try {
                    cl.sInput.close();
                    cl.sOutput.close();
                    cl.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
