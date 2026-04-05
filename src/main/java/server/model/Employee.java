package server.model;

public abstract class Employee extends User {
    private static final long serialVersionUID = 1L;

    protected String position;
    protected String department;
    protected double salary;

    public Employee() {
        super();
    }

    public Employee(int id, String login, String passwordHash, String fullName,
                    String role, String position, String department, boolean active) {
        super(id, login, passwordHash, fullName, role, active);
        this.position = position;
        this.department = department;
    }

    public String getPosition() { return position; }
    public String getDepartment() { return department; }
    public double getSalary() { return salary; }

    public void setPosition(String position) { this.position = position; }
    public void setDepartment(String department) { this.department = department; }
    public void setSalary(double salary) { this.salary = salary; }

    @Override
    public String toString() {
        return "Employee{id=" + id + ", fullName='" + fullName + "', role='" + role + "', position='" + position + "'}";
    }
}
