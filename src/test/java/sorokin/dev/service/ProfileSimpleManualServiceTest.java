package sorokin.dev.service;

import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sorokin.dev.config.TestHibernateConfiguration;
import sorokin.dev.entity.Profile;
import sorokin.dev.entity.Student;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProfileSimpleManualServiceTest {

    private AnnotationConfigApplicationContext context;
    private ProfileSimpleManualService profileService;
    private StudentSimpleManualService studentService;

    @BeforeAll
    public void setup() {
        context = new AnnotationConfigApplicationContext(
                TestHibernateConfiguration.class,
                ProfileSimpleManualService.class,
                StudentSimpleManualService.class
        );
        profileService = context.getBean(ProfileSimpleManualService.class);
        studentService = context.getBean(StudentSimpleManualService.class);
    }

    @AfterAll
    public void tearDown() {
        context.close();
    }

    @Test
    @DisplayName("Сохранение Студента и его Профиля и поиск студента по ID")
    public void testSaveAndFindProfile() {
        Student student = new Student("Test Student", 20, null);

        Student savedStudent = studentService.saveStudent(student);

        assertNotNull(savedStudent.getId());
        assertEquals("Test Student", savedStudent.getName());

        Student studentById = studentService.getStudentById(savedStudent.getId());
        assertNotNull(studentById);
        assertEquals(savedStudent.getId(), studentById.getId());

        //Создадим профиль для студента:
        Profile profile = new Profile("My bio", LocalDateTime.now(), student);
        Profile saveProfile = profileService.saveProfile(profile); //Hibernate: insert into profiles (bio,last_seen_time,student_id,id) values (?,?,?,default)
        System.out.println(saveProfile);
        //Profile{id=1, bio='My bio', lastSeenTime=2026-01-10T16:51:26.304001800, student=Student{id=1, name='Test Student', age=20}}

        //Получение профиля
        Profile profileById = profileService.getProfileById(saveProfile.getId());
        //В запросе hibernate сам выполнил left join: "from profiles p1_0 left join students s1_0 on s1_0.id=p1_0.student_id"
        //Hibernate: select p1_0.id,p1_0.bio,p1_0.last_seen_time,s1_0.id,s1_0.student_age,g1_0.id,g1_0.grad_year,g1_0.number,s1_0.name,cl1_0.student_id,cl1_1.id,cl1_1.name,cl1_1.type from profiles p1_0 left join students s1_0 on s1_0.id=p1_0.student_id left join student_group g1_0 on g1_0.id=s1_0.group_id left join student_courses cl1_0 on s1_0.id=cl1_0.student_id left join courses cl1_1 on cl1_1.id=cl1_0.course_id where p1_0.id=?
        System.out.println(profileById);
        //Profile{id=1, bio='My bio', lastSeenTime=2026-01-10T16:51:26.304001800, student=Student{id=1, name='Test Student', age=20}}

        //Получение студента:
        Student studentById1 = studentService.getStudentById(savedStudent.getId());
        //В запросе hibernate сам выполнил left join: from students s1_0 left join profiles p1_0 on s1_0.id=p1_0.student_id
         //Hibernate: select s1_0.id,s1_0.student_age,g1_0.id,g1_0.grad_year,g1_0.number,s1_0.name,p1_0.id,p1_0.bio,p1_0.last_seen_time,cl1_0.student_id,cl1_1.id,cl1_1.name,cl1_1.type from students s1_0 left join student_group g1_0 on g1_0.id=s1_0.group_id left join profiles p1_0 on s1_0.id=p1_0.student_id left join student_courses cl1_0 on s1_0.id=cl1_0.student_id left join courses cl1_1 on cl1_1.id=cl1_0.course_id where s1_0.id=?
        System.out.println("==getProfile==: " + studentById1.getProfile());
        //==getProfile==: Profile{id=1, bio='My bio', lastSeenTime=2026-01-10T17:31:14.103479, student=Student{id=1, name='Test Student', age=20}}
    }

    @Test
    @DisplayName("Удаление Студента и по цепочке его Профиля")
    public void testRemoveStudentAndHisProfileCascade() {
        Student student = new Student("Test Student", 20, null);

        Student savedStudent = studentService.saveStudent(student);

        assertNotNull(savedStudent.getId());
        assertEquals("Test Student", savedStudent.getName());

        //Создадим профиль для студента:
        Profile profile = new Profile("My bio", LocalDateTime.now(), student);
        Profile savedProfile = profileService.saveProfile(profile); //Hibernate: insert into profiles (bio,last_seen_time,student_id,id) values (?,?,?,default)
        System.out.println(savedProfile);
        //Profile{id=1, bio='My bio', lastSeenTime=2026-01-10T16:51:26.304001800, student=Student{id=1, name='Test Student', age=20}}

        studentService.deleteStudent(savedStudent.getId());
        //видим два запроса на удаление:
        //Hibernate: delete from profiles where id=?
        //Hibernate: delete from students where id=?
        assertNull(studentService.getStudentById(savedStudent.getId()));
        assertNull(profileService.getProfileById(savedProfile.getId()));
    }

    @Test
    @DisplayName("Удаление только Профиля")
    public void testRemoveOnlyProfile() {
        Student student = new Student("Test Student", 20, null);

        Student savedStudent = studentService.saveStudent(student);

        assertNotNull(savedStudent.getId());
        assertEquals("Test Student", savedStudent.getName());

        //Создадим профиль для студента:
        Profile profile = new Profile("My bio", LocalDateTime.now(), student);
        Profile savedProfile = profileService.saveProfile(profile); //Hibernate: insert into profiles (bio,last_seen_time,student_id,id) values (?,?,?,default)
        System.out.println(savedProfile);
        //Profile{id=1, bio='My bio', lastSeenTime=2026-01-10T16:51:26.304001800, student=Student{id=1, name='Test Student', age=20}}

        profileService.deleteProfileById(savedProfile.getId());
        //Hibernate: delete from profiles where id=?

        Student studentById = studentService.getStudentById(savedStudent.getId());
        assertNotNull(studentById);
        System.out.println(studentById); //Student{id=1, name='Test Student', age=20}
        assertNull(studentById.getProfile());
        System.out.println(studentById.getProfile()); //null
    }

    @Test
    @DisplayName("Получение всех студентов")
    public void testFindAllStudentsStudents() {
        studentService.saveStudent(new Student("Alice", 21, null));
        studentService.saveStudent(new Student("Bob", 22, null));

        var all = studentService.findAll();
        assertEquals(2, all.size());
    }
}