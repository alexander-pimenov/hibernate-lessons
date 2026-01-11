package sorokin.dev;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sorokin.dev.entity.Student;
import sorokin.dev.service.StudentSimpleManualService;

import java.util.List;

/**
 * После запуска приложения в БД появятся таблицы: students, profiles, student_group, courses.
 * Их создаст Hibernate.
 * Когда мы будем сохранять объекты в БД, например, session.persist(student), то Hibernate создаст для них строки в таблицах.
 * Это мы моем увидеть в логах:
 * Hibernate: insert into students (student_age,group_id,name) values (?,?,?) returning id
 * <p>
 * В этом классе мы будем совсем немного тестировать код. Далее все будет в тестах.
 */
public class Main {
    public static void main(String[] args) {
        // Создаем контекст, в котором будут созданы бины, которые будут использоваться в проекте.
        // В качестве параметра передаем название пакета, в котором находятся бины, например, "sorokin.dev".
        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext("sorokin.dev");

        // Получаем бин SessionFactory из контекста.
        SessionFactory sessionFactory = context.getBean(SessionFactory.class);
        // Создаем сессию. Сессия - это объект, который позволяет работать с БД.
        // ТАКЖЕ НЕ ЗАБЫВАЕМ: Все модифицирующие операции должны проходить в транзакции.
        // Операции поиска можно выполнять без транзакции.
        Session session1 = sessionFactory.openSession(); // Открываем сессию.

        //Создадим студентов:
        Student student1 = new Student("Vasya", 22, null);
        Student student2 = new Student("Pasha", 32, null);
        //Начинаем работу с БД (открыть транзакцию - выполнить действие - закрыть транзакцию):
        session1.beginTransaction();          // Начинаем транзакцию.
        session1.persist(student1);           // Сохраняем объект в БД.
        session1.persist(student2);           // Сохраняем объект в БД.
        session1.getTransaction().commit();   // Завершаем транзакцию.

        //Выполним поиск всех студентов.
        //=== 1 вариант: ===
        Student studentById1 = session1.get(Student.class, 1L); // Получаем объект из БД по id.
        Student studentById2 = session1.get(Student.class, 2L); // Получаем объект из БД по id.
        System.out.println("student1: " + studentById1.toString());
        System.out.println("student2: " + studentById2.toString());

        //=== 2 вариант: ===
        //Также можно выполнить запрос на языке JPQL (в нём мы оперируем сущностями, а не таблицами):
        Student studentById3 = session1.createQuery("SELECT s FROM Student s WHERE s.id = :id", Student.class)
                .setParameter("id", 1L)
                .getSingleResult(); // Получаем объект из БД по id
        //Hibernate: select s1_0.id,s1_0.student_age,s1_0.group_id,s1_0.name from students s1_0 where s1_0.id=?

        Student studentById4 = session1.createQuery("SELECT s FROM Student s WHERE s.id = :id", Student.class)
                .setParameter("id", 2L)
                .getSingleResult(); // Получаем объект из БД по id
        //Hibernate: select s1_0.id,s1_0.student_age,s1_0.group_id,s1_0.name from students s1_0 where s1_0.id=?

        System.out.println("student3: " + studentById3.toString()); //student3: Student{id=1, name='Vasya', age=22}
        System.out.println("student4: " + studentById4.toString()); //student4: Student{id=2, name='Pasha', age=32}

        //Поиск по имени:
        Student studentByName = session1.createQuery("SELECT s FROM Student s WHERE s.name = :name", Student.class)
                .setParameter("name", "Vasya")
                .getSingleResult();
        System.out.println("studentByName: " + studentByName.toString()); //studentByName: Student{id=1, name='Vasya', age=22}

        //Получим все записи из таблицы:
        List<Student> students = session1.createQuery("from Student", Student.class).list(); //или такой запрос: SELECT s FROM Student s
        //Hibernate: select s1_0.id,s1_0.student_age,s1_0.group_id,s1_0.name from students s1_0
        System.out.println("students: " + students.toString()); //students: [Student{id=1, name='Vasya', age=22}, Student{id=2, name='Pasha', age=32}]

        //Рассмотрим примеры обновления данных (UPDATE):
        session1.beginTransaction();         // Начинаем транзакцию.
        Student studentForUpdate = session1.get(Student.class, 1L); // Получаем объект из БД по id.
        //Для обновления можно использовать обычные сеттеры:
        if (studentForUpdate != null) {
            studentForUpdate.setName("Vasya Pupkin");
            studentForUpdate.setAge(23);
            //Session следит за изменениями объектов, поэтому, если мы изменим объект, то Hibernate обновит его в БД:
            //Hibernate: update students set student_age=?,group_id=?,name=? where id=?
            //session.update(studentForUpdate); // Обновляем объект в БД. - это не нужно делать, т.к. Hibernate Session следит за изменениями объектов.
        }
        //Сделаем запрос для проверки, что объект обновился:
        Student studentByIdForUpdate2 = session1.createQuery("SELECT s FROM Student s WHERE s.id = :id", Student.class)
                .setParameter("id", 1L)
                .getSingleResult(); // Получаем объект из БД по id
        //Hibernate: select s1_0.id,s1_0.student_age,s1_0.group_id,s1_0.name from students s1_0 where s1_0.id=?

        System.out.println("studentByIdForUpdate2: " + studentByIdForUpdate2.toString());
        //studentByIdForUpdate2: Student{id=1, name='Vasya Pupkin', age=23}

        session1.getTransaction().commit();  // Завершаем транзакцию.

        //Пример удаления данных (DELETE):
        session1.beginTransaction();         // Начинаем транзакцию.
        Student studentForDelete = session1.get(Student.class, 2L); // Получаем объект из БД по id.
        //Для обновления можно использовать обычные сеттеры:
        if (studentForDelete != null) {
            session1.remove(studentForDelete);   // Удаляем объект из БД.
            //Hibernate: delete from students where id=?
        }

        //Для проверки, что объект удалился, получим все записи из таблицы:
        List<Student> studentList = session1.createQuery("from Student", Student.class).list();
        //Hibernate: select s1_0.id,s1_0.student_age,s1_0.group_id,s1_0.name from students s1_0
        System.out.println("studentList: " + studentList.toString()); //studentList: [Student{id=1, name='Vasya Pupkin', age=23}]

        //Для примера напишем запрос на голом SQL, чтобы проверить данные в БД:
        List<Student> list = session1.createNativeQuery("SELECT * FROM students", Student.class).list();
        //Hibernate: SELECT * FROM students
        System.out.println("list: " + list.toString()); //list: [Student{id=1, name='Vasya Pupkin', age=23}]

        //Удалим еще одного, но с помощью запроса на языке JPQL:
        if (!studentList.isEmpty()) {
            session1.createQuery("DELETE FROM Student s WHERE s.id = :id")
                    .setParameter("id", 1L)
                    .executeUpdate(); // Удаляем объект из БД.
            //Hibernate: delete from students where id=?
            //Hibernate: select s1_0.id,s1_0.student_age,s1_0.group_id,s1_0.name from students s1_0
            //studentList: [Student{id=1, name='Vasya Pupkin', age=23}]
            //Hibernate: delete from student_courses to_delete_ where to_delete_.student_id in (select s1_0.id from students s1_0 where s1_0.id=?)
            //Hibernate: delete from students s1_0 where s1_0.id=?
            //Получим все записи из таблицы:
            //запрос должен вернуть 0
            studentList = session1.createQuery("from Student", Student.class).list();
            System.out.println("studentList: " + studentList.toString()); //studentList: [];
        }
        session1.getTransaction().commit();     // Завершаем транзакцию.
        session1.close();                       // Закрываем сессию.

        //////*********************************************************************************
        //2-й УРОК. Состояния сущности. Переходы между ними.

        Session session2 = sessionFactory.openSession();
        //Начинаем работу с БД (открыть транзакцию - выполнить действие - закрыть транзакцию):
        session2.beginTransaction();            // Начинаем транзакцию.

        //Создадим студентов, о них Hibernate не знает, они в состоянии TRANSIENT:
        Student student2_1 = new Student("Vasya", 32, null);
        Student student2_2 = new Student("Pasha", 42, null);

        // Сохраним студентов в БД, о них Hibernate/Сессия теперь знает, кэширует их у себя внутри, теперь помнит о них,
        // они в состоянии PERSISTENT:
        session2.persist(student2_1);           // Сохраняем объект в БД. - Hibernate: insert into students (student_age,group_id,name) values (?,?,?) returning id
        session2.persist(student2_2);           // Сохраняем объект в БД. - Hibernate: insert into students (student_age,group_id,name) values (?,?,?) returning id

        session2.getTransaction().commit();     // Завершаем транзакцию.
        session2.close();                       // Закрываем сессию. После этого Сессия не хранит состояния сущностей.
        // И они переходят в состояние DETACHED.

        Session session3 = sessionFactory.openSession();
        //Переведем студента из состояния DETACHED в PERSISTENT c помощью метода merge():
        student2_1 = session3.merge(student2_1);    //Hibernate идет в БД и подтягивает объект student2_1, который был в состоянии DETACHED, так он становится PERSISTENT.
        //Обновление объекта student2_1 делаем в транзакции:
        session3.beginTransaction();            // Начинаем транзакцию.
        student2_1.setAge(18);                  // Обновляем объект.
        student2_1.setName("Vasya Gubkin");     // Теперь мы можем изменить имя студента и оно после коммита попадет в БД.
        //Hibernate: select s1_0.id,s1_0.student_age,g1_0.id,g1_0.grad_year,g1_0.number,s1_0.name,p1_0.id,p1_0.bio,p1_0.last_seen_time,cl1_0.student_id,cl1_1.id,cl1_1.name,cl1_1.type from students s1_0 left join student_group g1_0 on g1_0.id=s1_0.group_id left join profiles p1_0 on s1_0.id=p1_0.student_id left join student_courses cl1_0 on s1_0.id=cl1_0.student_id left join courses cl1_1 on cl1_1.id=cl1_0.course_id where s1_0.id=?

        session3.detach(student2_1);            // Отсоединяем объект student2_1, он теперь в состоянии DETACHED.
        student2_1.setAge(200);                 // Обновляем объект. Но в БД он не обновится, т.к. он в состоянии DETACHED.

        //Получим все записи из таблицы:
        List<Student> students2_1 = session3.createQuery("from Student", Student.class).list(); //или такой запрос: SELECT s FROM Student s
        //Hibernate: select s1_0.id,s1_0.student_age,s1_0.group_id,s1_0.name from students s1_0
        System.out.println("students2_1: " + students2_1.toString()); //students2_1: [Student{id=3, name='Vasya', age=32}, Student{id=4, name='Pasha', age=42}]


        session3.getTransaction().commit();     // Завершаем транзакцию.
        //Hibernate: update students set student_age=?,group_id=?,name=? where id=?


        session3.close();

        //*********************************************************************************
        // Получаем бин StudentSimpleManualService из контекста Spring.
        // Поработаем через сервис с сессией:
        StudentSimpleManualService studentSimpleManualService = context.getBean(StudentSimpleManualService.class);

        //Создадим студентов:
        Student student3_1 = new Student("Bob", 25, null);
        Student student3_2 = new Student("John", 35, null);
        // Сохраним студентов в БД:
        studentSimpleManualService.saveStudent(student3_1);
        studentSimpleManualService.saveStudent(student3_2);
        //Получим всех студентов:
        List<Student> allStudents = studentSimpleManualService.findAll();
        System.out.println("allStudents: " + allStudents);
        //allStudents: [Student{id=3, name='Vasya', age=32}, Student{id=4, name='Pasha', age=42}, Student{id=5, name='Bob', age=25}, Student{id=6, name='John', age=35}]

/*        StudentService studentService = context.getBean(StudentService.class);
        ProfileService profileService = context.getBean(ProfileService.class);
        GroupService groupService = context.getBean(GroupService.class);
        CourseService courseService = context.getBean(CourseService.class);

//        Group group1 = groupService.saveGroup("1", 2024L);
//        Group group2 = groupService.saveGroup("2", 2024L);
//        Group group3 = groupService.saveGroup("3", 2024L);

//        Student student1 = new Student("Vasya", 22, group1);
//        Student student2 = new Student("Pasha", 20, group1);
//
//        studentService.saveStudent(student1);
//        studentService.saveStudent(student2);

        Course course1 = new Course("math-1", "math");
        Course course2 = new Course("math-2", "math");
        Course course3 = new Course("math-3", "math");

//        courseService.saveCourse(course1);
//        courseService.saveCourse(course2);
//        courseService.saveCourse(course3);

        courseService.enrollStudentToCourse(2L, 2L);
        courseService.enrollStudentToCourse(3L, 2L);

        Student studentFomDb = studentService.getById(2L);
        System.out.println(studentFomDb);
*/
    }
}