package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class ReportFullAnalyticsCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            String report = EducationService.getInstance().getFullAnalyticsReport();
            return new Response(true, "Отчёт сгенерирован", report);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}