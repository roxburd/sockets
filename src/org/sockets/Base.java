package org.sockets;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Base {

    private static final int PORT = 4444;

    public static final String GO_CMD = "GO";
    public static final String WAIT_CMD = "WAIT";

    private void go() throws IOException {
        Socket client1socket = null;
        Socket client2socket = null;
        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("IP:" + ss.getInetAddress());
            System.out.println("port:" + ss.getLocalPort());
            do {
                try {
                    System.out.println("Waiting for client connections...");
                    client1socket = ss.accept();
                    System.out.println("Client 1 connected.");
                    client2socket = ss.accept();
                    System.out.println("Client 2 connected.");
                    serve(client1socket, client2socket);
                    System.out.println("=================================");
                } finally {
                    if (client1socket != null) {
                        client1socket.close();
                    }
                    if (client2socket != null) {
                        client2socket.close();
                    }
                }
            } while (true);
        }
    }

    private void serve(Socket client1, Socket client2) throws IOException {
        DataInputStream in1 = new DataInputStream(client1.getInputStream());
        DataOutputStream out1 = new DataOutputStream(client1.getOutputStream());
        DataInputStream in2 = new DataInputStream(client2.getInputStream());
        DataOutputStream out2 = new DataOutputStream(client2.getOutputStream());
        boolean client1Turn = true;  // Math.random() < 0.5;
        sendWait(out1, out2, client1Turn);
        boolean run = true;
        try {
            while (run){
                run = relay(client1Turn ? in1 : in2,
                            client1Turn ? out1 : out2,
                            client1Turn ? in2 : in1,
                            client1Turn ? out2 : out1);
                client1Turn = !client1Turn;
            }
        } catch (IOException e) {
            // do nowt
        }
    }

    private void sendWait(DataOutputStream out1, DataOutputStream out2, boolean client1Turn) throws IOException {
        DataOutputStream dos = (client1Turn ? out2 : out1);
        dos.writeUTF(WAIT_CMD);
    }

    /**
     * Prompt active.
     * Read and relay command.
     * Read and relay response.
     */
    private boolean relay(DataInputStream activeIn,
                          DataOutputStream activeOut,
                          DataInputStream targetIn,
                          DataOutputStream targetOut) throws IOException {
        activeOut.writeUTF(GO_CMD);  // Prompt active.
        targetOut.writeUTF(activeIn.readUTF());
        activeOut.writeUTF(targetIn.readUTF());

        return true;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Starting org.sockets.Base.");
        Base main = new Base();
        main.go();
    }
}