package server.command;

import server.network.Request;
import server.network.Response;
import server.service.AuthService;
import server.model.User;

public class LoginCommand implements Command {
    private AuthService authService = new AuthService();

    @Override
    public Response execute(Request request) {
        try {
            Object[] credentials = (Object[]) request.getData();
            String login = (String) credentials[0];
            String password = (String) credentials[1];

            User user = authService.login(login, password);

            if (user == null) {
                return new Response(false, "Неверный логин или пароль", null);
            }

            // Запоминаем ID пользователя в запросе (для сессии)
            request.setUserId(user.getId());

            return new Response(true, "Вход выполнен успешно", user);

        } catch (Exception e) {
            return new Response(false, "Ошибка сервера: " + e.getMessage(), null);
        }
    }
}