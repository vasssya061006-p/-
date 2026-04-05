package server.dao.impl;

import server.dao.GroupDao;
import server.model.Group;
import server.dao.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of GroupDao.
 */
public class GroupDaoImpl implements GroupDao {
    
    @Override
    public Group findById(int id) throws SQLException {
        String sql = "SELECT * FROM groups WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToGroup(rs);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return null;
    }
    
    @Override
    public List<Group> findAll() throws SQLException {
        String sql = "SELECT * FROM groups ORDER BY name";
        List<Group> groups = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                groups.add(mapResultSetToGroup(rs));
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return groups;
    }
    
    @Override
    public List<Group> findByCuratorId(int curatorId) throws SQLException {
        String sql = "SELECT * FROM groups WHERE curator_id = ? ORDER BY name";
        List<Group> groups = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, curatorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    groups.add(mapResultSetToGroup(rs));
                }
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
        return groups;
    }
    
    @Override
    public boolean insert(Group group) throws SQLException {
        String sql = "INSERT INTO groups (name, curator_id, year_of_study, student_count) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, group.getName());
            stmt.setObject(2, group.getCuratorId(), Types.INTEGER);
            stmt.setInt(3, group.getYearOfStudy());
            stmt.setInt(4, group.getStudentCount());
            
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        group.setId(keys.getInt(1));
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
    public boolean update(Group group) throws SQLException {
        String sql = "UPDATE groups SET name = ?, curator_id = ?, year_of_study = ?, " +
                     "student_count = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, group.getName());
            stmt.setObject(2, group.getCuratorId(), Types.INTEGER);
            stmt.setInt(3, group.getYearOfStudy());
            stmt.setInt(4, group.getStudentCount());
            stmt.setInt(5, group.getId());
            return stmt.executeUpdate() > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM groups WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not found", e);
        }
    }
    
    @Override
    public int getStudentCount(int groupId) throws SQLException {
        String sql = "SELECT COUNT(*) as student_count FROM users WHERE group_id = ? AND role = 'STUDENT'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
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
    public double getAverageGrade(int groupId) throws SQLException {
        String sql = "SELECT AVG(ar.grade_value) as avg_grade FROM academic_records ar " +
                     "INNER JOIN users u ON ar.student_id = u.id WHERE u.group_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
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
    
    private Group mapResultSetToGroup(ResultSet rs) throws SQLException {
        Group group = new Group();
        group.setId(rs.getInt("id"));
        group.setName(rs.getString("name"));
        group.setCuratorId(rs.getObject("curator_id", Integer.class));
        group.setYearOfStudy(rs.getInt("year_of_study"));
        group.setStudentCount(rs.getInt("student_count"));
        return group;
    }
}
