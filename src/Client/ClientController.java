package Client;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.ResourceBundle;

import java.io.*;
import javax.net.ssl.*;

import Models.ClientUser;
import Models.Message;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;


public class ClientController implements Initializable{

    @FXML
    Button sendButton;
    @FXML
    TextArea chatView;
    @FXML
    TextField messageField;
    @FXML
    TextField UserName;
    @FXML
    TextField HostPort;
    @FXML
    TextField HostAddress;
    @FXML
    Button ConnectButton;
    @FXML
    Button DisconnectButton;
    @FXML
    ListView<String> listView = new ListView<String>();

    private SSLSocket socket;
    private ClientUser clientUser;
    private String disconnectMessage;
    ObservableList<String> names = FXCollections.observableArrayList();
    ObjectOutputStream messageToServer;
    private int sslPortNumber = 443;
    private String host = "hostname";


    @FXML
    public void onClickedNameInList(MouseEvent event){
        if(!messageField.getText().startsWith("[/w]")){
            String name = listView.getSelectionModel().getSelectedItem();
            String whisperCommand = "[/w] (" + name +") ";
            String textInMessageField = messageField.getText();
            String whisperCommandAddedToMessageField = whisperCommand.concat(textInMessageField);
            Platform.runLater(() ->{
                messageField.setText(whisperCommandAddedToMessageField);
            });
        }
    }

    @FXML
    public void sendMessage(ActionEvent event){
        String filteredInputMessage = filterText(messageField.getText());
        try{
            if(filteredInputMessage.isEmpty()){
                return;
            }
            Message newMessage = new Message(clientUser, new Date(), filteredInputMessage);

            messageToServer.writeObject(newMessage);
            messageToServer.flush();
            messageField.clear();

        }catch(IOException e){
            System.out.println("send message error place");
            e.printStackTrace();
        }
    }
    private boolean checkNumeric(String text){
        try{
            int numeric = Integer.parseInt(text);
            return true;
        }catch(InputMismatchException ex){
            chatView.appendText("Host port cannot contain non numeric characters\n");
            return false;
        }

    }
    @FXML
    public void connect(ActionEvent event){
        if(UserName.getText()!="" && HostPort != null && HostAddress != null && checkNumeric(HostPort.getText())){
            listView.setItems(names);
            messageField.setEditable(true);
            ConnectButton.setDisable(true);
            DisconnectButton.setDisable(false);
            sendButton.setDisable(false);
            UserName.setEditable(false);
            HostAddress.setEditable(false);
            HostPort.setEditable(false);
            messageField.requestFocus();
            try{
                SSLSocketFactory sslFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
                SSLSocket socket = (SSLSocket)sslFactory.createSocket(host, sslPortNumber);
                clientUser = new ClientUser(UserName.getText());
                new Thread(() ->{
                    try{
                        messageToServer = new ObjectOutputStream(socket.getOutputStream());
                        Message addOutputStreamToServerList = new Message(clientUser, new Date(), "");
                        messageToServer.writeObject(addOutputStreamToServerList);
                        ObjectInputStream messageFromServer = new ObjectInputStream(socket.getInputStream());

                        while(true){
                            Object serverMessage = messageFromServer.readObject();
                            if(serverMessage.getClass()==Message.class){
                                Message clientMessage= (Message) serverMessage;
                                if(clientMessage.getMessage().contains("have disconnected from the server")){
                                    if(names.contains(clientMessage.getClientUser().getUserName())){
                                        Platform.runLater(() ->{
                                            names.remove(clientMessage.getClientUser().getUserName());
                                        });
                                    }
                                }
                                String userName = clientMessage.getClientUser().getUserName();
                                SimpleDateFormat dt = new SimpleDateFormat("HH:mm");
                                String timestamp = dt.format(clientMessage.getMessageSentAt());
                                Platform.runLater(()->{
                                    chatView.appendText(timestamp + ' ' + (clientMessage.getClientUser().getUserName().equals(UserName.getText()) ? "you" : clientMessage.getClientUser().getUserName())
                                            + ": " + clientMessage.getMessage() + '\n');
                                });

                            }else if(serverMessage.getClass()==ClientUser.class){
                                ClientUser clientUser = (ClientUser) serverMessage;
                                if(!names.contains(clientUser.getUserName())){
                                    Platform.runLater(() ->{
                                        names.add(clientUser.getUserName());
                                    });
                                }
                            }
                        }
                    }catch(SocketException ex){
                        disconnect();
                        System.out.println("socket disconnected in client side, cc");
                    }catch(Exception ex){

                        ex.printStackTrace();
                    }
                }).start();
            }catch(Exception ex){
                disconnect();
                Platform.runLater(() ->{
                    chatView.appendText(ex.getMessage() + ", Try another port\n");
                });
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void disconnect(){
        try{
            messageField.clear();
            if(names.contains(clientUser.getUserName())){
                names.remove(clientUser.getUserName());
                Platform.runLater(() ->{
                    messageField.appendText("you have disconnected from the server");
                });
                for(int i = 0; i<names.size(); i++){
                    names.remove(i);
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        messageField.setEditable(false);
        ConnectButton.setDisable(false);
        DisconnectButton.setDisable(true);
        sendButton.setDisable(true);
        UserName.setEditable(true);
        HostAddress.setEditable(false);
        HostPort.setEditable(false);
        try{
            socket.close();
        }catch(Exception ex){
            System.out.println("disconnect ex in disconnect, cc");
        }
    }

    private String filterText(String message){
        return message;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {}
}