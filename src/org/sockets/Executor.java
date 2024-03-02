package org.sockets;

public class Executor {
    public static final String HIT_REPORT_STRING = "HIT";
    private int[] program = new int[Socket.MAX_PROGRAM_SIZE];
    private int counter;
    private Integer pendingInstruction = null;
    private int add;

    enum Instructions {
        NOP,
        JMP,  // Takes address
        LOC,  // Takes increment to add
        WRT
    }

    public Executor(int[] program) {
        this.program = program;
        counter = 0;
    }

    public String cycle() throws InvalidMemoryAddressException, InvalidInstructionException, InvalidPendingInstructionException {
        String retval = "";
        try {
            if (pendingInstruction == null) {  // Look for instruction.
                Instructions instruction = Instructions.values()[program[counter]];
                switch(instruction) {
                    case JMP:
                    case LOC:
                        pendingInstruction = program[counter];
                        break;
                    case WRT:
                        retval = String.valueOf(add);
                        System.out.println(String.format("0x%04X " + Instructions.values()[program[counter]], counter) +
                                " (add = " + add + ")");
                        pendingInstruction = null;
                        break;
                    case NOP:
                        System.out.println(String.format("0x%04X " + Instructions.values()[program[counter]], counter));
                        pendingInstruction = null;
                        break;
                    default:
                        throw new InvalidInstructionException(program[counter], counter);
                }
            } else {  // Look for instruction's operand.
                Instructions instruction = Instructions.values()[pendingInstruction];
                switch(instruction) {
                    case JMP:
                        System.out.println(String.format("0x%04X " + Instructions.values()[pendingInstruction] +
                                " " + program[counter], counter));
                        counter = program[counter] - 1;
                        break;
                    case LOC:
                        add += program[counter];
                        System.out.println(String.format("0x%04X " + Instructions.values()[pendingInstruction] +
                                " " + program[counter], counter));
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
