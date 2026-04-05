package server.service;

import server.model.Grade;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GradeService {
    private static Map<Integer, List<Grade>> studentGrades = new HashMap<>();
    private static AtomicInteger idGenerator = new AtomicInteger(1);

    public List<Grade> getGradesByStudentId(Integer studentId) {
        return studentGrades.getOrDefault(studentId, new ArrayList<>());
    }

    public Grade addGrade(Integer studentId, String studentName, String courseName,
                          Integer gradeValue, String gradeType, String date) {
        Grade grade = new Grade(idGenerator.getAndIncrement(), studentId, studentName,
                courseName, gradeValue, gradeType, date);

        studentGrades.computeIfAbsent(studentId, k -> new ArrayList<>()).add(grade);
        return grade;
    }

    public boolean updateGrade(Integer gradeId, Integer newGradeValue) {
        for (List<Grade> grades : studentGrades.values()) {
            for (Grade grade : grades) {
                if (grade.getId().equals(gradeId)) {
                    grade.setGradeValue(newGradeValue);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean deleteGrade(Integer gradeId) {
        for (List<Grade> grades : studentGrades.values()) {
            if (grades.removeIf(grade -> grade.getId().equals(gradeId))) {
                return true;
            }
        }
        return false;
    }
}
