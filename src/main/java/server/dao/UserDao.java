package server.dao;

import server.model.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDao {

    User findById(int id) throws SQLException;

    User findByLogin(String login) throws SQLException;

    List<User> findAll() throws SQLException;

    List<User> findByRole(String role) throws SQLException;

    boolean insert(User user) throws SQLException;

    boolean update(User user) throws SQLException;

    boolean delete(int id) throws SQLException;

    boolean updatePassword(int userId, String newPasswordHash) throws SQLException;

    List<User> findByGroupId(Integer groupId) throws SQLException;
}
