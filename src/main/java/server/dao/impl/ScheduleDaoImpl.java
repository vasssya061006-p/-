package server.dao.impl;

import server.dao.ScheduleDao;
import server.model.Schedule;
import server.dao.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of ScheduleDao.
 */
public class ScheduleDaoImpl implements ScheduleDao {
    
    @Override
    public Schedule findById(int id) throws SQLException {
        String sql = "SELECT s.*, c.name as course_name, g.name as group_name, " +
                     "u.full_name as teacher_name FROM schedules s " +
                     "LEFT JOIN courses c ON s.course_id = c.id " +
                     "LEFT JOIN groups g ON s.group_id = g.id " +
                     "LEFT JOIN users u ON s.teacher_id = u.id " +
                     "WHERE s.id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSchedule(rs);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return null;
    }
    
    @Override
    public List<Schedule> findByGroupId(int groupId) throws SQLException {
        String sql = "SELECT s.*, c.name as course_name, g.name as group_name, " +
                     "u.full_name as teacher_name FROM schedules s " +
                     "LEFT JOIN courses c ON s.course_id = c.id " +
                     "LEFT JOIN groups g ON s.group_id = g.id " +
                     "LEFT JOIN users u ON s.teacher_id = u.id " +
                     "WHERE s.group_id = ? ORDER BY s.day_of_week, s.start_time";
        return executeQuery(sql, groupId);
    }
    
    @Override
    public List<Schedule> findByTeacherId(int teacherId) throws SQLException {
        String sql = "SELECT s.*, c.name as course_name, g.name as group_name, " +
                     "u.full_name as teacher_name FROM schedules s " +
                     "LEFT JOIN courses c ON s.course_id = c.id " +
                     "LEFT JOIN groups g ON s.group_id = g.id " +
                     "LEFT JOIN users u ON s.teacher_id = u.id " +
                     "WHERE s.teacher_id = ? ORDER BY s.day_of_week, s.start_time";
        return executeQuery(sql, teacherId);
    }
    
    @Override
    public List<Schedule> findByCourseId(int courseId) throws SQLException {
        String sql = "SELECT s.*, c.name as course_name, g.name as group_name, " +
                     "u.full_name as teacher_name FROM schedules s " +
                     "LEFT JOIN courses c ON s.course_id = c.id " +
                     "LEFT JOIN groups g ON s.group_id = g.id " +
                     "LEFT JOIN users u ON s.teacher_id = u.id " +
                     "WHERE s.course_id = ? ORDER BY s.day_of_week, s.start_time";
        return executeQuery(sql, courseId);
    }
    
    @Override
    public List<Schedule> findByRoom(String roomNumber) throws SQLException {
        String sql = "SELECT s.*, c.name as course_name, g.name as group_name, " +
                     "u.full_name as teacher_name FROM schedules s " +
                     "LEFT JOIN courses c ON s.course_id = c.id " +
                     "LEFT JOIN groups g ON s.group_id = g.id " +
                     "LEFT JOIN users u ON s.teacher_id = u.id " +
                     "WHERE s.room_number = ? ORDER BY s.day_of_week, s.start_time";
        return executeQuery(sql, roomNumber);
    }
    
    @Override
    public List<Schedule> findByDayOfWeek(String dayOfWeek) throws SQLException {
        String sql = "SELECT s.*, c.name as course_name, g.name as group_name, " +
                     "u.full_name as teacher_name FROM schedules s " +
                     "LEFT JOIN courses c ON s.course_id = c.id " +
                     "LEFT JOIN groups g ON s.group_id = g.id " +
                     "LEFT JOIN users u ON s.teacher_id = u.id " +
                     "WHERE s.day_of_week = ? ORDER BY s.start_time, s.room_number";
        return executeQuery(sql, dayOfWeek);
    }
    
