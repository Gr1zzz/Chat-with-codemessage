package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message) {
        for (String key : connectionMap.keySet()) {
            try {
                connectionMap.get(key).send(message);
            } catch (IOException e) {
                Message message1 = new Message(MessageType.TEXT, "Не удалось отправить сообщение");
                try {
                    connectionMap.get(key).send(message1);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            boolean v = true;
            String name = "";
            other:
            while (v) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                name = message.getData();
                if (name != "" && message.getType() == MessageType.USER_NAME) {
                    for (String key : connectionMap.keySet()) {
                        if (name.equals(key)) {
                            continue other;
                        }
                    }
                    connectionMap.put(name, connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    v = false;
                }
            }
            return name;
        }
        private void notifyUsers(Connection connection, String userName) throws IOException{
            for (String key : connectionMap.keySet()) {
                if(!key.equals(userName)){
                    connection.send(new Message(MessageType.USER_ADDED,key));
                }
            }
        }
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if(message.getType() == MessageType.TEXT){
                    Message m = new Message(MessageType.TEXT, userName+": " + message.getData());
                    sendBroadcastMessage(m);
                    System.out.println(userName+": " + message.getData());
                }
                else if(message.getType() == MessageType.SHIFRTEXT) {
                    Message m = new Message(MessageType.SHIFRTEXT, userName + ": " + message.getData());
                    sendBroadcastMessage(m);
                    m.Shifr();
                    System.out.println(userName+": " + m.getData());
                }
                else {
                    ConsoleHelper.writeMessage("Error");
                }
            }
        }
        @Override
        public void run() {
            ConsoleHelper.writeMessage("Установлено новое соединение с " + socket.getRemoteSocketAddress());

            String userName = null;

            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);

                // Сообщаем всем участникам, что присоединился новый участник
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));

                // Сообщаем новому участнику о существующих участниках
                notifyUsers(connection, userName);

                // Обрабатываем сообщения пользователей
                serverMainLoop(connection, userName);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка при обмене данными с " + socket.getRemoteSocketAddress());
            }

            if (userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }

            ConsoleHelper.writeMessage("Соединение с " + socket.getRemoteSocketAddress() + " закрыто.");
        }
    }

    public static void main(String[] args) throws IOException {
        ConsoleHelper.writeMessage("Введите порт сервера:");
        int port = ConsoleHelper.readInt();
        try (ServerSocket serverSocket = new ServerSocket(port, 10, InetAddress.getByName("192.168.0.196"))) {
            ConsoleHelper.writeMessage("Чат сервер запущен.");
            while (true) {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage("Произошла ошибка при запуске или работе сервера.");
        }

    }
}
