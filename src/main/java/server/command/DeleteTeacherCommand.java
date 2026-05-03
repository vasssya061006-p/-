package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class DeleteTeacherCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int id = (Integer) data[0];

            boolean success = EducationService.getInstance().deleteTeacher(id);
            return new Response(success, success ? "Преподаватель удалён" : "Ошибка удаления", null);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}