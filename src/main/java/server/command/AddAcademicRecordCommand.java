package server.command;

import server.model.AcademicRecord;
import server.network.Request;
import server.network.Response;
import server.dao.AcademicRecordDao;
import server.dao.impl.AcademicRecordDaoImpl;

public class AddAcademicRecordCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            AcademicRecord record = (AcademicRecord) data[0];
            AcademicRecordDao dao = new AcademicRecordDaoImpl();
            boolean success = dao.insert(record);
            return new Response(success, success ? "Record added" : "Failed to add record", record);
        } catch (Exception e) {
            return new Response(false, "Error adding record: " + e.getMessage(), null);
        }
    }
}
