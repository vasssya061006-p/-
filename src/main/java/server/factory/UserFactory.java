package server.factory;

import server.model.*;

/**
 * Factory Method pattern implementation for creating User objects.
 * This factory encapsulates the creation logic for different user types
 * (Student, Teacher, Admin) based on the role.
 */
public class UserFactory {
    
    /**
     * Creates a User object based on the specified role.
     * 
     * @param role the role of the user (STUDENT, TEACHER, ADMIN)
     * @return the appropriate User subclass instance
     * @throws IllegalArgumentException if role is unknown
     */
    public static User createUser(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be null or empty");
        }
        
        switch (role.toUpperCase()) {
            case "STUDENT":
                return new Student();
            case "TEACHER":
                return new Teacher();
            case "ADMIN":
                return new Admin();
            default:
                throw new IllegalArgumentException("Unknown user role: " + role);
        }
    }
    
    /**
     * Creates a fully initialized Student with default values.
     */
    public static Student createStudent(int id, String login, String fullName, String group) {
        Student student = new Student();
        student.setId(id);
        student.setLogin(login);
        student.setFullName(fullName);
        student.setGroup(group);
        student.setRole("STUDENT");
        student.setActive(true);
        return student;
    }
    
    /**
     * Creates a fully initialized Teacher with default values.
     */
    public static Teacher createTeacher(int id, String login, String fullName, 
                                        String position, String department) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setLogin(login);
        teacher.setFullName(fullName);
        teacher.setPosition(position);
        teacher.setDepartment(department);
        teacher.setRole("TEACHER");
        teacher.setActive(true);
        return teacher;
    }
    
    /**
     * Creates a fully initialized Admin with default values.
     */
    public static Admin createAdmin(int id, String login, String fullName, 
                                    String position, String department) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setLogin(login);
        admin.setFullName(fullName);
        admin.setPosition(position);
        admin.setDepartment(department);
        admin.setRole("ADMIN");
        admin.setAccessLevel("FULL");
        admin.setActive(true);
        return admin;
    }
    
    /**
     * Creates a User from database data, determining the correct type.
     */
    public static User createUserFromData(String role, int id, String login, 
                                          String passwordHash, String fullName, boolean active) {
        User user = createUser(role);
        user.setId(id);
        user.setLogin(login);
        user.setPasswordHash(passwordHash);
        user.setFullName(fullName);
        user.setActive(active);
        return user;
    }
}
