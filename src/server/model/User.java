package server.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String login;
    private String passwordHash;
    private String fullName;
    private String role;
    private boolean active;

    public User() {}

    public User(Integer id, String login, String passwordHash, String fullName, String role, boolean active) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    // Геттеры
    public Integer getId() { return id; }
    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }

    // Сеттеры
    public void setId(Integer id) { this.id = id; }
    public void setLogin(String login) { this.login = login; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }
}