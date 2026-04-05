package server.dao;

import server.model.Course;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Course operations.
 */
public interface CourseDao {
    
    Course findById(int id) throws SQLException;
    
    List<Course> findAll() throws SQLException;
    
    List<Course> findByTeacherId(int teacherId) throws SQLException;
    
    List<Course> findByDepartment(String department) throws SQLException;
    
    boolean insert(Course course) throws SQLException;
    
    boolean update(Course course) throws SQLException;
    
    boolean delete(int id) throws SQLException;
    
    int getStudentCount(int courseId) throws SQLException;
    
    double getAverageGrade(int courseId) throws SQLException;
}
