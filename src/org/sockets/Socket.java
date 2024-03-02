package org.sockets;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Socket {
    public static final int MAX_PROGRAM_SIZE = 1024;
    private InetAddress targetAddress;
    private int targetPort;
    private String codeFilename;

    private boolean init(String[] args) throws UnknownHostException {
        boolean retval = false;
        if (args.length > 2) {
            targetAddress = InetAddress.getByName(args[0]);
            targetPort = Integer.parseInt(args[1]);
            codeFilename = args[2];
            retval = true;
        } else {
            System.out.println("Usage: " + Socket.class.getSimpleName() + " <IP address> <port> <code filename>");
        }

        return retval;
    }

    private void go() throws IOException, Parser.InvalidInstructionException, Parser.InvalidLabelException, Parser.InvalidOperandException, ProtocolException, Executor.InvalidPendingInstructionException, Executor.InvalidMemoryAddressException, Executor.InvalidInstructionException {
        DataInputStream dIn = null;
        DataOutputStream dOut = null;
        boolean firstRun = true;
        Parser parser = new Parser(codeFilename);
        Executor executor = new Executor(parser.parse());

        try (java.net.Socket socket = new java.net.Socket(targetAddress, targetPort)) {
            System.out.println("Connected to base.");
            dIn = new DataInputStream(socket.getInputStream());
            dOut = new DataOutputStream(socket.getOutputStream());
            do {
                String init = dIn.readUTF();
                boolean cmdGo = isInitGo(init, firstRun);
                if (cmdGo) {
                    String cmdOut = executor.cycle();
                    dOut.writeUTF(cmdOut);
                    if (Executor.HIT_REPORT_STRING.equals(dIn.readUTF())) {
                        System.out.println("===> " + Executor.HIT_REPORT_STRING);
                    }
                }
                String cmdIn = dIn.readUTF();
                String response = "null";
                int address = 0;
                if (!cmdIn.isBlank()) {
                    address = Integer.parseInt(cmdIn) % MAX_PROGRAM_SIZE;
                    response = executor.actionCmd(address);
                }
                dOut.writeUTF(response);
                if (Executor.HIT_REPORT_STRING.equals(response)) {
                    System.out.println("They hit us at address " + address);
                }
                firstRun = false;
            } while (true);
        } catch (EOFException e) {
            System.out.println("=======\n= DIS =\n=======");
        } finally {
            if (dIn != null) {
                dIn.close();
            }
            if (dOut != null) {
                dOut.close();
            }
        }
    }

    static void showProg(int[] program) {
        System.out.print("Program: ");
        for(int i = 0; i < 10; ++i) {System.out.print(program[i] + " ");}
        System.out.println();
    }

    private boolean isInitGo(String init, boolean firstRun) throws ProtocolException {
        boolean retval = false;
        do {
            if (Base.GO_CMD.equals(init)) {
                retval = true;
                break;
            }
            if (firstRun && Base.WAIT_CMD.equals(init)) {
                break;
            }
            throw new ProtocolException("At init, received " + init);
        } while (false);
        return retval;
    }

    public static void main(String[] args) throws IOException,
                                                  Parser.InvalidInstructionException,
                                                  Parser.InvalidLabelException,
                                                  Parser.InvalidOperandException,
                                                  ProtocolException,
                                                  Executor.InvalidPendingInstructionException,
                                                  Executor.InvalidMemoryAddressException,
                                                  Executor.InvalidInstructionException {
        Socket socket = new Socket();
        if (socket.init(args)) {
            socket.go();
        }
    }

    static class ProtocolException extends Exception {
        public ProtocolException(String msg) {super(msg);}
    }
}