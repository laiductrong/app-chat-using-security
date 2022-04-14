package com.example.LTMTest.server;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String clinetUsername;
    public String partner = "";
    public String key;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public ClientHandler() {

    }

    //kiểm tra tên người dùng nhập vào
    public boolean checkName(String newName) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clinetUsername.equals(newName)) {
                return true;
            }
        }
        return false;
    }


    //gửi tin nhắn tới client xem có muốn trò chuyện với người này hay không
    public void askClient() {
        ClientHandler clientHandlerAsk = null;
        ArrayList<String> listUserNoHavePartner = new ArrayList<String>();
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals("")) {
                listUserNoHavePartner.add(clientHandler.clinetUsername);
            }
            if (clientHandler.clinetUsername.equals(clinetUsername)) {
                clientHandlerAsk = clientHandler;
            }
        }

        try {
            if (listUserNoHavePartner.size() > 1) {
                clientHandlerAsk.bufferedWriter.write(listUserNoHavePartner.toString());
                clientHandlerAsk.bufferedWriter.newLine();
                clientHandlerAsk.bufferedWriter.flush();
                setOption();
            } else {
                clientHandlerAsk.bufferedWriter.write("ban dang trong hang doi");
                clientHandlerAsk.bufferedWriter.newLine();
                clientHandlerAsk.bufferedWriter.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void askOtherClient(String nameOtherClient) {
        ArrayList<String> listUserNoHavePartner = new ArrayList<String>();
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals("")) {
                listUserNoHavePartner.add(clientHandler.clinetUsername);
            }
        }
        try {
            ClientHandler clientHandler = clientHandlers.get(clientHandlers.size() - 1);
            if (clientHandler.clinetUsername.equals(nameOtherClient)) {
                clientHandler.bufferedWriter.write(listUserNoHavePartner.toString());
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //xac nhan lua chon
    public boolean isOption(String option) {
        ArrayList<String> listUserNoHavePartner = new ArrayList<String>();
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals("")) {
                listUserNoHavePartner.add(clientHandler.clinetUsername);
            }
        }
        option = option.substring(clinetUsername.length() + 3);
        for (String s : listUserNoHavePartner) {
            if (s.equals(option)) {
                return true;
            }
        }
        return false;
    }

    //Gửi tin nhắn
    public void broadcastMessage(String messageToSend) {
        System.out.println("chuoi gui :" + messageToSend);
        for (ClientHandler clientHandler : clientHandlers) {
            //xác nhận lựa chọn
            if (clientHandler.partner.equals("") && isOption(messageToSend)) {
                String userWantChat = messageToSend.substring(clinetUsername.length() + 3);
                if (((clientHandlers.size())) > 0) {
                    for (ClientHandler clientHandler1 : clientHandlers) {
                        if (clientHandler1.clinetUsername.equals(userWantChat)) {
                            clientHandler1.partner = clinetUsername;
                            partner = userWantChat;
                        }
                    }
                }
            }
            //Kiểm tra nếu chưa có bạn chat thì cho chọn
            if (clientHandler.partner.equals("") && !isOption(messageToSend)) {
                try {
                    if (clientHandler.clinetUsername.equals(clinetUsername)) {
                        System.out.println("luwa chonn: " + messageToSend);
                        clientHandler.bufferedWriter.write("option of you is:");
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
            //Kiểm tra nếu đúng người chat chung thì send tin nhắn
            if (clientHandler.partner.equals(clinetUsername)) {
                try {
                    if (!clientHandler.clinetUsername.equals(clinetUsername)) {
                        clientHandler.bufferedWriter.write(messageToSend);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }
    }

    public void removeClientHandler() {
        //lấy danh sách hàng đợi
        ArrayList<String> listUserNoHavePartner = new ArrayList<String>();
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals("")) {
                listUserNoHavePartner.add(clientHandler.clinetUsername);
            }
        }
        //Gửi thông báo đến đối đương khi out và set partner bằng rỗng
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals(clinetUsername)) {
                try {
                    clientHandler.bufferedWriter.write("Đối phương đã thoát, danh sách chờ :" + listUserNoHavePartner.toString());
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
                clientHandler.partner = "";

            }
        }

        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + clinetUsername + " has left the chat!");
    }


    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
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

    public void setUserName() {
        try {
            String nametemp = bufferedReader.readLine();
            if (checkName(nametemp)) { //nếu bị trùng
                bufferedWriter.write("not ok");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                setUserName();
            } else {
                this.clinetUsername = nametemp;
//                bufferedWriter.write("ok");
//                bufferedWriter.newLine();
//                bufferedWriter.flush();
//                askClient();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //tạo khóa
    public String createKey(){
        int n=16;
        byte[] array = new byte[256];
        new Random().nextBytes(array);

        String randomString = new String(array, Charset.forName("UTF-8"));

        // Create a StringBuffer to store the result
        StringBuffer r = new StringBuffer();

        // remove all spacial char
        String  AlphaNumericString = randomString.replaceAll("[^A-Za-z0-9]", "");

        // Append first 20 alphanumeric characters
        // from the generated random String into the result
        for (int k = 0; k < AlphaNumericString.length(); k++) {
            if (Character.isLetter(AlphaNumericString.charAt(k))
                    && (n > 0)
                || Character.isDigit(AlphaNumericString.charAt(k))
                       && (n > 0)) {

                r.append(AlphaNumericString.charAt(k));
                n--;
            }
        }

        // return the resultant string
        return r.toString();
    }

    //lua chon cua nguoi dung
    public void setOption() {
        ArrayList<String> listUserNoHavePartner = new ArrayList<String>();
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.partner.equals("")) {
                listUserNoHavePartner.add(clientHandler.clinetUsername);
            }
        }
        {
            try {
                String option = bufferedReader.readLine();
                option = option.substring(clinetUsername.length() + 3);
                //tạo key cho người dùng
                this.key=createKey();
                //gắn partner cho người còn lại
                for (String s : listUserNoHavePartner) {
                    if (s.equals(option)) {
                        this.partner = option;
                        break;
                    }
                }
                for (ClientHandler clientHandler : clientHandlers) {
                    if (clientHandler.clinetUsername.equals(option)) {
                        clientHandler.partner = clinetUsername;
                        // gắn key cho người còn lại
                        clientHandler.key=this.key;
                        //khi co ng nhan tin thi gui thong diep toi ng con lai
                        clientHandler.bufferedWriter.write("bắt đầu trò chuyện với "+clinetUsername);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public String SECRET_KEY="123drtlcm5awo1xm";
//    String original = "trong dep trai vl";
    byte[] byteEncrypted;
    public String enCode(String messEncode) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher;
        String encrypted="";
        try {
            //encode
            cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byteEncrypted = cipher.doFinal(messEncode.getBytes());
            encrypted = Base64.getEncoder().encodeToString(byteEncrypted);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return encrypted;
    }

    public String deCode(String key){
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher;
        String decrypted="";
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] byteDecrypted = cipher.doFinal(byteEncrypted);
            decrypted = new String(byteDecrypted);

        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
            System.out.println("Lỗi mã hóa NoSuchAlgorithmException");
        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
            System.out.println("Lỗi mã hóa NoSuchPaddingException");
        } catch (InvalidKeyException e) {
//            e.printStackTrace();
            System.out.println("Lỗi mã hóa InvalidKeyException");
        } catch (IllegalBlockSizeException e) {
//            e.printStackTrace();
            System.out.println("Lỗi mã hóa IllegalBlockSizeException");
        } catch (BadPaddingException e) {
//            e.printStackTrace();
            System.out.println("Lỗi mã hóa BadPaddingException");
        }
        return decrypted;
    }

    @Override
    public void run() {
        this.clinetUsername = UUID.randomUUID().toString();// usernam temp
        clientHandlers.add(this);
        setUserName();
        askClient();
        while (socket.isConnected()) {
            try {
                String encodeMesss=enCode(bufferedReader.readLine());
                System.out.println("Nội dung tin nhắn đã được mã hóa" + encodeMesss);

                //Tìm tới người chat chung để gửi tin nhắn
                clientHandlers.stream().filter(c -> c.clinetUsername.equals(partner)).findFirst().ifPresent(clientHandler -> {
                            try {
                                clientHandler.bufferedWriter.write(deCode(clientHandler.key));
                                clientHandler.bufferedWriter.newLine();
                                clientHandler.bufferedWriter.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

}
