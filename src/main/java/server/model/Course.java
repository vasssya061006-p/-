package server.model;

public class Course extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private int credits;
    private Integer teacherId;
    private String department;

    public Course() {
        super();
    }

    public Course(int id, String name, String description, int credits, Integer teacherId, String department) {
        super(id);
        this.name = name;
        this.description = description;
        this.credits = credits;
        this.teacherId = teacherId;
        this.department = department;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCredits() { return credits; }
    public Integer getTeacherId() { return teacherId; }
    public String getDepartment() { return department; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCredits(int credits) { this.credits = credits; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
    public void setDepartment(String department) { this.department = department; }

    @Override
    public String toString() {
        return "Course{id=" + id + ", name='" + name + "', credits=" + credits + "}";
    }
}
