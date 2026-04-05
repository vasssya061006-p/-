package server.model;

import java.time.LocalDateTime;

/**
 * Represents a specific lesson/scheduled class session.
 */
public class Lesson extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
    private Integer courseId;
    private Integer groupId;
    private String roomNumber;
    private LocalDateTime lessonDate;
    private String lessonType; // lecture, practice, lab
    private Integer teacherId;
    
    public Lesson() {
        super();
    }
    
    public Lesson(int id, Integer courseId, Integer groupId, String roomNumber,
                  LocalDateTime lessonDate, String lessonType, Integer teacherId) {
        super(id);
        this.courseId = courseId;
        this.groupId = groupId;
        this.roomNumber = roomNumber;
        this.lessonDate = lessonDate;
        this.lessonType = lessonType;
        this.teacherId = teacherId;
    }
    
    // Getters
    public Integer getCourseId() { return courseId; }
    public Integer getGroupId() { return groupId; }
    public String getRoomNumber() { return roomNumber; }
    public LocalDateTime getLessonDate() { return lessonDate; }
    public String getLessonType() { return lessonType; }
    public Integer getTeacherId() { return teacherId; }
    
    // Setters
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setLessonDate(LocalDateTime lessonDate) { this.lessonDate = lessonDate; }
    public void setLessonType(String lessonType) { this.lessonType = lessonType; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
    
    @Override
    public String toString() {
        return "Lesson{id=" + id + ", courseId=" + courseId + ", date=" + lessonDate + ", type='" + lessonType + "'}";
    }
}
