package server.model;

public class Student extends User {
    private static final long serialVersionUID = 1L;

    private String group;
    private int yearOfStudy;
    private String studentIdNumber;

    public Student() {
        super();
        this.role = "STUDENT";
    }

    public Student(int id, String login, String passwordHash, String fullName,
                   String group, int yearOfStudy, String studentIdNumber, boolean active) {
        super(id, login, passwordHash, fullName, "STUDENT", active);
        this.group = group;
        this.yearOfStudy = yearOfStudy;
        this.studentIdNumber = studentIdNumber;
    }

    public String getGroup() { return group; }
    public int getYearOfStudy() { return yearOfStudy; }
    public String getStudentIdNumber() { return studentIdNumber; }

    public void setGroup(String group) { this.group = group; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    public void setStudentIdNumber(String studentIdNumber) { this.studentIdNumber = studentIdNumber; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", fullName='" + fullName + "', group='" + group + "'}";
    }
}
