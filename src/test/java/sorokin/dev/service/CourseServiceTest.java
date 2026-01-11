package sorokin.dev.service;

import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sorokin.dev.utils.TransactionHelper;
import sorokin.dev.config.TestHibernateConfiguration;
import sorokin.dev.entity.Course;
import sorokin.dev.entity.Group;
import sorokin.dev.entity.Student;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CourseServiceTest {

    private AnnotationConfigApplicationContext context;
    private StudentService studentService;
    private TransactionHelper transactionHelper;
    private CourseService courseService;
    private GroupService groupService;

    @BeforeAll
    public void setup() {
        context = new AnnotationConfigApplicationContext(
                TestHibernateConfiguration.class,
                CourseService.class,
                StudentService.class,
                TransactionHelper.class,
                GroupService.class
        );
        courseService = context.getBean(CourseService.class);
        studentService = context.getBean(StudentService.class);
        groupService = context.getBean(GroupService.class);
    }

    @AfterAll
    public void tearDown() {
        context.close();
    }

    @Test
    @DisplayName("Сохранение Студента и Курсы")
    public void testSaveStudentAndCourse() {
        //Создадим и сохраним несколько курсов, студентов, групп:

        Course course1 = courseService.saveCourse(new Course("math-1", "math"));
        Course course2 = courseService.saveCourse(new Course("math-2", "math"));

        Group group1 = groupService.saveGroup("1", 2023L);

        Student student1 = new Student("Test Student 1", 20, group1);
        Student student2 = new Student("Test Student 2", 20, group1);

        Student savedStudent1 = studentService.saveStudent(student1);
        Student savedStudent2 = studentService.saveStudent(student2);

        assertNotNull(savedStudent1.getId());
        assertEquals("Test Student 1", savedStudent1.getName());

        //зачислим студентов на курсы:

        courseService.enrollStudentToCourse(course1.getId(), savedStudent1.getId());
        courseService.enrollStudentToCourse(course2.getId(), savedStudent2.getId());
        //Hibernate: INSERT INTO student_courses (student_id, course_id)
        //VALUES (?, ?);

        //проверим, что студенты записаны на курсы:

        Student studentsOnCourse1 = studentService.getStudentById(savedStudent1.getId());
        Student studentsOnCourse2 = studentService.getStudentById(savedStudent2.getId());
        //Эти запросы сгенерировал Hibernate:
        //Hibernate: select s1_0.id,s1_0.student_age,g1_0.id,g1_0.grad_year,g1_0.number,s1_0.name,p1_0.id,p1_0.bio,p1_0.last_seen_time,cl1_0.student_id,cl1_1.id,cl1_1.name,cl1_1.type from students s1_0 left join student_group g1_0 on g1_0.id=s1_0.group_id left join profiles p1_0 on s1_0.id=p1_0.student_id left join student_courses cl1_0 on s1_0.id=cl1_0.student_id left join courses cl1_1 on cl1_1.id=cl1_0.course_id where s1_0.id=?
        //Hibernate: select sl1_0.group_id,sl1_0.id,sl1_0.student_age,sl1_0.name,p1_0.id,p1_0.bio,p1_0.last_seen_time from students sl1_0 left join profiles p1_0 on sl1_0.id=p1_0.student_id where sl1_0.group_id=?
        //Hibernate: select cl1_0.student_id,cl1_1.id,cl1_1.name,cl1_1.type from student_courses cl1_0 join courses cl1_1 on cl1_1.id=cl1_0.course_id where cl1_0.student_id=?
        //Hibernate: select s1_0.id,s1_0.student_age,g1_0.id,g1_0.grad_year,g1_0.number,s1_0.name,p1_0.id,p1_0.bio,p1_0.last_seen_time,cl1_0.student_id,cl1_1.id,cl1_1.name,cl1_1.type from students s1_0 left join student_group g1_0 on g1_0.id=s1_0.group_id left join profiles p1_0 on s1_0.id=p1_0.student_id left join student_courses cl1_0 on s1_0.id=cl1_0.student_id left join courses cl1_1 on cl1_1.id=cl1_0.course_id where s1_0.id=?
        //Hibernate: select sl1_0.group_id,sl1_0.id,sl1_0.student_age,sl1_0.name,p1_0.id,p1_0.bio,p1_0.last_seen_time from students sl1_0 left join profiles p1_0 on sl1_0.id=p1_0.student_id where sl1_0.group_id=?
        //Hibernate: select cl1_0.student_id,cl1_1.id,cl1_1.name,cl1_1.type from student_courses cl1_0 join courses cl1_1 on cl1_1.id=cl1_0.course_id where cl1_0.student_id=?

        System.out.println(studentsOnCourse1); //Student{id=1, name='Test Student 1', age=20}
        System.out.println(studentsOnCourse1.getCourseList()); //[Course{id=1, name='math-1', type='math'}]
        System.out.println(studentsOnCourse2); //Student{id=2, name='Test Student 2', age=20}
        System.out.println(studentsOnCourse2.getCourseList()); //[Course{id=2, name='math-2', type='math'}]
//
//        assertEquals(1, studentsOnCourse1.size());
//        assertEquals(1, studentsOnCourse2.size());
    }

}