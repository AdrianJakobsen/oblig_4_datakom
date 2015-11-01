package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.io.*;
import javax.net.ssl.*;

import Models.ClientUser;
import Models.ClientUserContainer;
import Models.Message;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ChatServer extends Application{
    private TextArea textArea = new TextArea();
    private int clientNumber = 0;
    private ArrayList<ClientUserContainer> clientOutputStreams;
    private SSLServerSocket sslServerSocket;
    private int sslPortnumber = 4430;


    @Override
    public void start(Stage primaryStage) throws Exception {

        clientOutputStreams = new ArrayList<ClientUserContainer>();
        Scene scene = new Scene(new ScrollPane(textArea), 600, 220);
        textArea.setEditable(false);
        primaryStage.setTitle("Chat Server");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();


        new Thread( () -> {
            try {
                SSLServerSocketFactory sslSrvFact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                sslServerSocket = (SSLServerSocket) sslSrvFact.createServerSocket(sslPortnumber);
                Platform.runLater(() -> {
                    textArea.appendText("Chat Server started at " + new Date() + '\n');
                });

                while (true) {
                    SSLSocket clientSslSocket = (SSLSocket) sslServerSocket.accept();
                    clientNumber++;

                    Platform.runLater(() -> {

                        //	textArea.appendText("client number " + clientNumber+ " have started a thread at " + new Date() + '\n');

                        InetAddress inetAddres = clientSslSocket.getInetAddress();
                        textArea.appendText("Client " + clientNumber + "'s host name is " + inetAddres.getHostName() + "\n");
                        //	textArea.appendText("Client "+clientNumber+"'s IP address is " + inetAddres.getHostAddress()+ "\n");
                    });

                    new Thread(new HandleAClient(clientSslSocket)).start();
                }
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    class HandleAClient implements Runnable{
        private SSLSocket sslSocket;
        private InputStream input;
        private ObjectInputStream inputFromClient;
        private OutputStream output;
        private ObjectOutputStream outputToClient;
        private ClientUserContainer client;

        public HandleAClient(SSLSocket sslSocket){
            this.sslSocket = sslSocket;
        }
        @Override
        public void run() {
            try{
                input = sslSocket.getInputStream();
                inputFromClient = new ObjectInputStream(input);
                output = sslSocket.getOutputStream();
                outputToClient = new ObjectOutputStream(output);
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                String disconnectMessage = "thisClientWillDisconnectFromTheServerNow99118822";
                String whisper = "[/w]";

                Message userClient =(Message) inputFromClient.readObject();
                client = new ClientUserContainer(output, outputToClient, userClient.getClientUser());
                clientOutputStreams.add(client);
                connectionMessageToAllClients(userClient);

                while(true){
                    uptodateUserNameList();
                    Message clientMessage =(Message) inputFromClient.readObject();
                    String timestamp = dateFormat.format(clientMessage.getMessageSentAt());

                    if(clientMessage.getMessage().startsWith(disconnectMessage)){
                        disconnect(clientMessage, timestamp);


                    }else if(clientMessage.getMessage().startsWith(whisper)){
                        whisperMessageToSpecificClient(clientMessage,timestamp, whisper);
                    }else{
                        sendMessageToClients(clientMessage,  timestamp);
                    }
                }
            }catch(IOException ex){
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                String timestamp = dateFormat.format(new Date());
                String disconnectMessage = "thisClientWillDisconnectFromTheServerNow99118822";
                Message removeFromListMessage = new Message(client.getClientUser(), new Date(), disconnectMessage);
                disconnect(removeFromListMessage, timestamp);
                clientNumber--;
            }catch(Exception ex){
                System.out.println(ex.getMessage());
                //ex.printStackTrace();
            }
        }
        private void removeUserFromList(ClientUserContainer client,String timestamp) {
            String disconnectMessage = "thisClientWillDisconnectFromTheServerNow99118822";
            Platform.runLater(() ->{
                textArea.appendText(client.getClientUser().getUserName() + " have disconnected from the Server" + '\n');
            });
            Message removeFromListMessage = new Message(client.getClientUser(), new Date(), disconnectMessage);
            for(ClientUserContainer clientOutputStream : clientOutputStreams){
                try{
                    clientOutputStream.getoutputToClient().writeObject(removeFromListMessage);
                    clientOutputStream.getoutputToClient().flush();
                }catch(Exception ex){
                    System.out.println(ex.getMessage());
                }
            }

        }
        private void whisperMessageToSpecificClient(Message whisperClientMessage, String timestamp, String whisper){
            String senderOfWhisper = whisperClientMessage.getClientUser().getUserName();
            for (ClientUserContainer name : clientOutputStreams) {
                if (whisperClientMessage.getMessage().contains(whisper+" ("+name.getClientUser().getUserName()+")")) {
                    String whisperFilteredMessage ="\"whisper\"" + whisperClientMessage.getMessage().substring(whisper.length()+name.getClientUser().getUserName().length()+3);
                    Message newMessage = new Message(whisperClientMessage.getClientUser(), new Date(), whisperFilteredMessage);
                    try{
                        name.getoutputToClient().writeObject(newMessage);
                        String whisperFilteredMessageBackToSelf ="\"w/("+name.getClientUser().getUserName()+")\" "
                                + whisperClientMessage.getMessage().substring(whisper.length()+name.getClientUser().getUserName().length()+3);
                        Message newMessage2 = new Message(whisperClientMessage.getClientUser(), new Date(), whisperFilteredMessageBackToSelf);
                        for (ClientUserContainer nameAgain : clientOutputStreams) {
                            if(nameAgain.getClientUser().getUserName()==senderOfWhisper){
                                nameAgain.getoutputToClient().writeObject(newMessage2);
                            }
                        }
                    }catch(Exception ex){
                        System.out.println(ex.getMessage()+ " from whisper specific message, Server");
                    }
                }
            }
        }
    }
    private void uptodateUserNameList() {
        for(ClientUserContainer clientOutputStream : clientOutputStreams){
            for(ClientUserContainer clientUserNames : clientOutputStreams){
                try{
                    clientOutputStream.getoutputToClient().writeObject(clientUserNames.getClientUser());
                    clientOutputStream.getoutputToClient().flush();
                }catch(Exception ex){
                    System.out.println(ex.getMessage());
                }
            }
        }

    }
    private void sendMessageToClients(Message clientMessage, String timestamp){
        try{
            Platform.runLater(() ->{
                textArea.appendText(timestamp + ' ' + clientMessage.getClientUser().getUserName()  + ": " + clientMessage.getMessage() + '\n');
            });
            for (ClientUserContainer clientOutputStream : clientOutputStreams) {


                try {
                    clientOutputStream.getoutputToClient().writeObject(clientMessage);
                    clientOutputStream.getoutputToClient().flush();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    ex.printStackTrace();
                }

            }
        }finally{
        }
    }


    private void disconnect(Message clientMessage, String timestamp){
        Message disconnectRespons = new Message(clientMessage.getClientUser(), clientMessage.getMessageSentAt(), ": have disconnected from the Server");

        try{
            Platform.runLater(() ->{
                textArea.appendText(timestamp + ' ' + clientMessage.getClientUser().getUserName()  + ": have disconnected from the Server");
            });
            for (ClientUserContainer clientOutputStream : clientOutputStreams) {
                try {
                    clientOutputStream.getoutputToClient().writeObject(disconnectRespons);
                    clientOutputStream.getoutputToClient().flush();
                } catch(SocketException ex){
                    System.out.println("A socket has been closed");
                }catch (Exception ex) {
                    System.out.println("");
                    ex.printStackTrace();
                }
            }
        }finally{
            for(int i = 0; i < clientOutputStreams.size(); i++){
                if(clientOutputStreams.get(i).getClientUser()==disconnectRespons.getClientUser()){
                    clientOutputStreams.remove(i);
                }
                clientNumber--;
            }
        }
    }

    private void connectionMessageToAllClients(Message userClient){
        String connectRespons = ": have connected to the Server";
        for (ClientUserContainer clientOutputStream : clientOutputStreams) {
            try {
                clientOutputStream.getoutputToClient().writeObject(new Message(userClient.getClientUser(), userClient.getMessageSentAt(), connectRespons));
                clientOutputStream.getoutputToClient().flush();
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        launch(args);
    }
}