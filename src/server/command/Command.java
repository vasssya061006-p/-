package server.command;

import server.network.Request;
import server.network.Response;

public interface Command {
    Response execute(Request request);
}