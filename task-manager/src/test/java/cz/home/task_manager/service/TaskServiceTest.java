package cz.home.task_manager.service;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.server.ResponseStatusException;

import cz.home.task_manager.dto.TaskEvent;
import cz.home.task_manager.model.Task;
import cz.home.task_manager.repository.TaskRepository;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        taskService = new TaskService(
                taskRepository,
                messagingTemplate
        );
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

        verify(taskRepository).save(task);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/tasks"),
                any(TaskEvent.class)
        );

    }

    @Test
    void updateTask_shouldUpdateTaskAndSendWebSocketEvent() {
        Task existingTask = new Task();
        existingTask.setTitle("Původní název");
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
        assertEquals(true, result.isCompleted());

        verify(taskRepository).save(existingTask);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/tasks"),
                any(TaskEvent.class)
        );
    }

    @Test
    void getAllTasks_shouldReturnAllTasks() {
        Task task1 = new Task();
        task1.setTitle("Úkol 1");

        Task task2 = new Task();
        task2.setTitle("Úkol 2");

        when(taskRepository.findAll())
                .thenReturn(List.of(task1, task2));

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        assertEquals("Úkol 1", result.get(0).getTitle());
        assertEquals("Úkol 2", result.get(1).getTitle());

        verify(taskRepository).findAll();
    }

    @Test
    void getTaskById_shouldReturnTask() {
        Task task = new Task();
        task.setTitle("Testovací úkol");

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
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Úkol ke smazání");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/tasks"),
                any(TaskEvent.class)
        );
    }

    @Test
    void updateTaskCompletion_shouldUpdateStatusAndSendWebSocketEvent() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Testovací úkol");
        task.setCompleted(false);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(task))
                .thenReturn(task);

        Task result = taskService.updateTaskCompletion(1L, true);

        assertTrue(result.isCompleted());

        verify(taskRepository).save(task);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/tasks"),
                any(TaskEvent.class)
        );
    }

    @Test
    void searchTasks_shouldReturnMatchingTasks() {
        Task task = new Task();
        task.setTitle("Java projekt");

        when(taskRepository.findByTitleContainingIgnoreCase("Java"))
                .thenReturn(List.of(task));

        List<Task> result = taskService.searchTasks("Java");

        assertEquals(1, result.size());
        assertEquals("Java projekt", result.get(0).getTitle());

        verify(taskRepository)
                .findByTitleContainingIgnoreCase("Java");
    }

    @Test
    void getTasksByCompleted_shouldReturnCompletedTasks() {
        Task task = new Task();
        task.setTitle("Hotový úkol");
        task.setCompleted(true);

        when(taskRepository.findByCompleted(true))
                .thenReturn(List.of(task));

        List<Task> result = taskService.getTasksByCompleted(true);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isCompleted());

        verify(taskRepository).findByCompleted(true);
    }

    @Test
    void searchTasksByCompleted_shouldFilterByTitleAndStatus() {
        Task task = new Task();
        task.setTitle("Java projekt");
        task.setCompleted(true);

        when(taskRepository.findByCompletedAndTitleContainingIgnoreCase(
                true,
                "Java"
        )).thenReturn(List.of(task));

        List<Task> result
                = taskService.searchTasksByCompleted(true, "Java");

        assertEquals(1, result.size());
        assertEquals("Java projekt", result.get(0).getTitle());
        assertTrue(result.get(0).isCompleted());

        verify(taskRepository)
                .findByCompletedAndTitleContainingIgnoreCase(true, "Java");
    }
}
