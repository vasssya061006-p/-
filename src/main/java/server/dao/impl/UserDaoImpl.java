package server.dao.impl;

import server.dao.UserDao;
import server.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of UserDao.
 */
public class UserDaoImpl implements UserDao {
    
    @Override
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = server.dao.DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return null;
    }
    
    @Override
    public User findByLogin(String login) throws SQLException {
        String sql = "SELECT * FROM users WHERE login = ? AND is_active = true";
        try (Connection conn = server.dao.DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return null;
    }
    
    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY role, full_name";
        List<User> users = new ArrayList<>();
        try (Connection conn = server.dao.DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return users;
    }
    
    @Override
    public List<User> findByRole(String role) throws SQLException {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (Connection conn = server.dao.DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return users;
    }
    
    @Override
    public boolean insert(User user) throws SQLException {
        String sql = "INSERT INTO users (login, password_hash, full_name, role, group_id, is_active, " +
                     "student_group, year_of_study, student_id_number, position, department, specialization, " +
                     "teaching_hours_per_week, access_level) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = server.dao.DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole());
            stmt.setObject(5, user.getGroupId(), Types.INTEGER);
            stmt.setBoolean(6, user.isActive());
            
            // Role-specific fields
            if (user instanceof Student) {
                Student s = (Student) user;
                stmt.setString(7, s.getGroup());
                stmt.setInt(8, s.getYearOfStudy());
                stmt.setString(9, s.getStudentIdNumber());
            } else {
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.INTEGER);
                stmt.setNull(9, Types.VARCHAR);
            }
            
            if (user instanceof Employee) {
                Employee e = (Employee) user;
                stmt.setString(10, e.getPosition());
                stmt.setString(11, e.getDepartment());
                if (user instanceof Teacher) {
                    Teacher t = (Teacher) user;
                    stmt.setString(12, t.getSpecialization());
                    stmt.setInt(13, t.getTeachingHoursPerWeek());
                } else {
                    stmt.setNull(12, Types.VARCHAR);
                    stmt.setNull(13, Types.INTEGER);
                }
            } else {
                stmt.setNull(10, Types.VARCHAR);
                stmt.setNull(11, Types.VARCHAR);
                stmt.setNull(12, Types.VARCHAR);
                stmt.setNull(13, Types.INTEGER);
            }
            
            if (user instanceof Admin) {
                stmt.setString(14, ((Admin) user).getAccessLevel());
            } else {
                stmt.setNull(14, Types.VARCHAR);
            }
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
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
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET login = ?, password_hash = ?, full_name = ?, role = ?, " +
                     "group_id = ?, is_active = ?, student_group = ?, year_of_study = ?, " +
                     "student_id_number = ?, position = ?, department = ?, specialization = ?, " +
                     "teaching_hours_per_week = ?, access_level = ? WHERE id = ?";
        try (Connection conn = server.dao.DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getRole());
            stmt.setObject(5, user.getGroupId(), Types.INTEGER);
            stmt.setBoolean(6, user.isActive());
            
            if (user instanceof Student) {
                Student s = (Student) user;
                stmt.setString(7, s.getGroup());
                stmt.setInt(8, s.getYearOfStudy());
                stmt.setString(9, s.getStudentIdNumber());
            } else {
                stmt.setNull(7, Types.VARCHAR);
                stmt.setNull(8, Types.INTEGER);
                stmt.setNull(9, Types.VARCHAR);
            }
            
            if (user instanceof Employee) {
                Employee e = (Employee) user;
                stmt.setString(10, e.getPosition());
                stmt.setString(11, e.getDepartment());
                if (user instanceof Teacher) {
                    Teacher t = (Teacher) user;
                    stmt.setString(12, t.getSpecialization());
                    stmt.setInt(13, t.getTeachingHoursPerWeek());
                } else {
                    stmt.setNull(12, Types.VARCHAR);
                    stmt.setNull(13, Types.INTEGER);
                }
            } else {
                stmt.setNull(10, Types.VARCHAR);
                stmt.setNull(11, Types.VARCHAR);
                stmt.setNull(12, Types.VARCHAR);
                stmt.setNull(13, Types.INTEGER);
            }
            
            if (user instanceof Admin) {
                stmt.setString(14, ((Admin) user).getAccessLevel());
            } else {
                stmt.setNull(14, Types.VARCHAR);
            }
            
            stmt.setInt(15, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = server.dao.DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public boolean updatePassword(int userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = server.dao.DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public List<User> findByGroupId(Integer groupId) throws SQLException {
        String sql = "SELECT * FROM users WHERE group_id = ? AND role = 'STUDENT' ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (Connection conn = server.dao.DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, groupId, Types.INTEGER);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return users;
    }
    
    /**
     * Maps a ResultSet row to the appropriate User subclass.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        int id = rs.getInt("id");
        String login = rs.getString("login");
        String passwordHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        boolean isActive = rs.getBoolean("is_active");
        Integer groupId = rs.getObject("group_id", Integer.class);
        
        User user;
        switch (role) {
            case "STUDENT":
                Student student = new Student();
                student.setId(id);
                student.setLogin(login);
                student.setPasswordHash(passwordHash);
                student.setFullName(fullName);
                student.setActive(isActive);
                student.setGroupId(groupId);
                student.setGroup(rs.getString("student_group"));
                student.setYearOfStudy(rs.getInt("year_of_study"));
                student.setStudentIdNumber(rs.getString("student_id_number"));
                user = student;
                break;
                
            case "TEACHER":
                Teacher teacher = new Teacher();
                teacher.setId(id);
                teacher.setLogin(login);
                teacher.setPasswordHash(passwordHash);
                teacher.setFullName(fullName);
                teacher.setActive(isActive);
                teacher.setGroupId(groupId);
                teacher.setPosition(rs.getString("position"));
                teacher.setDepartment(rs.getString("department"));
                teacher.setSpecialization(rs.getString("specialization"));
                teacher.setTeachingHoursPerWeek(rs.getInt("teaching_hours_per_week"));
                user = teacher;
                break;
                
            case "ADMIN":
                Admin admin = new Admin();
                admin.setId(id);
                admin.setLogin(login);
                admin.setPasswordHash(passwordHash);
                admin.setFullName(fullName);
                admin.setActive(isActive);
                admin.setGroupId(groupId);
                admin.setPosition(rs.getString("position"));
                admin.setDepartment(rs.getString("department"));
                admin.setAccessLevel(rs.getString("access_level"));
                user = admin;
                break;
                
            default:
                throw new SQLException("Unknown user role: " + role);
        }
        
        return user;
    }
}
