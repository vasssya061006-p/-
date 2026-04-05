package server.dao.impl;

import server.dao.CourseDao;
import server.model.Course;
import server.dao.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDaoImpl implements CourseDao {

    @Override
    public Course findById(int id) throws SQLException {
        String sql = "SELECT * FROM courses WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCourse(rs);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return null;
    }

    @Override
    public List<Course> findAll() throws SQLException {
        String sql = "SELECT * FROM courses ORDER BY name";
        List<Course> courses = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                courses.add(mapResultSetToCourse(rs));
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return courses;
    }

    @Override
    public List<Course> findByTeacherId(int teacherId) throws SQLException {
        String sql = "SELECT * FROM courses WHERE teacher_id = ? ORDER BY name";
        return executeQuery(sql, teacherId);
    }

    @Override
    public List<Course> findByDepartment(String department) throws SQLException {
        String sql = "SELECT * FROM courses WHERE department = ? ORDER BY name";
        return executeQuery(sql, department);
    }

    @Override
    public boolean insert(Course course) throws SQLException {
        String sql = "INSERT INTO courses (name, description, credits, teacher_id, department) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, course.getName());
            stmt.setString(2, course.getDescription());
            stmt.setInt(3, course.getCredits());
            stmt.setObject(4, course.getTeacherId(), Types.INTEGER);
            stmt.setString(5, course.getDepartment());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        course.setId(keys.getInt(1));
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
    public boolean update(Course course) throws SQLException {
        String sql = "UPDATE courses SET name = ?, description = ?, credits = ?, " +
                     "teacher_id = ?, department = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, course.getName());
            stmt.setString(2, course.getDescription());
            stmt.setInt(3, course.getCredits());
            stmt.setObject(4, course.getTeacherId(), Types.INTEGER);
            stmt.setString(5, course.getDepartment());
            stmt.setInt(6, course.getId());
            return stmt.executeUpdate() > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }

    @Override
    public int getStudentCount(int courseId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT student_id) as student_count FROM academic_records WHERE course_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("student_count");
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return 0;
    }

    @Override
    public double getAverageGrade(int courseId) throws SQLException {
        String sql = "SELECT AVG(grade_value) as avg_grade FROM academic_records WHERE course_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, courseId);
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

    private List<Course> executeQuery(String sql, Object param) throws SQLException {
        List<Course> courses = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, param);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapResultSetToCourse(rs));
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return courses;
    }

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getInt("id"));
        course.setName(rs.getString("name"));
        course.setDescription(rs.getString("description"));
        course.setCredits(rs.getInt("credits"));
        course.setTeacherId(rs.getObject("teacher_id", Integer.class));
        course.setDepartment(rs.getString("department"));
        return course;
    }
}
