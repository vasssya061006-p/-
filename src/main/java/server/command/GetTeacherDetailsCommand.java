package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class GetTeacherDetailsCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int teacherId = (Integer) data[0];
            Object[] details = EducationService.getInstance().getTeacherDetails(teacherId);
            return new Response(true, "Данные загружены", details);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}