package server.command;

import server.network.Request;
import server.network.Response;

public class EchoCommand implements Command {
    @Override
    public Response execute(Request request) {
        String message = (String) request.getData();
        return new Response(true, "OK", "Эхо: " + message);
    }
}