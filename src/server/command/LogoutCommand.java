package server.command;

import server.network.Request;
import server.network.Response;

public class LogoutCommand implements Command {
    @Override
    public Response execute(Request request) {
        return new Response(true, "До свидания!", null);
    }
}