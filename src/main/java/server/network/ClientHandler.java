package server.network;

import server.command.Command;
import server.command.CommandFactory;

import java.io.*;
import java.net.Socket;

/**
 * Handles a single client connection in a separate thread.
 * Reads Request objects, executes commands, and returns Response objects.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Client connected: " + socket.getInetAddress());
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Request request = (Request) input.readObject();
                if (request == null) {
                    System.out.println("Received null request, closing connection");
                    break;
                }

                System.out.println("Received command: " + request.getCommand() + 
                                 " from user " + request.getUserId());

                Command command = CommandFactory.getCommand(request.getCommand());
                if (command == null) {
                    sendResponse(new Response(false, "Unknown command: " + request.getCommand(), null));
                    continue;
                }

                Response response = command.execute(request);
                sendResponse(response);

                // Log response status
                System.out.println("Sent response: " + (response.isSuccess() ? "SUCCESS" : "FAILED") +
                                 " - " + response.getMessage());

                // Disconnect on logout
                if ("LOGOUT".equals(request.getCommand())) {
                    System.out.println("Client logged out, closing connection");
                    break;
                }
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected unexpectedly: " + socket.getInetAddress());
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found during deserialization: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO error with client " + socket.getInetAddress() + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error handling client request: " + e.getMessage());
            e.printStackTrace();
            try {
                sendResponse(new Response(false, "Server error: " + e.getMessage(), null));
            } catch (IOException ioException) {
                System.err.println("Failed to send error response: " + ioException.getMessage());
            }
        } finally {
            closeConnection();
        }
    }

    private void sendResponse(Response response) throws IOException {
        if (output != null && !socket.isClosed()) {
            output.writeObject(response);
            output.flush();
        }
    }
    
    private void closeConnection() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Client connection closed: " + socket.getInetAddress());
            }
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}