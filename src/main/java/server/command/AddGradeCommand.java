package server.command;

import server.network.Request;
import server.network.Response;
import server.dao.AcademicRecordDao;
import server.dao.impl.AcademicRecordDaoImpl;
import server.model.AcademicRecord;
import java.time.LocalDate;

public class AddGradeCommand implements Command {

    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();

            Integer studentId = (Integer) data[0];
            String studentName = (String) data[1];
            String courseName = (String) data[2];
            Integer gradeValue = (Integer) data[3];
            String gradeType = (String) data[4];
            String date = (String) data[5];
            String attendanceStatus = (String) data[6];

            AcademicRecord record = new AcademicRecord();
            record.setStudentId(studentId);
            record.setStudentName(studentName);
            record.setCourseName(courseName);
            record.setGradeValue(gradeValue);
            record.setGradeType(gradeType);
            record.setGradeDate(LocalDate.parse(date));
            record.setAttendanceStatus(attendanceStatus);
            record.setSemester("2025-2026-spring");

            AcademicRecordDao dao = new AcademicRecordDaoImpl();
            boolean success = dao.insert(record);

            return new Response(success, success ? "Оценка добавлена" : "Ошибка добавления", record);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}