import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

public class MyXargs {

// Checks if the character passed is valid
    private static boolean validChar(char letter) {
        switch (letter) {
            case ';':
            case '&':
            case '|':
            case '>':
            case '<':
            case '*':
            case '?':
            case '(':
            case ')':
            case '$':
                return false;
            default:
                return true;
        }
    }

// Reads lines and returns List of String args
    private static List<String> formatIn( String line) {
        List<String> wordList = new ArrayList<>();
        String word = "";
        int letter = 0;

        while (letter < line.length()) {
            char s = line.charAt(letter);

            if (validChar(s)) {
                if (s != ' ') {
                    word+=s;
                } else {
                    if (word.length() > 0) {
                        wordList.add(word.trim());
                        word = "";
                    }
                }
            }
            letter++;
        }
        if (word.length() > 0) {
            wordList.add(word.trim());
        }

        return wordList;
    }

// command and args are joined and depending on the options
    private static void runProgram(List<String> wordList, List<String> commands, String xargsOp, boolean enablePrintCommand) {

        List<String> newCommand = new ArrayList<>(commands);
        String printCommandArgs = String.join(" ", wordList);

    // If option is "-I", place "{}" in all args
        if (xargsOp.equals("-I")) {
            for (int i = 0; i < newCommand.size(); i++) {
                newCommand.set(i, newCommand.get(i).replace("{}", printCommandArgs));
            }

    // If "-n" and no commands, add all args, print and return
        } else if (xargsOp.equals("-n") && commands.isEmpty()) {
            String nOut="";
            for (String s : wordList) {
                nOut = nOut + " " + s;
            }
            nOut = nOut.trim();
            System.out.println(nOut);
            return;
        } else if (!xargsOp.equals("-I")) {
    // Split the arguments so that each word is a separate argument
            newCommand.addAll(wordList);
        }

    // If print "-t" is a option then enablePrintCommand is true
        if (enablePrintCommand) {
            System.out.println("+ " + String.join(" ", newCommand));
        }

    // Run process with the String List that contains commands and args
        try {
            ProcessBuilder pb = new ProcessBuilder(newCommand);
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static final String USAGE_ERR = "Usage: java MyXargs.java [-n num] [-I replace] [-t] [-r] command";

    public static void main(String[] args) throws IOException {

    // No args given
        if (args.length == 0) {
            System.err.println(USAGE_ERR);
            System.exit(1);
        }

        BufferedReader sInReader = new BufferedReader(new InputStreamReader(System.in));
        List<String> wordList = new ArrayList<>();
        boolean enablePrintCommand = false;
        int numOfCommands = 0;
        String xargsOp = "";
        String stdInEmptyOp = "";
        List<String> commands = new ArrayList<>();

        int j = 0;

    
    // Validating args, and splitting commands to run
        while (j < args.length) {
            if (args[j].charAt(0) == '-') {
                switch (args[j]) {
                    case "-r":
                        // If "-r" set stdInEmptyOp
                        stdInEmptyOp = "-r";
                        break;
                    case "-t":
                        enablePrintCommand = true;
                        break;
                    case "-I":
                        
                        // If "-I" then check next arg if equals "{}" else print Usage error
                        xargsOp = "-I";
                        if (j + 1 < args.length && args[j + 1].equals("{}")) {
                            j++;
                        } else {
                            System.err.println(USAGE_ERR);
                            System.exit(1);
                        }
                        break;
                    case "-n":

                        // If "-n" read next value as int, value must greater than zero else print usage error
                        xargsOp = "-n";
                        if (j + 1 < args.length) {
                            numOfCommands = Integer.parseInt(args[j + 1]);
                            if (numOfCommands <= 0) {
                                System.err.println(USAGE_ERR);
                                System.exit(1);
                            }
                            j++;
                        } else {
                            System.err.println(USAGE_ERR);
                            System.exit(1);
                        }
                        break;
                    default:
                        // Not valid option for MyXargs
                        System.err.println(USAGE_ERR);
                        System.exit(1);
                }
            } else {
                // If not star with "-" then we read left over args, as they are commands
                while(j < args.length){
                    commands.add(args[j]);
                    j++;
                }
            }
            j++;
        }

        String inLine;
        List<String> currentBatch = new ArrayList<>();

        switch (xargsOp) {
            case "-I":
                
                // Check if we can read from BufferRead object aleast once
                if((inLine = sInReader.readLine()) != null){
                    wordList = formatIn(inLine);
                    runProgram(wordList, commands, xargsOp, enablePrintCommand);
                    wordList.clear();
                    while ((inLine = sInReader.readLine()) != null) {
                        wordList = formatIn(inLine);
                        runProgram(wordList, commands, xargsOp, enablePrintCommand);
                        wordList.clear();
                    }
                // If not then System.in passed nothing
                }else if ((inLine == null) && stdInEmptyOp.equals("-r")) {
                    System.exit(1);
                }
                break;

            case "-n":

            // Read and check if wordList is empty and stdInEmptyOp
                while ((inLine = sInReader.readLine()) != null) {
                    wordList.addAll(formatIn(inLine));
                }
                if (wordList.isEmpty() && stdInEmptyOp.equals("-r")) {
                    System.exit(1);
                }

            // Add numOfCommand args we want per line to i to be passed to runProgram, 
            // and find the min between i + numOfCommands and totalItems
            // to prevent out of bound error
                int totalItems = wordList.size();
                for (int i = 0; i < totalItems; i += numOfCommands) {
                    currentBatch = wordList.subList(i, Math.min(i + numOfCommands, totalItems));
                    runProgram(currentBatch, commands, xargsOp, enablePrintCommand);
                }
                break;

            default:
            // Read and check if wordList is empty and stdInEmptyOp, and runProgram
            // with all args
                while ((inLine = sInReader.readLine()) != null) {
                    wordList.addAll(formatIn(inLine));
                }
                if (wordList.isEmpty() && stdInEmptyOp.equals("-r")) {
                    System.exit(1);
                }

                runProgram(wordList, commands, xargsOp, enablePrintCommand);
                break;
        }
    }
}