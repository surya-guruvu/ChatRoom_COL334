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
    private static String     username          = null;

    public static ServerHandler sendObj = null;
    public static ServerHandler recvObj = null;


    public Client(String address,int sendPort,int recvPort){
        try{
            stdIn  = new BufferedReader(new InputStreamReader(System.in)); //takes input from terminal

            //Registration starts
                
            System.out.print("Enter username:");

            //String username = "";
            username = stdIn.readLine();

            clientSendSocket = new Socket(address,sendPort);

            input_send  = new DataInputStream(clientSendSocket.getInputStream());   //takes input from socket
            output_send = new DataOutputStream(clientSendSocket.getOutputStream()); //sends output to socket

            output_send.writeUTF(username);

            String res_send = input_send.readUTF();

            if(res_send.equals("NAK")){
                System.out.println("Username might be invalid or has been already used\n");
                input_send.close();
                output_send.close();
                clientSendSocket.close();
            }

            clientRecvSocket = new Socket(address,recvPort);

            input_recv  = new DataInputStream(clientRecvSocket.getInputStream());
            output_recv = new DataOutputStream(clientRecvSocket.getOutputStream());

            output_recv.writeUTF(username);

            String res_recv = input_recv.readUTF();

            if(res_recv.equals("NAK")){
                System.out.println("Username might be invalid or has been already used\n");
                input_recv.close();
                output_recv.close();
                clientRecvSocket.close();
                return;
            }

            //Registration ends

            System.out.println("Welcome to Chat room\n");

            sendObj = new ServerHandler(clientSendSocket,stdIn,0,input_send,output_send);
            recvObj = new ServerHandler(clientRecvSocket,stdIn,1,input_recv,output_recv);

            Thread serverThreadSend = new Thread(sendObj);
            Thread serverThreadRecv = new Thread(recvObj);

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
        private void messageSendParser(String userInput){
            String rec_name = "";
            String msg      = "";

            int sz = userInput.length();

            int i = 1;
            for(;i<sz;i++){
                if(userInput.charAt(i)==' '){
                    break;
                }
                char a = userInput.charAt(i);
                rec_name+=a;
            }
            i++;

            for(;i<sz;i++){
                char a = userInput.charAt(i);
                msg+=a;
            }

            // Constructing the header section with the Content-length field and msg
            String header = "SEND " + rec_name + "\n";
            header += "Content-length: " + msg.length() + "\n";
            String fullMessage = header + "\n" + msg;

            try{
                output.writeUTF(fullMessage);
            }catch (IOException e) {
                System.out.println(e);
                return;
            }
        }
        private void messageRecvParser(String serverMsg){
            String[] parts = serverMsg.split("\n\n");
            String header = parts[0];
            String[] lines = header.split("\n");
            String type = lines[0].substring(0,4);

            if(type.equals("SENT")){
                System.out.println("SENT to " + lines[0].substring(5) + "\n");
            }
            else if(type.equals("ERRO")){
                System.out.println(lines[0]);
            }
            else if(type.equals("FORW")){
                String sender = lines[0].substring(8);
                int contentLength = Integer.parseInt(lines[1].substring(16));
                String data = "";

                if(contentLength!=0){
                    data = parts[1];
                }
                try{
                    if(contentLength==0 || data.length()!=contentLength){
                        String err = "ERROR 103 Header incomplete\n" + "\n";
                        sendObj.output.writeUTF(err);
                        return;
                    }
                    else{
                        System.out.println(sender+" : "+data);
                        String se = "RECEIVED "+sender+"\n\n";
                        sendObj.output.writeUTF(se);
                    }
                }
                catch (IOException e) {
                    System.out.println(e);
                    return;
                }
            }
            else{
                if(lines[0].equals("java.net.SocketException: Connection reset by peer"))
                System.out.println(lines[1] + " has left");
            }
        }
        private void sendSocketHandler(){
            
            //string to read the message from stdIn
            String userInput = "";
            try{
                while((userInput = stdIn.readLine())!=null){
                    messageSendParser(userInput);
                }
                

            }
            catch (IOException i) {
                System.out.println(i);
                return;
            }

            
        }
        
        private void recvSocketHandler(){
            try{
                String inputLine;
                while ((inputLine = input.readUTF()) != null) {
                    messageRecvParser(inputLine);
                    
                }
            }
            catch (IOException i) {
                System.out.println(i);
                return;
            }

        }
        
        public void run(){
            try{
                // on recv thread only recv works. on send thread only send works
                // To avoid concurrency issues
                if(op==1){
                    recvSocketHandler();
                }
                else{
                    sendSocketHandler();   
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
