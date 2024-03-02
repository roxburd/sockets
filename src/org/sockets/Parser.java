package org.sockets;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private final String filename;
    private int[] program = new int[Socket.MAX_PROGRAM_SIZE];
    private Pattern labelPattern;

    public Parser(String filename) throws IOException {
        this.filename = filename;
        labelPattern = Pattern.compile("^[a-zA-Z]*$");
    }

    public int[] parse() throws IOException, InvalidInstructionException, InvalidLabelException, InvalidOperandException {
        Map<String, Integer> labels = new HashMap<>();

        BufferedReader br = new BufferedReader(new FileReader(filename));
        // Read labels.
        int counter = 0;
        String line = br.readLine();
        while (line != null) {
            counter += findLabel(line, counter, labels);
            line = br.readLine();
        }
        br.close();
        // Read program.
        counter = 0;
        br = new BufferedReader(new FileReader(filename));
        line = br.readLine();
        while (line != null) {
            counter = parseLine(line, counter, labels);
            line = br.readLine();
        }
        br.close();
        Socket.showProg(program);
        return program;
    }

    private int findLabel(String line, int counter, Map<String, Integer> labels) throws InvalidLabelException {
        int addressSpacesUsed = 0;
        if (line.startsWith(":")) {
            String label = line.substring(1);
            Matcher labelMatcher = labelPattern.matcher(label);
            if (labelMatcher.find()) {
                labels.put(label, counter);
            } else {
                throw new InvalidLabelException(label, counter);
            }
        } else {
            String[] tokens = line.split("\\s+");
            addressSpacesUsed = tokens.length > 1 ? 2 : 1;
        }
        return addressSpacesUsed;
    }

    /*pkg-priv*/ int parseLine(String line, int counter, Map<String, Integer> labels) throws InvalidInstructionException, InvalidOperandException {
        Integer instruction;
        Integer operand = null;

        if (!line.startsWith(":")) {
            String[] tokens = line.split("\\s+");
            switch (tokens[0].toUpperCase()) {
                case "NOP":
                    instruction = 0;
                    break;
                case "JMP":
                    instruction = 1;
                    try{
                        operand = getOperand(tokens[1], labels);
                    } catch (NumberFormatException e) {
                        throw new InvalidOperandException(line, counter);
                    }
                    break;
                case "LOC":
                    instruction = 2;
                    try{
                        operand = getOperand(tokens[1], null);
                    } catch (NumberFormatException e) {
                        throw new InvalidOperandException(line, counter);
                    }
                    break;
                case "WRT":
                    instruction = 3;
                    break;
                default:
                    throw new InvalidInstructionException(line, counter);
            }
            program[counter++] = instruction;
            if (operand != null) {
                program[counter++] = operand;
            }
        }
        return counter;
    }

    private Integer getOperand(String token, Map<String, Integer> labels) {
        Integer retval;
        if (labels != null && labels.containsKey(token)) {
            retval = labels.get(token);
        } else {
            retval = Integer.parseInt(token);
        }
        return retval;
    }

    static class InvalidInstructionException extends Exception {
        InvalidInstructionException(String line, int counter) {
            // Invalid instruction 'XXX' at line 34.
            super("Invalid instruction '" + line + "' at line " + counter + ".");
        }
    }

    static class InvalidOperandException extends Exception {
        InvalidOperandException(String line, int counter) {
            // Invalid operand 'XXX' at line 34.
            super("Invalid operand '" + line + "' at line " + counter + ".");
        }
    }

    static class InvalidLabelException extends Exception {
        InvalidLabelException(String line, int counter) {
            // Invalid label 'XXX' at line 34.
            super("Invalid label '" + line + "' at line " + counter + ".");
        }
    }
}
