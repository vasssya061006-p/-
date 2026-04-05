package server.network;

import server.command.Command;
import server.command.CommandFactory;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Request request = (Request) input.readObject();
                if (request == null) break;

                System.out.println("Получена команда: " + request.getCommand());

                Command command = CommandFactory.getCommand(request.getCommand());
                if (command == null) {
                    sendResponse(new Response(false, "Неизвестная команда", null));
                    continue;
                }

                Response response = command.execute(request);
                sendResponse(response);

                if ("LOGOUT".equals(request.getCommand())) break;
            }
        } catch (EOFException e) {
            System.out.println("Клиент отключился");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) {}
        }
    }

    private void sendResponse(Response response) throws IOException {
        output.writeObject(response);
        output.flush();
    }
}