    @Override
    public List<Schedule> findBySemester(String semester) throws SQLException {
        String sql = "SELECT s.*, c.name as course_name, g.name as group_name, " +
                     "u.full_name as teacher_name FROM schedules s " +
                     "LEFT JOIN courses c ON s.course_id = c.id " +
                     "LEFT JOIN groups g ON s.group_id = g.id " +
                     "LEFT JOIN users u ON s.teacher_id = u.id " +
                     "WHERE s.semester = ? ORDER BY s.day_of_week, s.start_time";
        return executeQuery(sql, semester);
    }
    
    @Override
    public boolean insert(Schedule schedule) throws SQLException {
        String sql = "INSERT INTO schedules (course_id, group_id, teacher_id, room_number, " +
                     "start_time, end_time, day_of_week, lesson_type, semester, " +
                     "course_name, group_name, teacher_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, schedule.getCourseId(), Types.INTEGER);
            stmt.setObject(2, schedule.getGroupId(), Types.INTEGER);
            stmt.setObject(3, schedule.getTeacherId(), Types.INTEGER);
            stmt.setString(4, schedule.getRoomNumber());
            stmt.setObject(5, schedule.getStartTime() != null ? Timestamp.valueOf(schedule.getStartTime()) : null, Types.TIMESTAMP);
            stmt.setObject(6, schedule.getEndTime() != null ? Timestamp.valueOf(schedule.getEndTime()) : null, Types.TIMESTAMP);
            stmt.setString(7, schedule.getDayOfWeek());
            stmt.setString(8, schedule.getLessonType());
            stmt.setString(9, schedule.getSemester());
            stmt.setString(10, schedule.getCourseName());
            stmt.setString(11, schedule.getGroupName());
            stmt.setString(12, schedule.getTeacherName());
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        schedule.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return false;
    }
    
    @Override
    public boolean update(Schedule schedule) throws SQLException {
        String sql = "UPDATE schedules SET course_id = ?, group_id = ?, teacher_id = ?, " +
                     "room_number = ?, start_time = ?, end_time = ?, day_of_week = ?, " +
                     "lesson_type = ?, semester = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, schedule.getCourseId(), Types.INTEGER);
            stmt.setObject(2, schedule.getGroupId(), Types.INTEGER);
            stmt.setObject(3, schedule.getTeacherId(), Types.INTEGER);
            stmt.setString(4, schedule.getRoomNumber());
            stmt.setObject(5, schedule.getStartTime() != null ? Timestamp.valueOf(schedule.getStartTime()) : null, Types.TIMESTAMP);
            stmt.setObject(6, schedule.getEndTime() != null ? Timestamp.valueOf(schedule.getEndTime()) : null, Types.TIMESTAMP);
            stmt.setString(7, schedule.getDayOfWeek());
            stmt.setString(8, schedule.getLessonType());
            stmt.setString(9, schedule.getSemester());
            stmt.setInt(10, schedule.getId());
            return stmt.executeUpdate() > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public boolean hasConflict(Schedule schedule) throws SQLException {
        String sql = "SELECT COUNT(*) as conflict_count FROM schedules WHERE id != ? AND (" +
                     "(room_number = ? AND day_of_week = ? AND start_time = ?) OR " +
                     "(teacher_id = ? AND day_of_week = ? AND start_time = ?) OR " +
                     "(group_id = ? AND day_of_week = ? AND start_time = ?))";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, schedule.getId());
            stmt.setString(2, schedule.getRoomNumber());
            stmt.setString(3, schedule.getDayOfWeek());
            stmt.setObject(4, schedule.getStartTime() != null ? Timestamp.valueOf(schedule.getStartTime()) : null, Types.TIMESTAMP);
            stmt.setObject(5, schedule.getTeacherId(), Types.INTEGER);
            stmt.setString(6, schedule.getDayOfWeek());
            stmt.setObject(7, schedule.getStartTime() != null ? Timestamp.valueOf(schedule.getStartTime()) : null, Types.TIMESTAMP);
            stmt.setObject(8, schedule.getGroupId(), Types.INTEGER);
            stmt.setString(9, schedule.getDayOfWeek());
            stmt.setObject(10, schedule.getStartTime() != null ? Timestamp.valueOf(schedule.getStartTime()) : null, Types.TIMESTAMP);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("conflict_count") > 0;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return false;
    }
    
