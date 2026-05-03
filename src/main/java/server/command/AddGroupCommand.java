package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class AddGroupCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            String name = (String) data[0];
            int yearOfStudy = (Integer) data[1];
            Integer curatorId = (Integer) data[2];

            boolean success = EducationService.getInstance().addGroup(name, yearOfStudy, curatorId);
            return new Response(success, success ? "Группа добавлена" : "Ошибка добавления группы", null);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}