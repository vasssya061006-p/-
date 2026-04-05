package server;

import server.network.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) {
        int port = 8080;
        ExecutorService threadPool = Executors.newCachedThreadPool();

        System.out.println("Запуск сервера на порту " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер успешно запущен! Ожидание подключений...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Новый клиент подключился: " + clientSocket.getInetAddress());
                threadPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
        }
    }
}