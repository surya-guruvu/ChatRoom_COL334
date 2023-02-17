import java.net.*;
import java.io.*;

public class Client {

    //Intialize socket and input,output streams

    private Socket            clientSendSocket  = null;
    private Socket            clientRecvSocket  = null;
    private DataInputStream   input_send        = null;
    private DataOutputStream  output_send       = null;
    private DataInputStream   input_recv        = null;
    private DataOutputStream  output_recv       = null;
    private BufferedReader    stdIn             = null;


    public Client(String address,int sendPort,int recvPort){
        try{
            stdIn  = new BufferedReader(new InputStreamReader(System.in)); //takes input from terminal
                
            System.out.println("Enter username:\n");

            String username = "";
            username = stdIn.readLine();

            System.out.println("Registering to send\n");

            clientSendSocket = new Socket(address,sendPort);

            input_send  = new DataInputStream(clientSendSocket.getInputStream());   //takes input from socket
            output_send = new DataOutputStream(clientSendSocket.getOutputStream()); //sends output to socket

            output_send.writeUTF(username);


            System.out.println("Registered to send\n");

            System.out.println("Registering to recv\n");

            clientRecvSocket = new Socket(address,recvPort);

            input_recv  = new DataInputStream(clientRecvSocket.getInputStream());
            output_recv = new DataOutputStream(clientRecvSocket.getOutputStream());

            output_recv.writeUTF(username);

            System.out.println("Registered to recv\n");

            Thread serverThreadSend = new Thread(new ServerHandler(clientSendSocket,stdIn,0,input_send,output_send));
            Thread serverThreadRecv = new Thread(new ServerHandler(clientRecvSocket,stdIn,1,input_recv,output_recv));

            serverThreadSend.start();
            serverThreadRecv.start();

        }
        catch (UnknownHostException u) {
            System.out.println(u);
            return;
        }
        catch (IOException i) {
            System.out.println(i);
            return;
        }

    }

    private static class ServerHandler implements Runnable {
        private Socket            clientSocket  = null;
        private DataInputStream   input         = null;
        private DataOutputStream  output        = null;
        private BufferedReader    stdIn         = null;
        int op                                  = 1; //0 for send, 1 for recv
        String arr[]                            = {"send","recv"};

        public ServerHandler(Socket socket,BufferedReader stdIn,int op,DataInputStream input,DataOutputStream output){
            this.clientSocket = socket;
            this.stdIn        = stdIn; 
            this.op           = op;  
            this.input        = input;
            this.output       = output;
        }
        private void sendSocketHandler(){
            
            //string to read the message from stdIn
            String userInput = "";
            try{
                while(true){
                    while((userInput = stdIn.readLine())!=null){
                        output.writeUTF(userInput);
                    }
                }

            }
            catch (IOException i) {
                System.out.println(i);
                return;
            }

            
        }
        
        private void recvSocketHandler(){

            

        }
        
        public void run(){
            try{
                if(op==1){
                    try{
                        String inputLine;
                        while(true){
                            while ((inputLine = input.readUTF()) != null) {
                                System.out.println(inputLine);
                            }
                        }


                    }

                    catch (IOException i) {
                        System.out.println(i);
                        return;
                    }
                    
                }
                else{
                    while(true){
                        sendSocketHandler();
                    }
                }
                
                    
                
            }

            finally {
                try {
                    input.close();
                    output.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
    public static void main(String[] args) throws IOException {
        Client client = new Client("127.0.0.1", 5000,8000);
    }
}