    @Override
    public List<Schedule> findByRoomAndTime(String roomNumber, String dayOfWeek, String startTime) throws SQLException {
        String sql = "SELECT s.*, c.name as course_name, g.name as group_name, " +
                     "u.full_name as teacher_name FROM schedules s " +
                     "LEFT JOIN courses c ON s.course_id = c.id " +
                     "LEFT JOIN groups g ON s.group_id = g.id " +
                     "LEFT JOIN users u ON s.teacher_id = u.id " +
                     "WHERE s.room_number = ? AND s.day_of_week = ? AND CAST(s.start_time AS TIME) = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            stmt.setString(2, dayOfWeek);
            stmt.setString(3, startTime);
            return executeResultSet(stmt.executeQuery());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public List<Schedule> findByTeacherAndTime(int teacherId, String dayOfWeek, String startTime) throws SQLException {
        String sql = "SELECT s.*, c.name as course_name, g.name as group_name, " +
                     "u.full_name as teacher_name FROM schedules s " +
                     "LEFT JOIN courses c ON s.course_id = c.id " +
                     "LEFT JOIN groups g ON s.group_id = g.id " +
                     "LEFT JOIN users u ON s.teacher_id = u.id " +
                     "WHERE s.teacher_id = ? AND s.day_of_week = ? AND CAST(s.start_time AS TIME) = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teacherId);
            stmt.setString(2, dayOfWeek);
            stmt.setString(3, startTime);
            return executeResultSet(stmt.executeQuery());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public List<Schedule> findByGroupAndTime(int groupId, String dayOfWeek, String startTime) throws SQLException {
        String sql = "SELECT s.*, c.name as course_name, g.name as group_name, " +
                     "u.full_name as teacher_name FROM schedules s " +
                     "LEFT JOIN courses c ON s.course_id = c.id " +
                     "LEFT JOIN groups g ON s.group_id = g.id " +
                     "LEFT JOIN users u ON s.teacher_id = u.id " +
                     "WHERE s.group_id = ? AND s.day_of_week = ? AND CAST(s.start_time AS TIME) = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, dayOfWeek);
            stmt.setString(3, startTime);
            return executeResultSet(stmt.executeQuery());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    private List<Schedule> executeQuery(String sql, Object... params) throws SQLException {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return executeResultSet(stmt.executeQuery());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    private List<Schedule> executeResultSet(ResultSet rs) throws SQLException {
        List<Schedule> schedules = new ArrayList<>();
        while (rs.next()) {
            schedules.add(mapResultSetToSchedule(rs));
        }
        return schedules;
    }
    
    private Schedule mapResultSetToSchedule(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule();
        schedule.setId(rs.getInt("id"));
        schedule.setCourseId(rs.getObject("course_id", Integer.class));
        schedule.setCourseName(rs.getString("course_name"));
        schedule.setGroupId(rs.getObject("group_id", Integer.class));
        schedule.setGroupName(rs.getString("group_name"));
        schedule.setTeacherId(rs.getObject("teacher_id", Integer.class));
        schedule.setTeacherName(rs.getString("teacher_name"));
        schedule.setRoomNumber(rs.getString("room_number"));
        
        Timestamp startTs = rs.getTimestamp("start_time");
        if (startTs != null) {
            schedule.setStartTime(startTs.toLocalDateTime());
        }
        
        Timestamp endTs = rs.getTimestamp("end_time");
        if (endTs != null) {
            schedule.setEndTime(endTs.toLocalDateTime());
        }
        
        schedule.setDayOfWeek(rs.getString("day_of_week"));
        schedule.setLessonType(rs.getString("lesson_type"));
        schedule.setSemester(rs.getString("semester"));
        return schedule;
    }
}
