package server.command;

import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private static Map<String, Command> commands = new HashMap<>();

    static {
        commands.put("ECHO", new EchoCommand());
        commands.put("LOGOUT", new LogoutCommand());
        commands.put("LOGIN", new LoginCommand());
        commands.put("GET_GRADES", new GetGradesCommand());
        commands.put("ADD_GRADE", new AddGradeCommand());
    }

    public static Command getCommand(String commandName) {
        return commands.get(commandName);
    }
}