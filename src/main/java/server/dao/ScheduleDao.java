package server.dao;

import server.model.Schedule;
import java.sql.SQLException;
import java.util.List;

public interface ScheduleDao {

    Schedule findById(int id) throws SQLException;

    List<Schedule> findByGroupId(int groupId) throws SQLException;

    List<Schedule> findByTeacherId(int teacherId) throws SQLException;

    List<Schedule> findByCourseId(int courseId) throws SQLException;

    List<Schedule> findByRoom(String roomNumber) throws SQLException;

    List<Schedule> findByDayOfWeek(String dayOfWeek) throws SQLException;

    List<Schedule> findBySemester(String semester) throws SQLException;

    boolean insert(Schedule schedule) throws SQLException;

    boolean update(Schedule schedule) throws SQLException;

    boolean delete(int id) throws SQLException;

    boolean hasConflict(Schedule schedule) throws SQLException;

    List<Schedule> findByRoomAndTime(String roomNumber, String dayOfWeek, String startTime) throws SQLException;

    List<Schedule> findByTeacherAndTime(int teacherId, String dayOfWeek, String startTime) throws SQLException;

    List<Schedule> findByGroupAndTime(int groupId, String dayOfWeek, String startTime) throws SQLException;
}
