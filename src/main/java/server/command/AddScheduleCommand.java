package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class AddScheduleCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            String courseInfo = (String) data[0];
            String groupInfo = (String) data[1];
            String teacherInfo = (String) data[2];
            String room = (String) data[3];
            String day = (String) data[4];
            String startTime = (String) data[5];
            String endTime = (String) data[6];
            String type = (String) data[7];
            String semester = (String) data[8];

            boolean success = EducationService.getInstance().addSchedule(courseInfo, groupInfo, teacherInfo,
                    room, day, startTime, endTime, type, semester);
            return new Response(success, success ? "Занятие добавлено" : "Ошибка добавления", null);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}