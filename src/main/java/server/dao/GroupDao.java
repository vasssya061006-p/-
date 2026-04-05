package server.dao;

import server.model.Group;
import java.sql.SQLException;
import java.util.List;

public interface GroupDao {

    Group findById(int id) throws SQLException;

    List<Group> findAll() throws SQLException;

    List<Group> findByCuratorId(int curatorId) throws SQLException;

    boolean insert(Group group) throws SQLException;

    boolean update(Group group) throws SQLException;

    boolean delete(int id) throws SQLException;

    int getStudentCount(int groupId) throws SQLException;

    double getAverageGrade(int groupId) throws SQLException;
}
