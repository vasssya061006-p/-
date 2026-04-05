# Education Management System - Курсовой Проект

## Тема
Интегрированная система организации образовательного процесса с модулем учета успеваемости учащихся

## Архитектура
Client-Server архитектура (TCP/Sockets), многопоточный сервер, JavaFX клиент, MySQL/PostgreSQL база данных

---

## 📋 Реализованные требования

### ✅ 1. Иерархия классов (12 классов)
```
BaseEntity (abstract)
├── User (abstract)
│   ├── Student
│   └── Employee (abstract)
│       ├── Teacher
│       └── Admin
├── Course
├── Lesson
├── AcademicRecord
├── Group
└── Schedule
```

**Всего классов:** 18 (требование: 8+)
- **BaseEntity** - абстрактный базовый класс с ID
- **User** - абстрактный класс пользователя
- **Student, Teacher, Admin** - конкретные роли
- **Employee** - абстрактный класс сотрудника
- **Course, Lesson, AcademicRecord, Group, Schedule** - предметная область

### ✅ 2. Паттерны проектирования (2 паттерна)

#### Паттерн 1: Singleton
- **DatabaseManager** - управление подключением к БД
  - Thread-safe double-checked locking
  - Гарантирует единственное подключение
  - Читает настройки из config.properties

#### Паттерн 2: Factory Method
- **UserFactory** - создание объектов пользователей
  - Инкапсулирует логику создания Student/Teacher/Admin
  - Статические методы для удобства использования

#### Паттерн 3: Command (дополнительно)
- **Command interface** + 17 конкретных команд
- **CommandFactory** - реестр команд
- **ClientHandler** - вызывающий (Invoker)

### ✅ 3. ООП техники

| Техника | Реализация |
|---------|------------|
| **Сокрытие данных** | Все поля `protected`/`private`, доступ через геттеры/сеттеры |
| **Перегрузка методов** | Конструкторы с разным количеством параметров (напр., `User()`, `User(id, login, ...)`) |
| **Переопределение методов** | `toString()` в каждом классе, `execute()` в командах |
| **Сериализация** | Все модели реализуют `Serializable` для передачи по сети |
| **Абстрактные типы** | `BaseEntity`, `User`, `Employee` - абстрактные классы; `Command`, `*Dao` - интерфейсы |
| **Статические методы** | `User.hashPassword()`, `UserFactory.createUser()`, `EducationService.getInstance()` |
| **Обработка исключений** | `AuthenticationException`, try-catch в командах, SQLException в DAO |

### ✅ 4. Бизнес-логика (12+ Use Cases)

| # | Use Case | Команда | Описание | Роли |
|---|----------|---------|----------|------|
| 1 | **Authenticate User** | `LOGIN` | Авторизация с session management | Все |
| 2 | **Calculate Student GPA** | `CALCULATE_GPA` | Расчет среднего балла с разбивкой | Teacher, Admin |
| 3 | **Check Session Eligibility** | `CHECK_SESSION_ELIGIBILITY` | Проверка допуска к сессии | Teacher, Admin |
| 4 | **Generate Grade Report** | `GENERATE_GRADE_REPORT` | Генерация ведомости | Teacher, Admin |
| 5 | **Get Group Analytics** | `GET_GROUP_ANALYTICS` | Статистика группы | Teacher, Admin |
| 6 | **Low-Grade Alerts** | `GET_LOW_GRADE_ALERTS` | Уведомления о низких баллах | Teacher |
| 7 | **Teacher Workload Report** | `GET_TEACHER_WORKLOAD` | Нагрузка преподавателей | Admin |
| 8 | **Schedule Validation** | `VALIDATE_SCHEDULE` | Проверка конфликтов в расписании | Admin |
| 9 | **Attendance Summary** | `GET_ATTENDANCE_SUMMARY` | Сводка посещаемости | Teacher, Admin |
| 10 | **Archive Records** | `ARCHIVE_RECORDS` | Архивация записей семестра | Admin |
| 11 | **Course Statistics** | `GET_COURSE_STATISTICS` | Статистика курса | Teacher, Admin |
| 12 | **Export Analytics** | `EXPORT_ANALYTICS` | Институциональный отчет | Admin |

### ✅ 5. Роли пользователей (3 роли)

1. **STUDENT** - просмотр своих оценок, посещаемости, проверка допуска к сессии
2. **TEACHER** - управление оценками, аналитика группы, уведомления
3. **ADMIN** - полный доступ, включая архивацию, расписание, институциональную аналитику

### ✅ 6. База данных (5 таблиц, 3NF)

```sql
users        - Пользователи (STUDENT, TEACHER, ADMIN)
groups       - Учебные группы
courses      - Учебные дисциплины
schedules    - Расписание занятий
academic_records - Успеваемость и посещаемость
```

