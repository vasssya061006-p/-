/*package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class AddStudentCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            Object[] data = (Object[]) request.getData();
            String login = (String) data[0];
            String fullName = (String) data[1];
            String groupName = (String) data[2];
            int yearOfStudy = (Integer) data[3];
            String studentIdNumber = (String) data[4];

            boolean success = EducationService.getInstance().addStudent(login, fullName, groupName, yearOfStudy, studentIdNumber);
            return new Response(success, success ? "Студент добавлен" : "Ошибка добавления студента", null);
        } catch (Exception e) {
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}
*/
package server.command;

import server.network.Request;
import server.network.Response;
import server.service.EducationService;

public class AddStudentCommand implements Command {
    @Override
    public Response execute(Request request) {
        try {
            System.out.println("=== AddStudentCommand called ===");
            Object[] data = (Object[]) request.getData();

            String login = (String) data[0];
            String fullName = (String) data[1];
            String groupName = (String) data[2];
            int yearOfStudy = (Integer) data[3];
            String studentIdNumber = (String) data[4];

            System.out.println("Login: " + login);
            System.out.println("FullName: " + fullName);
            System.out.println("GroupName: " + groupName);
            System.out.println("Year: " + yearOfStudy);
            System.out.println("StudentId: " + studentIdNumber);

            boolean success = EducationService.getInstance().addStudent(login, fullName, groupName, yearOfStudy, studentIdNumber);
            System.out.println("Success: " + success);

            return new Response(success, success ? "Студент добавлен" : "Ошибка добавления студента", null);
        } catch (Exception e) {
            System.err.println("Error in AddStudentCommand: " + e.getMessage());
            e.printStackTrace();
            return new Response(false, "Ошибка: " + e.getMessage(), null);
        }
    }
}