package server.command;

import server.network.Request;
import server.network.Response;
import server.service.GradeService;
import server.model.Grade;
import java.util.List;

public class GetGradesCommand implements Command {
    private GradeService gradeService = new GradeService();

    @Override
    public Response execute(Request request) {
        try {
            Integer studentId = (Integer) request.getData();
            List<Grade> grades = gradeService.getGradesByStudentId(studentId);
            return new Response(true, "Оценки загружены", grades);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}