**Нормализация до 3НФ:**
- 1НФ: Все поля атомарны
- 2НФ: Все неключевые поля зависят от полного первичного ключа
- 3НФ: Нет транзитивных зависимостей

**Связи:**
- `users.group_id` → `groups.id` (неидентифицирующая, N:1)
- `groups.curator_id` → `users.id` (неидентифицирующая, N:1)
- `courses.teacher_id` → `users.id` (неидентифицирующая, N:1)
- `schedules.course_id` → `courses.id` (идентифицирующая, N:1)
- `schedules.group_id` → `groups.id` (идентифицирующая, N:1)
- `schedules.teacher_id` → `users.id` (неидентифицирующая, N:1)
- `academic_records.student_id` → `users.id` (идентифицирующая, N:1)
- `academic_records.course_id` → `courses.id` (неидентифицирующая, N:1)

**Ссылочная целостность:** Обеспечена через FOREIGN KEY с ON DELETE CASCADE/SET NULL

---

## 🗂️ Структура проекта (Professional Layout)

```
src/
└── main/
    ├── java/                           # Исходный код Java
    │   ├── client/                     # Клиентское приложение (JavaFX)
    │   │   ├── LoginWindow.java        # Окно авторизации
    │   │   ├── MainWindow.java         # Главное окно с роль-зависимым UI
    │   │   └── ServerConnection.java   # TCP/IP подключение
    │   └── server/                     # Серверное приложение
    │       ├── Server.java             # Точка входа сервера
    │       ├── command/                # Command Pattern (17 команд)
    │       │   ├── Command.java        # Интерфейс
    │       │   ├── CommandFactory.java # Factory для команд
    │       │   ├── LoginCommand.java   # UC1: Авторизация
    │       │   ├── CalculateGPACommand.java      # UC2
    │       │   ├── CheckSessionEligibilityCommand.java  # UC3
    │       │   ├── GenerateGradeReportCommand.java    # UC4
    │       │   ├── GetGroupAnalyticsCommand.java      # UC5
    │       │   ├── GetLowGradeAlertsCommand.java      # UC6
    │       │   ├── GetTeacherWorkloadCommand.java     # UC7
    │       │   ├── ValidateScheduleCommand.java       # UC8
    │       │   ├── GetAttendanceSummaryCommand.java   # UC9
    │       │   ├── ArchiveRecordsCommand.java         # UC10
    │       │   ├── GetCourseStatisticsCommand.java    # UC11
    │       │   ├── ExportAnalyticsCommand.java        # UC12
    │       │   └── AddAcademicRecordCommand.java
    │       ├── dao/                    # Data Access Object Layer
    │       │   ├── DatabaseManager.java    # Singleton для БД
    │       │   ├── UserDao.java            # Интерфейсы DAO
    │       │   ├── AcademicRecordDao.java
    │       │   ├── ScheduleDao.java
    │       │   ├── CourseDao.java
    │       │   ├── GroupDao.java
    │       │   └── impl/                   # JDBC реализации
    │       │       ├── UserDaoImpl.java
    │       │       ├── AcademicRecordDaoImpl.java
    │       │       ├── ScheduleDaoImpl.java
    │       │       ├── CourseDaoImpl.java
    │       │       └── GroupDaoImpl.java
    │       ├── factory/
    │       │   └── UserFactory.java    # Factory Method Pattern
    │       ├── model/                  # Domain Model (12 классов)
    │       │   ├── BaseEntity.java
    │       │   ├── User.java (abstract)
    │       │   ├── Student.java
    │       │   ├── Employee.java (abstract)
    │       │   ├── Teacher.java
    │       │   ├── Admin.java
    │       │   ├── Course.java
    │       │   ├── Lesson.java
    │       │   ├── AcademicRecord.java
    │       │   ├── Group.java
    │       │   └── Schedule.java
    │       ├── network/
    │       │   ├── ClientHandler.java  # Обработка клиента
    │       │   ├── Request.java        # Запрос
    │       │   └── Response.java       # Ответ
    │       └── service/
    │           ├── EducationService.java       # Бизнес-логика (12 UC)
    │           └── AuthenticationException.java
    └── resources/                      # Ресурсы приложения
        ├── config.properties           # Конфигурация сервера
        ├── client/
        │   └── styles.css              # Стили JavaFX
        └── database/
            └── init_database.sql       # Скрипт инициализации БД
```

---

## ⚙️ Установка и запуск

### Требования
- **Java:** JDK 25+
- **JavaFX:** SDK 26+
- **MySQL:** 8.0+ (или H2 для тестирования)
- **IDE:** IntelliJ IDEA (рекомендуется)

### 1. Настройка базы данных

#### Вариант A: MySQL
```bash
# 1. Установите MySQL 8.0+
# 2. Выполните скрипт инициализации:
mysql -u root -p < src/main/resources/database/init_database.sql

# 3. Настройте подключение в config.properties:
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/education_system
db.user=root
db.password=your_password
```

