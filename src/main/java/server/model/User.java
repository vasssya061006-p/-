package server.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Abstract base class for all user types in the education system.
 * Extends BaseEntity to form part of the class hierarchy.
 */
public abstract class User extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    protected String login;
    protected String passwordHash;
    protected String fullName;
    protected String role;
    protected boolean active;
    protected Integer groupId;
    
    public User() {
        super();
    }
    
    public User(int id, String login, String passwordHash, String fullName, String role, boolean active) {
        super(id);
        this.login = login;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }
    
    /**
     * Hashes a password using SHA-256 algorithm.
     * @param password plain text password
     * @return hashed password as hex string
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    // Getters
    public String getLogin() { return login; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public Integer getGroupId() { return groupId; }
    
    // Setters
    public void setLogin(String login) { this.login = login; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setActive(boolean active) { this.active = active; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    
    /**
     * Returns user display name for UI purposes.
     */
    public String getDisplayName() {
        return fullName + " (" + role + ")";
    }
    
    @Override
    public String toString() {
        return "User{id=" + id + ", login='" + login + "', fullName='" + fullName + "', role='" + role + "'}";
    }
}