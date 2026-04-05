package server.model;

import java.time.LocalDateTime;

public class Schedule extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Integer courseId;
    private String courseName;
    private Integer groupId;
    private String groupName;
    private Integer teacherId;
    private String teacherName;
    private String roomNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String dayOfWeek;
    private String lessonType;
    private String semester;

    public Schedule() {
        super();
    }

    public Schedule(int id, Integer courseId, String courseName, Integer groupId, String groupName,
                    Integer teacherId, String teacherName, String roomNumber,
                    LocalDateTime startTime, LocalDateTime endTime, String dayOfWeek,
                    String lessonType, String semester) {
        super(id);
        this.courseId = courseId;
        this.courseName = courseName;
        this.groupId = groupId;
        this.groupName = groupName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.roomNumber = roomNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dayOfWeek = dayOfWeek;
        this.lessonType = lessonType;
        this.semester = semester;
    }

    public Integer getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public Integer getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public Integer getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
    public String getRoomNumber() { return roomNumber; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getLessonType() { return lessonType; }
    public String getSemester() { return semester; }

    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setLessonType(String lessonType) { this.lessonType = lessonType; }
    public void setSemester(String semester) { this.semester = semester; }

    public boolean hasConflictWith(Schedule other) {
        if (other == null) return false;

        boolean sameTimeSlot = this.startTime.equals(other.startTime) &&
                               this.endTime.equals(other.endTime);
        if (!sameTimeSlot) return false;

        boolean sameRoom = this.roomNumber != null && this.roomNumber.equals(other.roomNumber);
        boolean sameTeacher = this.teacherId != null && this.teacherId.equals(other.teacherId);
        boolean sameGroup = this.groupId != null && this.groupId.equals(other.groupId);

        return sameRoom || sameTeacher || sameGroup;
    }

    @Override
    public String toString() {
        return "Schedule{id=" + id + ", course='" + courseName + "', room='" + roomNumber +
               "', day='" + dayOfWeek + "', type='" + lessonType + "'}";
    }
}
