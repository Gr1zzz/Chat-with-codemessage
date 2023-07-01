package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;
    public class SocketThread extends Thread{
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
              Message message = connection.receive();
              if(message.getType() == MessageType.NAME_REQUEST){
                  String name = getUserName();
                 Message message1 = new Message(MessageType.USER_NAME, name);
                 connection.send(message1);
              }
              else if (message.getType() == MessageType.NAME_ACCEPTED){
                  notifyConnectionStatusChanged(true);
                  break;
              }
              else {
                  throw new IOException("Unexpected MessageType");
              }
            }
        }
        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT){
                    processIncomingMessage(message.getData());
                }
                else if(message.getType() == MessageType.SHIFRTEXT){
                    //message.Rashifr();
                    processIncomingMessage(message.getData());
                }
                else if (message.getType() == MessageType.USER_ADDED){
                    informAboutAddingNewUser(message.getData());
                }
                else if (message.getType() == MessageType.USER_REMOVED){
                    informAboutDeletingNewUser(message.getData());
                }
                else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " join");
        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + " left");
        }
        protected void notifyConnectionStatusChanged (boolean clientConnected){
           Client.this.clientConnected = clientConnected;
           synchronized (Client.this){
               Client.this.notify();
           }
        }
        @Override
        public void run(){
            try {
                // Создаем соединение с сервером
                connection = new Connection(new Socket(getServerAddress(), getServerPort()));
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Введите адрес сервера:");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Введите порт сервера:");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Введите ваше имя:");
        return ConsoleHelper.readString();
    }
    protected boolean shouldSendTextFromConsole(){
        return true;
    }
    protected SocketThread getSocketThread(){
        return new SocketThread();
    }
    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT,text));
        } catch (IOException e) {
            System.out.println("Error");
            clientConnected = false;
        }
    }
    protected void sendShifrTextMessage(String text){
        try {
            Message message = new Message(MessageType.SHIFRTEXT,text);
            //message.Shifr();
            connection.send(message);
        } catch (IOException e) {
            System.out.println("Error");
            clientConnected = false;
        }
    }
    public void run(){
       SocketThread socketThread = getSocketThread();
       socketThread.setDaemon(true);
       socketThread.start();
        synchronized (this){
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
                return;
            }
        }
        if (clientConnected){
            ConsoleHelper.writeMessage("Соединение установлено.\n" +
                    "Для выхода наберите команду 'exit'.");
        }
        else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        while (clientConnected){
            String exit = ConsoleHelper.readString();
            if(exit.equals("exit")){
                clientConnected = false;
            }
            else if (shouldSendTextFromConsole()){
                sendTextMessage(exit);
            }
        }

    }

    public static void main(String[] args){
        Client client = new Client();
        client.run();

    }
}
