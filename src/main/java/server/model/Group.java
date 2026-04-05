package server.model;

public class Group extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private String name;
    private Integer curatorId;
    private int yearOfStudy;
    private int studentCount;

    public Group() {
        super();
    }

    public Group(int id, String name, Integer curatorId, int yearOfStudy, int studentCount) {
        super(id);
        this.name = name;
        this.curatorId = curatorId;
        this.yearOfStudy = yearOfStudy;
        this.studentCount = studentCount;
    }

    public String getName() { return name; }
    public Integer getCuratorId() { return curatorId; }
    public int getYearOfStudy() { return yearOfStudy; }
    public int getStudentCount() { return studentCount; }

    public void setName(String name) { this.name = name; }
    public void setCuratorId(Integer curatorId) { this.curatorId = curatorId; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    @Override
    public String toString() {
        return "Group{id=" + id + ", name='" + name + "', year=" + yearOfStudy + "}";
    }
}
