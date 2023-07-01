package com.javarush.task.task30.task3008;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    public static void writeMessage(String message){
        System.out.println(message);
    }
    public static String readString() {
        String line;
        while (true) {
            try {
                line = bufferedReader.readLine();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
                continue;
            }
        }
        return line;
    }
    public static int readInt(){
        int x = 0;
        while (true) {
            try {
                x = Integer.parseInt(readString());
                break;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                System.out.println("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
                continue;
            }
        }
        return x;
    }
}
