package server.model;

import java.io.Serializable;

public class Grade implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer studentId;
    private String studentName;
    private String courseName;
    private Integer gradeValue;
    private String gradeType;
    private String date;

    public Grade() {}

    public Grade(Integer id, Integer studentId, String studentName, String courseName,
                 Integer gradeValue, String gradeType, String date) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseName = courseName;
        this.gradeValue = gradeValue;
        this.gradeType = gradeType;
        this.date = date;
    }

    public Integer getId() { return id; }
    public Integer getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getCourseName() { return courseName; }
    public Integer getGradeValue() { return gradeValue; }
    public String getGradeType() { return gradeType; }
    public String getDate() { return date; }

    public void setId(Integer id) { this.id = id; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setGradeValue(Integer gradeValue) { this.gradeValue = gradeValue; }
    public void setGradeType(String gradeType) { this.gradeType = gradeType; }
    public void setDate(String date) { this.date = date; }
}
