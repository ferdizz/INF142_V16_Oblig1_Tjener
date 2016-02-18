import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class Server {

    public static void main(String[] args) {
        ArrayList<Request> history = new ArrayList<>();
        int number = 9001;
        String melding = "";
        String nyMelding = "Hei hei";
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(7001);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        while (true) {
            System.out.println("Server is running...");
            try {
                Socket connectionSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream out = new DataOutputStream(connectionSocket.getOutputStream());

                melding = in.readLine();
                String ip = connectionSocket.getInetAddress().getHostName();
                System.out.println("New request from " + ip + ": " + melding);
                String[] words = melding.split(" ");
                if (words.length == 3) {
                    if (words[2].equals("GET")) {
                        history.add(new Request(ip, "GET", "NULL"));
                        out.writeBytes("NUMBER " + number + "\n");
                    } else if (words[2].equals("HISTORY")) {
                        String historyMessage = "";
                        for (Request r : history) {
                            Date date = new Date(r.getTime());
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
                            String formattedDate = sdf.format(date);
                            historyMessage += formattedDate + " : " + r.getIp() + " : " + r.getAction() + " : " + r.getArgument() + "NEW_LINE";
                        }
                        out.writeBytes(historyMessage + "\n");
                    }
                } else if (words.length == 4) {
                    int newNumber = -1;
                    switch (words[2]) {
                        case "ADD":
                            newNumber = -1;
                            try {
                                newNumber = Integer.parseInt(words[3]);
                            } catch (NumberFormatException e) {
                                out.writeBytes("ERROR: Request didn't succeed. Invalid format.\n");
                            }
                            if (newNumber != -1) {
                                number += newNumber;
                                history.add(new Request(ip, "ADD", "" + newNumber));
                                out.writeBytes("OK\n");
                            }
                            break;
                        case "SUB":
                            newNumber = -1;
                            try {
                                newNumber = Integer.parseInt(words[3]);
                            } catch (NumberFormatException e) {
                                out.writeBytes("ERROR: Request didn't succeed. The number to add must be a number.\n");
                            }
                            if (newNumber != -1) {
                                number -= newNumber;
                                history.add(new Request(ip, "SUB", "" + newNumber));
                                out.writeBytes("OK\n");
                            }
                            break;
                        default:
                            out.writeBytes("ERROR: Request didn't succeed. Invalid argument.\n");
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
