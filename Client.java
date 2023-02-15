import java.net.*;
import java.io.*;

public class Client {

    //Intialize socket and input,output streams

    private Socket            clientSocket = null;
    private DataInputStream   input        = null;
    private DataOutputStream  output       = null;
    private BufferedReader    stdIn        = null;

    public Client(String address,int port){
        try{
            clientSocket = new Socket(address,port);
            System.out.println("Connected");
            input  = new DataInputStream(clientSocket.getInputStream());   //takes input from socket
            output = new DataOutputStream(clientSocket.getOutputStream()); //sends output to socket
            stdIn  = new BufferedReader(new InputStreamReader(System.in)); //takes input from terminal

        }
        catch (UnknownHostException u) {
            System.out.println(u);
            return;
        }
        catch (IOException i) {
            System.out.println(i);
            return;
        }
        try{
            //string to read the message from stdIn
            String userInput = "";

            while((userInput = stdIn.readLine())!=null){
                output.writeUTF(userInput);
                System.out.println("echo: " + input.readUTF());
            }
        }
        catch (IOException i) {
            System.out.println(i);
            return;
        }

        //close the connection
        try {
            input.close();
            output.close();
            clientSocket.close();
        }
        catch (IOException i) {
            System.out.println(i);
        }
    }
    public static void main(String[] args) throws IOException {
        Client client = new Client("127.0.0.1", 5000);
    }
}
