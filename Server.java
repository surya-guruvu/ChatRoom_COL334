import java.net.*;
import java.io.*;
import java.util.*;

public class Server{
    private Socket          sendSocket   = null;
    private Socket          recvSocket   = null;
    private ServerSocket    serverSend   = null;
    private ServerSocket    serverRecv   = null;
    private BufferedReader  stdIn        = null;

    HashMap<String,ClientHandler> mapSend = null;
    HashMap<String,ClientHandler> mapRecv = null;

    public Server(int sendPort,int recvPort){
        try{
            //server starts
            serverSend = new ServerSocket(sendPort);
            serverRecv = new ServerSocket(recvPort);

            mapSend = new HashMap<String,ClientHandler>();
            mapRecv = new HashMap<String,ClientHandler>();

            System.out.println("Server Started");

            System.out.println("Waiting for a Client .....");

            stdIn  = new BufferedReader(new InputStreamReader(System.in));

            while(true){
                sendSocket = serverSend.accept();
                recvSocket = serverRecv.accept();


                ClientHandler ClientHandlerSend = new ClientHandler(sendSocket,0,stdIn);
                ClientHandler ClientHandlerRecv = new ClientHandler(recvSocket,1,stdIn);

                Thread clientThreadSend = new Thread(ClientHandlerSend);
                Thread clientThreadRecv = new Thread(ClientHandlerRecv);

                clientThreadSend.start();
                clientThreadRecv.start();
            }
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }
    private static class ClientHandler implements Runnable {
        private Socket clientSocket        = null;
        private DataInputStream in         = null;
        private DataOutputStream out       = null;
        private BufferedReader stdIn       = null;
        private int op                     = 0;
        private String arr[]               = {"send","recv"};

        public ClientHandler(Socket socket,int op,BufferedReader stdIn) {
            this.clientSocket = socket;
            this.op           = op;
            this.stdIn        = stdIn;
        }

        public void run() {
            try {

                System.out.println("Registering to "+arr[op]);

                in  = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());

                String username = in.readUTF();

                System.out.println("Username: "+username);

                System.out.println("Registered to "+arr[op]);

                if(op==0){
                    String inputLine;
                    while(true){
                        while ((inputLine = in.readUTF()) != null) {
                            System.out.println(inputLine);
                        }
                    }
                }
                else{
                    String outputLine;
                    while(true){
                        while((outputLine = stdIn.readLine())!=null){
                            out.writeUTF(outputLine);
                        }
                    }
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
        Server server = new Server(5000,8000);
    }
}
