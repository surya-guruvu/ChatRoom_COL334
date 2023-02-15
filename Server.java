import java.net.*;
import java.io.*;

public class Server{
    private Socket          socket   = null;
    private ServerSocket    server   = null;

    public Server(int port){
        try{
            //server starts
            server = new ServerSocket(port);

            System.out.println("Server Started");

            System.out.println("Waiting for a Client .....");

            while(true){
                socket = server.accept();
                Thread clientThread = new Thread(new ClientHandler(socket));
                clientThread.start();
            }
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private DataInputStream in;
        private DataOutputStream out;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());

                String inputLine, outputLine;
                while ((inputLine = in.readUTF()) != null) {
                    outputLine = "Server: " + inputLine;
                    out.writeUTF(outputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) throws IOException{
        Server server = new Server(5000);
    }
}
