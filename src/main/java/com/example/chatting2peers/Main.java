package com.example.chatting2peers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    @FXML
    private TextField password;

    @FXML
    private Button refresh;
    @FXML
    private Button delete;
    @FXML
    private ComboBox<?>  availableinterface;

    @FXML
    private TextField localip;

    @FXML
    public TextField localport1;

    @FXML
    private Button login;

    @FXML
    private Button logout;

    @FXML
    private ListView<String> messages;

    @FXML
    private TextArea onlineusers;

    @FXML
    private TextField remoteip;

    @FXML
    private TextField remoteport;

    @FXML
    private Button send;

    @FXML
    private TextField status;

    @FXML
    private TextField tcpserverip;

    @FXML
    private TextField tcpserverport;

    @FXML
    private Button testbutton;

    @FXML
    private TextArea textthatsend;

    @FXML
    private TextField username;
    final ServerSide serverSide = new ServerSide();
    boolean signed = false;
    ArrayList<String> stats = new ArrayList<>();

    private static final Color SENT_COLOR = Color.RED;
    private static final Color RECEIVED_COLOR = Color.BLUE;

    boolean sendrecieve = true;
    ObservableList<String> list = FXCollections.observableArrayList();


    @FXML
    void refreshable(MouseEvent event) throws IOException {
        if (tcpserverport.getText().isEmpty() || tcpserverip.getText().isEmpty() || !signed){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty fields");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all fields");
            alert.showAndWait();
            return;
        }
        String response= serverSide.callForActive(Integer.parseInt(tcpserverport.getText()),tcpserverip.getText());
     onlineusers.setText(response);

    }

    private boolean checkFields() {
        if (tcpserverport.getText().isEmpty() || tcpserverip.getText().isEmpty() || username.getText().isEmpty() || password.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty fields");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all fields");
            alert.showAndWait();
            return true;
        }
        return false;
    }


    @FXML
    void login(MouseEvent event) throws IOException {
        String response1="";
        if (checkFields()) return;
        if (signed==true) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Already logged in");
            alert.setHeaderText(null);
            alert.setContentText("Please logout at first");
            alert.showAndWait();
            return;
        }
        String response = serverSide.login(Integer.parseInt(tcpserverport.getText()), username.getText() + "-" + password.getText() + "-" + localip.getText(), tcpserverip.getText() + "," + localip.getText() + "," + localport1.getText());
        if (response.equals("success")) {
            signed = true;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Successful");
            alert.setHeaderText(null);
            alert.setContentText("You are logged in as " + username.getText().split("-")[0]);
            alert.showAndWait();
            password.clear();
            Socket clientSocket = new Socket(tcpserverip.getText() , Integer.parseInt(tcpserverport.getText()));
            DataOutputStream outToServer =
                    new DataOutputStream(clientSocket.getOutputStream());

            outToServer.writeBytes("active"+"@active"+ '\n');
            BufferedReader inFromServer =
                    new BufferedReader(new
                            InputStreamReader(clientSocket.getInputStream()));
              response1 = inFromServer.readLine();
              response1=response1.replace("^","\n");
            clientSocket.close();
        } else if (response.equals("fail")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Fail");
            alert.setHeaderText(null);
            alert.setContentText("User dne or wrong password");
            alert.showAndWait();
            return;
        }

        onlineusers.setText(response1);
    }

    @FXML
    void logout(MouseEvent event) throws IOException {
        if (signed==false) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Not logged in");
            alert.setHeaderText(null);
            alert.setContentText("Please login at first");
            alert.showAndWait();
            return;
        }
        serverSide.logout(Integer.parseInt(tcpserverport.getText()), username.getText()  ,tcpserverip.getText() );
        signed = false;
        remoteip.clear();
        remoteport.clear();
        textthatsend.clear();
        username.clear();
        status.clear();
        onlineusers.clear();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Signed out");
        alert.setHeaderText(null);
        alert.setContentText("You are signed out");
        alert.showAndWait();

    }

    @FXML
    void sendMsg(MouseEvent event) throws SQLException, IOException {

        if (localip.getText().isEmpty() || localport1.getText().isEmpty() || remoteport.getText().isEmpty() || remoteip.getText().isEmpty() || !signed || textthatsend.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty fields");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all fields");
            alert.showAndWait();
            return;
        }
        serverSide.sendMessageTo(remoteip.getText(), Integer.parseInt(remoteport.getText()), textthatsend.getText(), username.getText().trim());

        String message = username.getText().trim() + ":" + textthatsend.getText();
        list.add(message);
        int lastIndex = list.size() - 1;
        list.set(lastIndex, message);
        messages.setItems(list);
        messages.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        textthatsend.clear();

        messages.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                setText(item);
                if (!(item == null)) {
                    //                        System.out.println("item osana" + " = " + item);
                    if (item.split(":")[0].equals(username.getText().trim()))
                        setTextFill(Color.BLUE);
                    else setTextFill(Color.RED);
                } else setTextFill(Color.BISQUE);

            }
        });

    }

    static int userPort = 0;
    int tcpPort = 0;
    static boolean flag = true;
    boolean delo = false;

    @FXML
    void recieved() {
        sendrecieve = true;
        userPort = Integer.parseInt(localport1.getText().isEmpty() ? "0" : localport1.getText().trim());
        tcpPort = Integer.parseInt(tcpserverport.getText().isEmpty() ? "0" : tcpserverport.getText().trim());
        if (userPort != 0 && flag && tcpPort != 0 && signed == true)
            try {

                flag = false;
                AtomicReference<String> msg = new AtomicReference<>("");
                Thread receiver = new Thread(() -> {
                    DatagramSocket clientSocket = null;

                    if (userPort != 0) {

                        try {
                            clientSocket = new DatagramSocket(userPort);
                        } catch (SocketException e) {
                            System.out.println("  ");
                            throw new RuntimeException(e);
                        }
                        byte[] receiveData = new byte[1024];
                        while (true) {
                            DatagramPacket receivePacket =
                                    new DatagramPacket(receiveData, receiveData.length);
                            try {
                                clientSocket.receive(receivePacket);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            String modifiedSentence =
                                    new String(receivePacket.getData()).trim();
                            String messageq = modifiedSentence.split("break")[0];
                            System.out.println(messageq);
                            if (messageq.split("=")[0].equals("del")) {
                                messages.getItems().remove(Integer.parseInt(String.valueOf(messageq.charAt(4))));

                            } else {

                                //                   if(msg.get().isEmpty()) {
                                {
                                    msg.set(messageq);
                                    String message = msg.get().split("=")[1];
                                    list.add(message);
                                    int lastIndex = list.size() - 1;
                                    list.set(lastIndex, message);
                                    messages.setItems(list);
                                    status.setText("Recived from ip=" + remoteip.getText() + ",port=" + remoteport.getText());
                                    messages.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                                    messages.setCellFactory(lv -> new ListCell<>() {
                                        @Override
                                        protected void updateItem(String item, boolean empty) {
                                            super.updateItem(item, empty);

                                            setText(item);
                                            if (!(item == null)) {
                                                if (item.split(":")[0].equals(username.getText().trim()))
                                                    setTextFill(Color.BLUE);
                                                else setTextFill(Color.RED);
                                            } else setTextFill(Color.BISQUE);
                                        }

                                    });
                                    //                   }

                                }
                            }
                        }
                    }

                });

                receiver.start();

            } catch (Exception e) {

            }

    }

    @FXML
    void deleteMsg(MouseEvent event) throws SQLException, IOException {
        messages.getItems().remove(messages.getSelectionModel().getSelectedIndex());
        messages.refresh();
        serverSide.sendDeleteSignal(remoteip.getText().trim(), Integer.parseInt(remoteport.getText().trim()), messages.getSelectionModel().getSelectedIndex() + 1);

    }

}