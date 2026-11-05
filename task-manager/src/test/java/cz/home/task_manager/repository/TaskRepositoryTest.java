package cz.home.task_manager.repository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import cz.home.task_manager.model.Task;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void saveTask_shouldPersistTask() {
        Task task = new Task();
        task.setTitle("Integrační test");
        task.setDescription("Test ukládání do PostgreSQL");

        Task savedTask = taskRepository.save(task);

        assertNotNull(savedTask.getId());
        assertEquals("Integrační test", savedTask.getTitle());
    }

    @Test
    void findById_shouldReturnSavedTask() {
        Task task = new Task();
        task.setTitle("Test načítání");

        Task savedTask = taskRepository.save(task);

        Optional<Task> foundTask
                = taskRepository.findById(savedTask.getId());

        assertTrue(foundTask.isPresent());
        assertEquals("Test načítání", foundTask.get().getTitle());
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldFindMatchingTasks() {
        Task task = new Task();
        task.setTitle("Dokončit Java projekt");

        taskRepository.save(task);

        List<Task> results
                = taskRepository.findByTitleContainingIgnoreCase("JAVA");

        assertEquals(1, results.size());
        assertEquals("Dokončit Java projekt", results.get(0).getTitle());
    }

    @Test
    void findByCompleted_shouldReturnOnlyMatchingTasks() {
        Task completedTask = new Task();
        completedTask.setTitle("Hotový úkol");
        completedTask.setCompleted(true);

        Task pendingTask = new Task();
        pendingTask.setTitle("Nedokončený úkol");
        pendingTask.setCompleted(false);

        taskRepository.saveAll(List.of(completedTask, pendingTask));

        List<Task> results = taskRepository.findByCompleted(true);

        assertEquals(1, results.size());
        assertTrue(results.get(0).isCompleted());
    }

    @Test
    void findByCompletedAndTitleContainingIgnoreCase_shouldApplyBothFilters() {
        Task matchingTask = new Task();
        matchingTask.setTitle("Java projekt");
        matchingTask.setCompleted(true);

        Task wrongStatusTask = new Task();
        wrongStatusTask.setTitle("Java úkol");
        wrongStatusTask.setCompleted(false);

        Task wrongTitleTask = new Task();
        wrongTitleTask.setTitle("Python projekt");
        wrongTitleTask.setCompleted(true);

        taskRepository.saveAll(
                List.of(matchingTask, wrongStatusTask, wrongTitleTask)
        );

        List<Task> results
                = taskRepository.findByCompletedAndTitleContainingIgnoreCase(
                        true, "Java"
                );

        assertEquals(1, results.size());
        assertEquals("Java projekt", results.get(0).getTitle());
    }

    @Test
    void deleteTask_shouldRemoveTaskFromDatabase() {
        Task task = new Task();
        task.setTitle("Úkol ke smazání");

        Task savedTask = taskRepository.save(task);

        taskRepository.deleteById(savedTask.getId());

        assertFalse(taskRepository.existsById(savedTask.getId()));
    }
}
