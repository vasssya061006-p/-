package server.service;

import server.model.User;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static Map<String, User> users = new HashMap<>();

    // Этот метод нужно будет вызывать при регистрации
    public void registerUser(User user) {
        users.put(user.getLogin(), user);
    }

    public User login(String login, String password) {
        User user = users.get(login);

        if (user == null) {
            return null;
        }

        if (!user.isActive()) {
            return null;
        }

        if (!user.getPasswordHash().equals(password)) {
            return null;
        }

        return new User(user.getId(), user.getLogin(), null,
                user.getFullName(), user.getRole(), user.isActive());
    }
}