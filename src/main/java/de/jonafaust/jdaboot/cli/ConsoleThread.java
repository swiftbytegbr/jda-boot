package de.jonafaust.jdaboot.cli;

import java.util.HashMap;
import java.util.Scanner;

public class ConsoleThread extends Thread {

    private final Scanner sc;
    private HashMap<String, ConsoleCommandExecutor> commands = new HashMap<>();

    public ConsoleThread() {
        this.sc = new Scanner(System.in);
    }

    @Override
    public void run() {


    }
}