#### Вариант B: H2 (для тестирования)
```properties
# В src/main/resources/config.properties раскомментируйте:
db.driver=org.h2.Driver
db.url=jdbc:h2:./education_db
db.user=sa
db.password=
```

### 2. Настройка JavaFX

1. Скачайте JavaFX SDK 26 с https://gluonhq.com/products/javafx/
2. Распакуйте в `C:/javaFX/javafx-sdk-26`
3. В IntelliJ IDEA настройте Library:
   - File → Project Structure → Libraries
   - Укажите путь к `lib/` директории JavaFX

### 3. Запуск сервера

```bash
# Из IntelliJ IDEA:
# 1. Откройте src/main/java/server/Server.java
# 2. Run 'Server'

# Из командной строки:
javac --module-path C:/javaFX/javafx-sdk-26/lib --add-modules javafx.controls,javafx.fxml -d out src/main/java/**/*.java
java -cp out;src/main/resources server.Server
```

### 4. Запуск клиента

```bash
# Из IntelliJ IDEA:
# 1. Откройте src/main/java/client/LoginWindow.java
# 2. Run 'LoginWindow'

# Из командной строки:
java --module-path C:/javaFX/javafx-sdk-26/lib --add-modules javafx.controls,javafx.fxml -cp out;src/main/resources client.LoginWindow
```

### 5. Тестовые учетные записи

| Логин | Пароль | Роль |
|-------|--------|------|
| `admin` | `admin123` | Администратор |
| `teacher1` | `teacher123` | Преподаватель |
| `student1` | `student123` | Студент |
| `student2` | `student123` | Студент |

---

## 🔐 Безопасность

- **Пароли:** Хэшируются SHA-256 перед сохранением в БД
- **Сессии:** Серверное хранение session map (ConcurrentHashMap)
- **Валидация:** Проверка входных данных на всех уровнях
- **Thread Safety:** ConcurrentHashMap для сессий, ExecutorService для пула потоков

---

## 📊 Архитектурные решения

### Многопоточность
- **ExecutorService.newFixedThreadPool(50)** - обработка клиентов
- **ClientHandler** - отдельный поток на клиента
- **ConcurrentHashMap** - thread-safe хранилище сессий

### Конфигурация без модификации кода
- **config.properties** - порт, БД, лимиты
- Аргументы командной строки (опционально)

### Сетевое взаимодействие
- **TCP/IP Sockets** - надежное соединение
- **Object Serialization** - передача объектов Request/Response
- **Синхронный запрос-ответ** - блокирующий вызов

---

## 🧪 Тестирование

### Проверка функциональности

1. **Авторизация:**
   - ✓ Успешный вход admin/admin123
   - ✓ Неверный пароль
   - ✓ Отключенный аккаунт

2. **Студент:**
   - ✓ Просмотр оценок
   - ✓ Расчет GPA
   - ✓ Проверка допуска к сессии
   - ✓ Посещаемость

3. **Преподаватель:**
   - ✓ Добавление оценок
   - ✓ Аналитика группы
   - ✓ Уведомления о низких баллах

4. **Администратор:**
   - ✓ Институциональная аналитика
   - ✓ Архивация записей
   - ✓ Валидация расписания

### Проверка требований ТЗ

| Требование | Статус | Подтверждение |
|------------|--------|---------------|
| 8+ классов | ✓ | 18 классов |
| 2+ паттерна | ✓ | Singleton, Factory Method, Command |
| 12+ use cases | ✓ | 12 бизнес-функций |
| Роли 2-3 | ✓ | STUDENT, TEACHER, ADMIN |
| 5+ таблиц | ✓ | users, groups, courses, schedules, academic_records |
| 3НФ | ✓ | Все таблицы нормализованы |
| JDBC | ✓ | Все DAO через JDBC |
| Многопоточность | ✓ | ExecutorService |
| Конфигурация | ✓ | config.properties |
| Авторизация | ✓ | SHA-256 + сессии |

---

## 📝 Примечания

### Используемые версии (актуальны на 2026)
- **Java:** JDK 25 (LTS)
- **JavaFX:** SDK 26
- **MySQL:** 8.0+
- **JDBC Driver:** mysql-connector-j-8.x

### Возможные улучшения
- Реализация Observer для уведомлений в реальном времени
- Strategy паттерн для различных алгоритмов расчета GPA
- Кэширование часто запрашиваемых данных
- WebSocket вместо TCP для real-time обновлений
- JPA/Hibernate вместо чистого JDBC

---

## 👨‍💻 Автор
Курсовой проект разработан в соответствии с требованиями ТЗ (разделы 3.1-3.4)

## 📄 Лицензия
Учебный проект
