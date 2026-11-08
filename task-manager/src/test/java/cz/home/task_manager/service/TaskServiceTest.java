package cz.home.task_manager.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import cz.home.task_manager.model.Task;
import cz.home.task_manager.model.User;
import cz.home.task_manager.repository.TaskRepository;
import cz.home.task_manager.repository.UserRepository;

class TaskServiceTest {

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "integration@test.com";

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserRepository userRepository;

    private TaskService taskService;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setEmail(USER_EMAIL);

        when(userRepository.findByEmail(USER_EMAIL))
                .thenReturn(Optional.of(testUser));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        USER_EMAIL,
                        null,
                        List.of()
                )
        );

        taskService = new TaskService(
                taskRepository,
                messagingTemplate,
                userRepository
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Task createTask(String title) {
        Task task = new Task();
        task.setId(1L);
        task.setTitle(title);
        task.setOwner(testUser);
        return task;
    }

    @Test
    void createTask_shouldSaveTaskAndSendWebSocketEvent() {
        Task task = new Task();
        task.setTitle("Testovací úkol");
        task.setDescription("Testovací popis");

        when(taskRepository.save(task)).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            savedTask.setId(1L);
            return savedTask;
        });

        Task result = taskService.createTask(task);

        assertEquals(1L, result.getId());
        assertEquals("Testovací úkol", result.getTitle());
        assertEquals(USER_ID, result.getOwner().getId());

        verify(taskRepository).save(task);

        verify(messagingTemplate).convertAndSendToUser(
                eq(USER_EMAIL),
                eq("/queue/tasks"),
                any()
        );
    }

    @Test
    void updateTask_shouldUpdateTaskAndSendWebSocketEvent() {
        Task existingTask = createTask("Původní název");
        existingTask.setDescription("Původní popis");

        Task updatedTask = new Task();
        updatedTask.setTitle("Nový název");
        updatedTask.setDescription("Nový popis");
        updatedTask.setCompleted(true);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(existingTask));

        when(taskRepository.save(existingTask))
                .thenReturn(existingTask);

        Task result = taskService.updateTask(1L, updatedTask);

        assertEquals("Nový název", result.getTitle());
        assertEquals("Nový popis", result.getDescription());
        assertTrue(result.isCompleted());

        verify(taskRepository).save(existingTask);

        verify(messagingTemplate).convertAndSendToUser(
                eq(USER_EMAIL),
                eq("/queue/tasks"),
                any()
        );
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        Task task1 = createTask("Úkol 1");
        Task task2 = createTask("Úkol 2");

        when(taskRepository.findByOwnerId(USER_ID))
                .thenReturn(List.of(task1, task2));

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        assertEquals("Úkol 1", result.get(0).getTitle());
        assertEquals("Úkol 2", result.get(1).getTitle());

        verify(taskRepository).findByOwnerId(USER_ID);
    }

    @Test
    void getTaskById_shouldReturnTask() {
        Task task = createTask("Testovací úkol");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertEquals("Testovací úkol", result.getTitle());

        verify(taskRepository).findById(1L);
    }

    @Test
    void getTaskById_shouldThrowNotFound() {
        when(taskRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.getTaskById(999L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void deleteTask_shouldDeleteTaskAndSendWebSocketEvent() {
        Task task = createTask("Úkol ke smazání");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);

        verify(messagingTemplate).convertAndSendToUser(
                eq(USER_EMAIL),
                eq("/queue/tasks"),
                any()
        );
    }

    @Test
    void updateTaskCompletion_shouldUpdateStatusAndSendWebSocketEvent() {
        Task task = createTask("Testovací úkol");
        task.setCompleted(false);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(task))
                .thenReturn(task);

        Task result = taskService.updateTaskCompletion(1L, true);

        assertTrue(result.isCompleted());

        verify(taskRepository).save(task);

        verify(messagingTemplate).convertAndSendToUser(
                eq(USER_EMAIL),
                eq("/queue/tasks"),
                any()
        );
    }

    @Test
    void searchTasks_shouldReturnMatchingTasks() {
        Task task = createTask("Java projekt");

        when(taskRepository.findByOwnerIdAndTitleContainingIgnoreCase(
                USER_ID,
                "Java"
        )).thenReturn(List.of(task));

        List<Task> result = taskService.searchTasks("Java");

        assertEquals(1, result.size());
        assertEquals("Java projekt", result.get(0).getTitle());

        verify(taskRepository)
                .findByOwnerIdAndTitleContainingIgnoreCase(USER_ID, "Java");
    }

    @Test
    void getTasksByCompleted_shouldReturnCompletedTasks() {
        Task task = createTask("Hotový úkol");
        task.setCompleted(true);

        when(taskRepository.findByOwnerIdAndCompleted(USER_ID, true))
                .thenReturn(List.of(task));

        List<Task> result = taskService.getTasksByCompleted(true);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isCompleted());

        verify(taskRepository).findByOwnerIdAndCompleted(USER_ID, true);
    }

    @Test
    void searchTasksByCompleted_shouldFilterByTitleAndStatus() {
        Task task = createTask("Java projekt");
        task.setCompleted(true);

        when(taskRepository
                .findByOwnerIdAndCompletedAndTitleContainingIgnoreCase(
                        USER_ID,
                        true,
                        "Java"
                )).thenReturn(List.of(task));

        List<Task> result
                = taskService.searchTasksByCompleted(true, "Java");

        assertEquals(1, result.size());
        assertEquals("Java projekt", result.get(0).getTitle());
        assertTrue(result.get(0).isCompleted());

        verify(taskRepository)
                .findByOwnerIdAndCompletedAndTitleContainingIgnoreCase(
                        USER_ID,
                        true,
                        "Java"
                );
    }
}
