package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class ArchiveRecordsCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            String semester = (String) data[0];
            EducationService.ArchiveResult result = EducationService.getInstance().archiveAcademicRecords(semester);
            return new Response(true, "Records archived", result);
        } catch (Exception e) {
            return new Response(false, "Error archiving records: " + e.getMessage(), null);
        }
    }
}
