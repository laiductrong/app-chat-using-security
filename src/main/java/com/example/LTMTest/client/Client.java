package com.example.LTMTest.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        } catch (IOException e) {
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
//            bufferedWriter.write(username);
//            bufferedWriter.newLine();
//            bufferedWriter.flush();
            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()){
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username+" : "+messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                       closeEverything(socket,bufferedReader,bufferedWriter);
                    }
                }
            }
        }).start();
    }

     public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String username= UUID.randomUUID().toString();
        System.out.println("Enter your username for the group chat: ");
//        String username = scanner.nextLine();
        Socket socket=new Socket("localhost",5678);
        Client client = new Client(socket,username);

        boolean isUsernameSet=false;
        do{
            username = scanner.nextLine();
            client.bufferedWriter.write(username);
            client.bufferedWriter.newLine();
            client.bufferedWriter.flush();
            String reponse= client.bufferedReader.readLine();
            System.out.println(reponse);
            client.username=username;
            if (!reponse.equals("not ok")){
                System.out.println("true");
                isUsernameSet=true;
            }
        }
        while (!isUsernameSet);

        client.listenForMessage();
        client.sendMessage();
    }
}
