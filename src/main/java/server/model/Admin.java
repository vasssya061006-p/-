package server.model;

public class Admin extends Employee {
    private static final long serialVersionUID = 1L;

    private String accessLevel;

    public Admin() {
        super();
        this.role = "ADMIN";
        this.accessLevel = "FULL";
    }

    public Admin(int id, String login, String passwordHash, String fullName,
                 String position, String department, String accessLevel, boolean active) {
        super(id, login, passwordHash, fullName, "ADMIN", position, department, active);
        this.accessLevel = accessLevel != null ? accessLevel : "FULL";
    }

    public String getAccessLevel() { return accessLevel; }

    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    @Override
    public String toString() {
        return "Admin{id=" + id + ", fullName='" + fullName + "', accessLevel='" + accessLevel + "'}";
    }
}
