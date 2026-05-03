package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class DeleteGroupCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            int id;
            Object data = request.getData();

            if (data instanceof Integer) {
                id = (Integer) data;
            } else if (data instanceof Object[]) {
                Object[] arr = (Object[]) data;
                id = (Integer) arr[0];
            } else {
                return new Response(false, "Неверный формат данных", null);
            }

            boolean success = EducationService.getInstance().deleteGroup(id);
            return new Response(success, success ? "Группа удалена" : "Ошибка удаления", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}