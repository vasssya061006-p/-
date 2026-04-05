package server.model;

public class Teacher extends Employee {
    private static final long serialVersionUID = 1L;

    private String specialization;
    private int teachingHoursPerWeek;

    public Teacher() {
        super();
        this.role = "TEACHER";
    }

    public Teacher(int id, String login, String passwordHash, String fullName,
                   String position, String department, String specialization,
                   int teachingHoursPerWeek, boolean active) {
        super(id, login, passwordHash, fullName, "TEACHER", position, department, active);
        this.specialization = specialization;
        this.teachingHoursPerWeek = teachingHoursPerWeek;
    }

    public String getSpecialization() { return specialization; }
    public int getTeachingHoursPerWeek() { return teachingHoursPerWeek; }

    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setTeachingHoursPerWeek(int teachingHoursPerWeek) { this.teachingHoursPerWeek = teachingHoursPerWeek; }

    public double getWorkloadPercentage() {
        return (teachingHoursPerWeek / 36.0) * 100.0;
    }

    @Override
    public String toString() {
        return "Teacher{id=" + id + ", fullName='" + fullName + "', specialization='" + specialization + "'}";
    }
}
