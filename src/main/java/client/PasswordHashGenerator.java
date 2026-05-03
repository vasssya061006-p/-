package client;

import server.model.User;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        String password = "teacher123";
        String hash = User.hashPassword(password);
        System.out.println("Пароль: " + password);
        System.out.println("Хеш: " + hash);
        System.out.println("Длина: " + hash.length());
    }
}