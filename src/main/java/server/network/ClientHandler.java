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

                try {
                    Command command = CommandFactory.getCommand(request.getCommand());
                    Response response = command.execute(request);
                    sendResponse(response);
                    System.out.println("Sent response: " + (response.isSuccess() ? "SUCCESS" : "FAILED") +
                            " - " + response.getMessage());
                } catch (IllegalArgumentException e) {
                    System.err.println("Unknown command: " + request.getCommand());
                    sendResponse(new Response(false, "Unknown command: " + request.getCommand(), null));
                }

                if ("LOGOUT".equals(request.getCommand())) {
                    System.out.println("Client logged out, closing connection");
                    break;
                }
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
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