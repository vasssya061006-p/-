package server.command;

import server.network.Request;
import server.network.Response;
import server.service.GradeService;
import server.model.Grade;

public class AddGradeCommand implements Command {
    private GradeService gradeService = new GradeService();

    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            Integer studentId = (Integer) data[0];
            String studentName = (String) data[1];
            String courseName = (String) data[2];
            Integer gradeValue = (Integer) data[3];
            String gradeType = (String) data[4];
            String date = (String) data[5];

            Grade grade = gradeService.addGrade(studentId, studentName, courseName,
                    gradeValue, gradeType, date);
            return new Response(true, "Оценка добавлена", grade);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}