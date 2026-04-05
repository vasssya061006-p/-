package server;

import server.dao.DatabaseManager;
import server.network.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {
    public static void main(String[] args) {
        int port = 8080;
        int maxThreads = 50;
        
        // Load configuration
        try {
            Properties props = new Properties();
            var inputStream = Server.class.getClassLoader().getResourceAsStream("config.properties");
            if (inputStream != null) {
                props.load(inputStream);
                port = Integer.parseInt(props.getProperty("server.port", "8080"));
                maxThreads = Integer.parseInt(props.getProperty("server.max.threads", "50"));
                System.out.println("Configuration loaded from config.properties");
            } else {
                System.out.println("Warning: config.properties not found. Using defaults.");
            }
        } catch (Exception e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
        
        // Initialize database connection
        try {
            System.out.println("Initializing database connection...");
            DatabaseManager dbManager = DatabaseManager.getInstance();
            dbManager.initialize();
            System.out.println("Database connection established: " + dbManager.getDbUrl());
        } catch (Exception e) {
            System.err.println("WARNING: Database initialization failed: " + e.getMessage());
            System.err.println("Server will start without database. Some features will not work.");
        }
        
        ExecutorService threadPool = Executors.newFixedThreadPool(maxThreads);

        System.out.println("Starting server on port " + port);
        System.out.println("Max concurrent connections: " + maxThreads);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started successfully! Waiting for connections...");
            System.out.println("Press Ctrl+C to stop the server.");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                threadPool.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup
            DatabaseManager.getInstance().close();
            System.out.println("Server stopped.");
        }
    }
}