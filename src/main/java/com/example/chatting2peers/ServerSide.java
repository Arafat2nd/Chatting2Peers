package com.example.chatting2peers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class ServerSide {
    void sendMessageTo(String receiverIP, int receiverPort, String body, String sender ) throws IOException, SQLException {
//        if (!Persistent.convoExists(sender, receiver)) {
//            Persistent.createConvo(sender, receiver);
//        }
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(receiverIP);
        byte[] sendData;
        long now = System.currentTimeMillis();
        Timestamp ts = new Timestamp(now);
        Date date = new Date(ts.getTime());

        String info = "msg="+sender + ":" + body + "-" + date ;
        sendData = info.getBytes();
        DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, IPAddress, receiverPort);
        clientSocket.send(sendPacket);
        clientSocket.close();

    }
    void sendDeleteSignal(String receiverIP, int receiverPort, int rank) throws IOException, SQLException {
         DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName(receiverIP);
        byte[] sendData;
        String info = "del="+rank;
        sendData = info.getBytes();
        DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, IPAddress, receiverPort);
        clientSocket.send(sendPacket);
        clientSocket.close();
    }
    String login(int port,String info,String ip) throws IOException {
        Socket clientSocket = new Socket(ip.split(",")[0] , port);
        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        outToServer.writeBytes(info +"@loginGG"+ip.split(",")[1]+","+ip.split(",")[2] + '\n');
        BufferedReader inFromServer =
                new BufferedReader(new
                        InputStreamReader(clientSocket.getInputStream()));
        String response = inFromServer.readLine();
        clientSocket.close();
        return  response;
    }
    String logout(int port,String info,String ip) throws IOException {
        Socket clientSocket = new Socket(ip , port);
        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        outToServer.writeBytes(info +"@remove"+ '\n');
        BufferedReader inFromServer =
                new BufferedReader(new
                        InputStreamReader(clientSocket.getInputStream()));
        String response = inFromServer.readLine();
        clientSocket.close();
        return  response;
    }
    String callForActive(int port,String ip) throws IOException {
        Socket clientSocket = new Socket(ip , port);
        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        outToServer.writeBytes("active"+"@active"+ '\n');
        BufferedReader inFromServer =
                new BufferedReader(new
                        InputStreamReader(clientSocket.getInputStream()));
        String response = inFromServer.readLine();
        response=response.replace("^","\n");
        System.out.println();
        clientSocket.close();
        return  response;
    }

}
