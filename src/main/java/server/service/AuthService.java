package server.service;

import server.factory.UserFactory;
import server.model.User;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class AuthService {
    private static Map<String, User> users = new HashMap<>();

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

        User safeUser = UserFactory.createUserFromData(
            user.getRole(), user.getId(), user.getLogin(),
            null, user.getFullName(), user.isActive()
        );

        return safeUser;
    }
}