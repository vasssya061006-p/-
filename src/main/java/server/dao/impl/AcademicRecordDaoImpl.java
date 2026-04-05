package server.dao.impl;

import server.dao.AcademicRecordDao;
import server.model.AcademicRecord;
import server.dao.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of AcademicRecordDao.
 */
public class AcademicRecordDaoImpl implements AcademicRecordDao {
    
    @Override
    public AcademicRecord findById(int id) throws SQLException {
        String sql = "SELECT * FROM academic_records WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRecord(rs);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return null;
    }
    
    @Override
    public List<AcademicRecord> findByStudentId(int studentId) throws SQLException {
        String sql = "SELECT ar.*, c.name as course_name FROM academic_records ar " +
                     "LEFT JOIN courses c ON ar.course_id = c.id " +
                     "WHERE ar.student_id = ? ORDER BY ar.grade_date DESC";
        return executeQuery(sql, studentId);
    }
    
    @Override
    public List<AcademicRecord> findByGroupId(int groupId) throws SQLException {
        String sql = "SELECT ar.*, c.name as course_name FROM academic_records ar " +
                     "INNER JOIN users u ON ar.student_id = u.id " +
                     "LEFT JOIN courses c ON ar.course_id = c.id " +
                     "WHERE u.group_id = ? ORDER BY u.full_name, ar.grade_date DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            return executeResultSet(stmt.executeQuery());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public List<AcademicRecord> findByCourseId(int courseId) throws SQLException {
        String sql = "SELECT ar.*, c.name as course_name FROM academic_records ar " +
                     "LEFT JOIN courses c ON ar.course_id = c.id " +
                     "WHERE ar.course_id = ? ORDER BY ar.grade_date DESC";
        return executeQuery(sql, courseId);
    }
    
    @Override
    public List<AcademicRecord> findBySemester(String semester) throws SQLException {
        String sql = "SELECT ar.*, c.name as course_name FROM academic_records ar " +
                     "LEFT JOIN courses c ON ar.course_id = c.id " +
                     "WHERE ar.semester = ? ORDER BY ar.student_id, ar.grade_date DESC";
        return executeQuery(sql, semester);
    }
    
    @Override
    public List<AcademicRecord> findByStudentAndCourse(int studentId, int courseId) throws SQLException {
        String sql = "SELECT ar.*, c.name as course_name FROM academic_records ar " +
                     "LEFT JOIN courses c ON ar.course_id = c.id " +
                     "WHERE ar.student_id = ? AND ar.course_id = ? ORDER BY ar.grade_date DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, courseId);
            return executeResultSet(stmt.executeQuery());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public boolean insert(AcademicRecord record) throws SQLException {
        String sql = "INSERT INTO academic_records (student_id, course_id, course_name, student_name, " +
                     "grade_value, grade_type, grade_date, attendance_status, semester, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, record.getStudentId(), Types.INTEGER);
            stmt.setObject(2, record.getCourseId(), Types.INTEGER);
            stmt.setString(3, record.getCourseName());
            stmt.setString(4, record.getStudentName());
            stmt.setDouble(5, record.getGradeValue());
            stmt.setString(6, record.getGradeType());
            stmt.setObject(7, record.getGradeDate() != null ? Date.valueOf(record.getGradeDate()) : null, Types.DATE);
            stmt.setString(8, record.getAttendanceStatus());
            stmt.setString(9, record.getSemester());
            stmt.setString(10, record.getNotes());
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        record.setId(keys.getInt(1));
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
    public boolean update(AcademicRecord record) throws SQLException {
        String sql = "UPDATE academic_records SET grade_value = ?, grade_type = ?, " +
                     "attendance_status = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, record.getGradeValue());
            stmt.setString(2, record.getGradeType());
            stmt.setString(3, record.getAttendanceStatus());
            stmt.setString(4, record.getNotes());
            stmt.setInt(5, record.getId());
            return stmt.executeUpdate() > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM academic_records WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public double getAverageGradeByStudent(int studentId) throws SQLException {
        String sql = "SELECT AVG(grade_value) as avg_grade FROM academic_records WHERE student_id = ?";
        return executeAverage(sql, studentId);
    }
    
    @Override
    public double getAverageGradeByCourse(int courseId) throws SQLException {
        String sql = "SELECT AVG(grade_value) as avg_grade FROM academic_records WHERE course_id = ?";
        return executeAverage(sql, courseId);
    }
    
    @Override
    public double getAverageGradeByGroup(int groupId) throws SQLException {
        String sql = "SELECT AVG(ar.grade_value) as avg_grade FROM academic_records ar " +
                     "INNER JOIN users u ON ar.student_id = u.id WHERE u.group_id = ?";
        return executeAverage(sql, groupId);
    }
    
    @Override
    public int countAbsencesByStudent(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) as absence_count FROM academic_records " +
                     "WHERE student_id = ? AND attendance_status = 'absent'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("absence_count");
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return 0;
    }
    
    @Override
    public List<AcademicRecord> getFailingGrades(double threshold) throws SQLException {
        String sql = "SELECT ar.*, c.name as course_name FROM academic_records ar " +
                     "LEFT JOIN courses c ON ar.course_id = c.id " +
                     "WHERE ar.grade_value < ? AND ar.grade_value > 0 " +
                     "ORDER BY ar.grade_value ASC, ar.student_id";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, threshold);
            return executeResultSet(stmt.executeQuery());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    private List<AcademicRecord> executeQuery(String sql, Object... params) throws SQLException {
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
    
    private List<AcademicRecord> executeResultSet(ResultSet rs) throws SQLException {
        List<AcademicRecord> records = new ArrayList<>();
        while (rs.next()) {
            records.add(mapResultSetToRecord(rs));
        }
        return records;
    }
    
    private double executeAverage(String sql, Object param) throws SQLException {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_grade");
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return 0.0;
    }
    
    private AcademicRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        AcademicRecord record = new AcademicRecord();
        record.setId(rs.getInt("id"));
        record.setStudentId(rs.getObject("student_id", Integer.class));
        record.setCourseId(rs.getObject("course_id", Integer.class));
        record.setCourseName(rs.getString("course_name"));
        record.setStudentName(rs.getString("student_name"));
        record.setGradeValue(rs.getDouble("grade_value"));
        record.setGradeType(rs.getString("grade_type"));
        
        Date gradeDate = rs.getDate("grade_date");
        if (gradeDate != null) {
            record.setGradeDate(gradeDate.toLocalDate());
        }
        
        record.setAttendanceStatus(rs.getString("attendance_status"));
        record.setSemester(rs.getString("semester"));
        record.setNotes(rs.getString("notes"));
        return record;
    }
}
