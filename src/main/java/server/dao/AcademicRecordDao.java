package server.dao;

import server.model.AcademicRecord;
import java.sql.SQLException;
import java.util.List;

public interface AcademicRecordDao {

    AcademicRecord findById(int id) throws SQLException;

    List<AcademicRecord> findByStudentId(int studentId) throws SQLException;

    List<AcademicRecord> findByGroupId(int groupId) throws SQLException;

    List<AcademicRecord> findByCourseId(int courseId) throws SQLException;

    List<AcademicRecord> findBySemester(String semester) throws SQLException;

    List<AcademicRecord> findByStudentAndCourse(int studentId, int courseId) throws SQLException;

    boolean insert(AcademicRecord record) throws SQLException;

    boolean update(AcademicRecord record) throws SQLException;

    boolean delete(int id) throws SQLException;

    double getAverageGradeByStudent(int studentId) throws SQLException;

    double getAverageGradeByCourse(int courseId) throws SQLException;

    double getAverageGradeByGroup(int groupId) throws SQLException;

    int countAbsencesByStudent(int studentId) throws SQLException;

    List<AcademicRecord> getFailingGrades(double threshold) throws SQLException;
}
