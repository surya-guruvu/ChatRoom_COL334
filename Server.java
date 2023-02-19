import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Server{
    private Socket          sendSocket   = null;
    private Socket          recvSocket   = null;
    private ServerSocket    serverSend   = null;
    private ServerSocket    serverRecv   = null;
    private BufferedReader  stdIn        = null;

    public static HashMap<String,ClientHandler> mapSend = null;
    public static HashMap<String,ClientHandler> mapRecv = null;

    public Server(int sendPort,int recvPort){
        try{
            //server starts
            serverSend = new ServerSocket(sendPort);
            serverRecv = new ServerSocket(recvPort);

            mapSend = new HashMap<String,ClientHandler>();
            mapRecv = new HashMap<String,ClientHandler>();

            System.out.println("Server Started");

            stdIn  = new BufferedReader(new InputStreamReader(System.in));

            while(true){
                //Registration starts
                sendSocket = serverSend.accept();

                ClientHandler ClientHandlerSend = new ClientHandler(sendSocket,0,stdIn,mapSend);

                Thread clientThreadSend = new Thread(ClientHandlerSend);

                clientThreadSend.start();

                recvSocket = serverRecv.accept();

                ClientHandler ClientHandlerRecv = new ClientHandler(recvSocket,1,stdIn,mapRecv);

                Thread clientThreadRecv = new Thread(ClientHandlerRecv);

                clientThreadRecv.start();
                //Registration ends
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
        private HashMap<String,ClientHandler> mp     = null; 
        private String username            = null;

        public ClientHandler(Socket socket,int op,BufferedReader stdIn,HashMap<String,ClientHandler> mp) {
            this.clientSocket = socket;
            this.op           = op;
            this.stdIn        = stdIn;
            this.mp           = mp;
        }

        public boolean isUsernameWellFormed(String username) {
            String pattern = "^[a-zA-Z0-9]+$";
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(username);
            return matcher.matches();
        }

        private void messageParser(String clientMsg){
            String[] parts = clientMsg.split("\n\n");
            String header = parts[0];
            String[] lines = header.split("\n");
            String type = lines[0].substring(0,4);

            //Msg to be forwared to the specified client
            if(type.equals("SEND")){
                String rec_name = lines[0].substring(5);
                int contentLength = Integer.parseInt(lines[1].substring(16));

                String data = "";

                if(contentLength!=0){
                    data = parts[1];
                }

                ClientHandler cur  = mapRecv.get(username); //current user recv socket
                ClientHandler temp = mapRecv.get(rec_name); //recipient user recv socket

                String head = "FORWARD " + username + "\n";
                head += "Content-length: " + contentLength + "\n";
                String fullMessage = head + "\n" + data;

                try{
                    if(!mapRecv.containsKey(rec_name)){
                        String err = "ERROR 102 Unable to send\n" + "\n";
                        cur.out.writeUTF(err);
                        return;
                    }
                    else if(contentLength==0 || data.length()!=contentLength){
                        String err = "ERROR 103 Header incomplete\n" + "\n";
                        cur.out.writeUTF(err);
                        return;
                    }
                    else{
                        temp.out.writeUTF(fullMessage);
                    }
                }
                catch (IOException e) {
                    try{
                        System.out.println(e);
                        cur.out.writeUTF(e + "\n" + rec_name +"\n\n");
                    }
                    catch (IOException a) {
                        System.out.println(a);
                        return;
                    }
                    return;
                }
            }

            //If msg is received by the client, then SENT msg is to be sent to the sender

            if(type.equals("RECE")){
                String rec_name = lines[0].substring(9);
                ClientHandler cur  = mapRecv.get(username); //current user recv socket
                ClientHandler temp = mapRecv.get(rec_name); //recipient user recv socket
                String fullMessage = "SENT " + username + "\n\n";

                try{
                    temp.out.writeUTF(fullMessage);
                }
                catch (IOException e) {
                    System.out.println(e);
                    return;
                }
            }

            if(type.equals("ERRO")){
                System.out.println(lines[0]);
            }

        }


        public void run() {
            try {
                in  = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());

                username = in.readUTF();

                //if username is not well formed or username exists
                if((!isUsernameWellFormed(username)) || mp.containsKey(username)){
                    out.writeUTF("NAK");
                    in.close();
                    out.close();
                    clientSocket.close();                    
                }

                else{
                    out.writeUTF("ACK");
                    mp.put(username,this);
                }

                if(op==0){
                    String inputLine;        
                    while ((inputLine = in.readUTF()) != null) {
                        messageParser(inputLine);
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

            } 
            catch (IOException e) {
                e.printStackTrace();
            } 
            finally {
                try {
                    in.close();
                    out.close();
                    clientSocket.close();
                } 
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) throws IOException{
        Server server = new Server(5000,8000);
    }
}
