package server.model;

import java.time.LocalDate;

public class AcademicRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Integer studentId;
    private Integer courseId;
    private String courseName;
    private String studentName;
    private double gradeValue;
    private String gradeType;
    private LocalDate gradeDate;
    private String attendanceStatus;
    private String semester;
    private String notes;

    public AcademicRecord() {
        super();
    }

    public AcademicRecord(int id, Integer studentId, Integer courseId, String courseName,
                          String studentName, double gradeValue, String gradeType,
                          LocalDate gradeDate, String attendanceStatus, String semester, String notes) {
        super(id);
        this.studentId = studentId;
        this.courseId = courseId;
        this.courseName = courseName;
        this.studentName = studentName;
        this.gradeValue = gradeValue;
        this.gradeType = gradeType;
        this.gradeDate = gradeDate;
        this.attendanceStatus = attendanceStatus;
        this.semester = semester;
        this.notes = notes;
    }

    public Integer getStudentId() { return studentId; }
    public Integer getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public String getStudentName() { return studentName; }
    public double getGradeValue() { return gradeValue; }
    public String getGradeType() { return gradeType; }
    public LocalDate getGradeDate() { return gradeDate; }
    public String getAttendanceStatus() { return attendanceStatus; }
    public String getSemester() { return semester; }
    public String getNotes() { return notes; }

    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setGradeValue(double gradeValue) { this.gradeValue = gradeValue; }
    public void setGradeType(String gradeType) { this.gradeType = gradeType; }
    public void setGradeDate(LocalDate gradeDate) { this.gradeDate = gradeDate; }
    public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isPassing() {
        return gradeValue >= 3.0;
    }

    public boolean isAbsent() {
        return "absent".equalsIgnoreCase(attendanceStatus);
    }

    @Override
    public String toString() {
        return "AcademicRecord{id=" + id + ", studentId=" + studentId +
               ", course='" + courseName + "', grade=" + gradeValue + "}";
    }
}
