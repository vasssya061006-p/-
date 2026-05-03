package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class UpdateGroupCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            int id = (Integer) data[0];
            String name = (String) data[1];
            int yearOfStudy = (Integer) data[2];
            Integer curatorId = (Integer) data[3];

            boolean success = EducationService.getInstance().updateGroup(id, name, yearOfStudy, curatorId);
            return new Response(success, success ? "Группа обновлена" : "Ошибка обновления", null);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}