package de.swiftbyte.jdaboot.cli;

import java.util.HashMap;
import java.util.Scanner;

public class ConsoleThread extends Thread {

    private HashMap<String, ConsoleCommandExecutor> commands = new HashMap<>();

    public ConsoleThread() {
        Scanner sc = new Scanner(System.in);
    }

    @Override
    public void run() {


    }
}
