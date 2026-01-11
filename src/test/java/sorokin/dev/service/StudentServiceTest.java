package sorokin.dev.service;

import org.junit.jupiter.api.*;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sorokin.dev.utils.TransactionHelper;
import sorokin.dev.config.TestHibernateConfiguration;
import sorokin.dev.entity.Student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StudentServiceTest {

    private AnnotationConfigApplicationContext context;
    private StudentService studentService;

    @BeforeAll
    public void setup() {
        context = new AnnotationConfigApplicationContext(
                TestHibernateConfiguration.class,
                //sorokin.dev.repository.StudentRepository.class,
                TransactionHelper.class,
                StudentService.class
        );
        studentService = context.getBean(StudentService.class);
    }

    @AfterAll
    public void tearDown() {
        context.close();
    }

    @Test
    @DisplayName("Сохранение и поиск студента по ID")
    public void testSaveAndFindStudent() {
        Student student = new Student("Test Student", 20, null);
        Student saved = studentService.saveStudent(student);

        assertNotNull(saved.getId());
        assertEquals("Test Student", saved.getName());

        Student found = studentService.getStudentById(saved.getId());
        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    @DisplayName("Получение всех студентов")
    public void testFindAllStudentsStudents() {
        studentService.saveStudent(new Student("Alice", 21, null));
        studentService.saveStudent(new Student("Bob", 22, null));

        var all = studentService.findAllStudents();
        assertEquals(2, all.size());
    }

}