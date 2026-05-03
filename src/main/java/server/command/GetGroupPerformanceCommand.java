package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class GetGroupPerformanceCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            String groupName;
            Object data = request.getData();

            if (data instanceof String) {
                groupName = (String) data;
            } else if (data instanceof Object[]) {
                Object[] arr = (Object[]) data;
                groupName = (String) arr[0];
            } else {
                return new Response(false, "Неверный формат данных", null);
            }

            String analytics = EducationService.getInstance().getGroupPerformanceReport(groupName);
            return new Response(true, "Аналитика загружена", analytics);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}