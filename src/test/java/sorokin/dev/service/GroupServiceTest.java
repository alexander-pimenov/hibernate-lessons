package sorokin.dev.service;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sorokin.dev.utils.TransactionHelper;
import sorokin.dev.config.TestHibernateConfiguration;
import sorokin.dev.entity.Group;
import sorokin.dev.entity.Student;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GroupServiceTest {

    private AnnotationConfigApplicationContext context;
    private StudentService studentService;
    private GroupService groupService;
    private TransactionHelper transactionHelper;

    @BeforeAll
    public void setup() {
        context = new AnnotationConfigApplicationContext(
                TestHibernateConfiguration.class,
                GroupService.class,
                StudentService.class,
                TransactionHelper.class
        );
        groupService = context.getBean(GroupService.class);
        studentService = context.getBean(StudentService.class);
    }

    @AfterAll
    public void tearDown() {
        context.close();
    }

    @Test
    @DisplayName("Сохранение Студента и Группы, в которых он состоит")
    public void testSaveStudentAndGroup() {
        //Создадим и сохраним несколько групп

        Group group1 = groupService.saveGroup("1", 2023L);
        Group group2 = groupService.saveGroup("2", 2024L);
        Group group3 = groupService.saveGroup("3", 2025L);

        Student student1 = new Student("Test Student 1", 20, group1);
        Student student2 = new Student("Test Student 2", 20, group1);

        Student savedStudent1 = studentService.saveStudent(student1);
        Student savedStudent2 = studentService.saveStudent(student2);

        assertNotNull(savedStudent1.getId());
        assertEquals("Test Student 1", savedStudent1.getName());
        System.out.println(savedStudent1.getGroup());
        System.out.println(savedStudent2.getGroup());

        //Посмотрим в Группах какие студенты есть у них
//        Group groupById = groupService.getGroupById(1L);
//        System.out.println(groupById.getStudentList());

        List<Group> all = groupService.findAll();
        System.out.println(all);

        groupService.findAll().forEach(group -> {
            System.out.println("group: " + group);
            group.getStudentList().forEach(student -> {
                System.out.println("__student: " + student);
            });
        });
    }

    @Test
    @DisplayName("Сохранение Студента и Группы, в которых он состоит 2")
    public void testSaveStudentAndGroup2() {
        //Создадим и сохраним несколько групп
        SessionFactory sessionFactory = context.getBean(SessionFactory.class);
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Group group = new Group("1", 2023L);
        session.persist(group);
        Student student1 = new Student("Test Student 1", 20, group);
        Student student2 = new Student("Test Student 2", 20, group);
        session.persist(student1);
        session.persist(student2);
        session.getTransaction().commit();
        session.close();
        //получим данные о группах
        var session2 = sessionFactory.openSession();
        Group groupById = session2.get(Group.class, 1L);
        System.out.println(groupById); //Group{id=1, number='1', graduationYear=2023}
        System.out.println("==============");
        List<Student> studentList = groupById.getStudentList();
        //Такие запросы сгенерирует Hibernate:
        //Hibernate: select sl1_0.group_id,sl1_0.id,sl1_0.student_age,sl1_0.name,p1_0.id,p1_0.bio,p1_0.last_seen_time from students sl1_0 left join profiles p1_0 on sl1_0.id=p1_0.student_id where sl1_0.group_id=?
        //Hibernate: select cl1_0.student_id,cl1_1.id,cl1_1.name,cl1_1.type from student_courses cl1_0 join courses cl1_1 on cl1_1.id=cl1_0.course_id where cl1_0.student_id=?
        //Hibernate: select cl1_0.student_id,cl1_1.id,cl1_1.name,cl1_1.type from student_courses cl1_0 join courses cl1_1 on cl1_1.id=cl1_0.course_id where cl1_0.student_id=?
        System.out.println(studentList); //[Student{id=1, name='Test Student 1', age=20}, Student{id=2, name='Test Student 2', age=20}]

        session2.close();

        //Если мы обратимся к связанной сущности после закрытия сессии, то получим ошибку LazyInitializationException
    }

    @Test
    @DisplayName("Сохранение Студента и Группы, и потом наблюдаем проблему N+1 запросов, когда у нас " +
            "установлено значение FetchType.EAGER")
    public void testSaveStudentAndGroup3() {
        //Создадим и сохраним группу
        Group group1 = groupService.saveGroup("1", 2023L);
        Student student1 = new Student("Test Student 1", 20, group1);
        Student student2 = new Student("Test Student 2", 20, group1);

        //Создадим и сохраним студентов с группами
        studentService.saveStudent(student1);
        studentService.saveStudent(student2);

        //получим данные о группах
        List<Group> allWithNPlusOneProblem = groupService.findAllWithNPlusOneProblem();
        // Тут наглядно видим, что у нас N+1 запрос (это когда FetchType.EAGER).
        // т.е. Hibernate сначала сделает запрос к группам, а потом для каждой группы сделает запрос к студентам:
        //Hibernate: select g1_0.id,g1_0.grad_year,g1_0.number from student_group g1_0
        //Hibernate: select sl1_0.group_id,sl1_0.id,sl1_0.student_age,sl1_0.name,p1_0.id,p1_0.bio,p1_0.last_seen_time from students sl1_0 left join profiles p1_0 on sl1_0.id=p1_0.student_id where sl1_0.group_id=?
        //Hibernate: select cl1_0.student_id,cl1_1.id,cl1_1.name,cl1_1.type from student_courses cl1_0 join courses cl1_1 on cl1_1.id=cl1_0.course_id where cl1_0.student_id=?
        //Hibernate: select cl1_0.student_id,cl1_1.id,cl1_1.name,cl1_1.type from student_courses cl1_0 join courses cl1_1 on cl1_1.id=cl1_0.course_id where cl1_0.student_id=?
    }


}