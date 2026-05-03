package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class ReportLowGradesCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            double threshold = (Double) data[0];
            String report = EducationService.getInstance().getLowGradesReport(threshold);
            return new Response(true, "Отчёт сгенерирован", report);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}