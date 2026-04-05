package server.command;

import server.network.Request;
import server.network.Response;
import server.model.User;
import server.service.AuthenticationException;
import server.service.EducationService;

public class LoginCommand implements Command {
    private EducationService educationService = EducationService.getInstance();

    @Override
    public Response execute(Request request) {
        try {
            Object[] credentials = (Object[]) request.getData();
            String login = (String) credentials[0];
            String password = (String) credentials[1];

            User user = educationService.authenticateUser(login, password);

            if (user == null) {
                return new Response(false, "Invalid login or password", null);
            }

            request.setUserId(user.getId());

            return new Response(true, "Login successful", user);

        } catch (AuthenticationException e) {
            return new Response(false, "Authentication error: " + e.getMessage(), null);
        } catch (Exception e) {
            return new Response(false, "Server error: " + e.getMessage(), null);
        }
    }
}
