package org.sockets;

import java.util.HashMap;
import java.util.Map;

public class Executor {
    public static final String HIT_REPORT_STRING = "HIT";
    /*pkg-priv*/ static Map<String, Integer> assembly = new HashMap<>();
    private int[] program = new int[Socket.MAX_PROGRAM_SIZE];
    private int counter;
    private Integer pendingInstruction = null;
    private int add;

    public Executor(int[] program) {
        this.program = program;
        assembly.put("NOP", 0);
        assembly.put("JMP", 1);  // Takes address
        assembly.put("LOC", 2);  // Takes increment to add
        assembly.put("WRT", 3);
        counter = 0;
    }

    public String cycle() throws InvalidMemoryAddressException, InvalidInstructionException, InvalidPendingInstructionException {
        System.out.print("executing address " + counter + " ");
        Socket.showProg(program);
        String retval = "";
        try {
            if (pendingInstruction == null) {  // Look for instruction.
                switch(program[counter]) {
                    case 1:  // JMP
                    case 2:  // LOC
                        pendingInstruction = program[counter];
                        break;
                    case 3:  // WRT
                        retval = String.valueOf(add);
                        System.out.println("Writing to " + add);
                        // fall-through
                    case 0:  // NOP
                        pendingInstruction = null;
                        break;
                    default:
                        throw new InvalidInstructionException(program[counter], counter);
                }
            } else {  // Look for instruction's operand.
                switch(pendingInstruction) {
                    case 1:  // JMP
                        counter = program[counter] - 1;
                        break;
                    case 2:  // LOC
                        add += program[counter];
                        break;
                    default:
                        throw new InvalidPendingInstructionException(pendingInstruction, counter);
                }
                pendingInstruction = null;
            }
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidMemoryAddressException(counter, e);
        }
        ++counter;
        return (retval);
    }

    public String actionCmd(int address) {
        String retval = "";
        if (address > 1) {
            int previous = program[address];
            program[address] = 0;
            if (previous != 0) {
                retval = HIT_REPORT_STRING;
                System.out.print("We were hit at address " + address + " (" + previous + " -> 0) ");
                Socket.showProg(program);
            }
        } else {
            System.out.println("Attempt to write to protected address (" + address + ") blocked.");
        }
        return retval;
    }

    static class InvalidMemoryAddressException extends Exception {
        public InvalidMemoryAddressException(int counter, Exception e) {
            // Invalid memory address (nnn).
            super("Invalid memory address (" + counter + ").", e);
        }
    }

    static class InvalidInstructionException extends Exception {
        InvalidInstructionException(int instruction, int counter) {
            // Invalid instruction 44 at line 34.
            super("Invalid instruction " + instruction + " at line " + counter + ".");
        }
    }

    static class InvalidPendingInstructionException extends Exception {
        InvalidPendingInstructionException(int instruction, int counter) {
            // Invalid pending instruction 44 at line 34.
            super("Invalid pending instruction " + instruction + " at line " + counter + ".");
        }
    }
}
