package cz.home.task_manager.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cz.home.task_manager.dto.TaskEvent;
import cz.home.task_manager.model.Task;
import cz.home.task_manager.model.User;
import cz.home.task_manager.repository.TaskRepository;
import cz.home.task_manager.repository.UserRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            SimpMessagingTemplate messagingTemplate,
            UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    // GET – načtení všech úkolů
    public List<Task> getAllTasks() {
        User currentUser = getCurrentUser();

        return taskRepository.findByOwnerId(currentUser.getId());
    }

    // GET – načtení úkolu podle ID
    public Task getTaskById(Long id) {

        User currentUser = getCurrentUser();

        return taskRepository.findById(id)
                .filter(task -> task.getOwner().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"
                ));
    }

    // POST – vytvoření nového úkolu
    public Task createTask(Task task) {
        task.setOwner(getCurrentUser());

        Task savedTask = taskRepository.save(task);

        sendTaskEvent("created", savedTask.getId());

        return savedTask;
    }

    // PUT – aktualizace úkolu
    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = getTaskById(id);

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setCompleted(updatedTask.isCompleted());
        existingTask.setDueDate(updatedTask.getDueDate());

        Task savedTask = taskRepository.save(existingTask);

        sendTaskEvent("updated", savedTask.getId());

        return savedTask;
    }

    // PUT – aktualizace úkolu dotovo
    public Task updateTaskCompletion(Long id, boolean completed) {
        Task task = getTaskById(id);
        task.setCompleted(completed);

        Task updatedTask = taskRepository.save(task);

        sendTaskEvent("updated", updatedTask.getId());

        return updatedTask;
    }

    // DELETE – odstranění úkolu
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);

        sendTaskEvent("deleted", task.getId());
    }

    private void sendTaskEvent(String type, Long taskId) {
        TaskEvent event = new TaskEvent(type, taskId);

        String email = getCurrentUser().getEmail();

        messagingTemplate.convertAndSendToUser(
                email,
                "/queue/tasks",
                event
        );
    }

    public List<Task> searchTasks(String title) {
        User currentUser = getCurrentUser();

        return taskRepository.findByOwnerIdAndTitleContainingIgnoreCase(
                currentUser.getId(), title
        );
    }

    public List<Task> getTasksByCompleted(boolean completed) {
        User currentUser = getCurrentUser();

        return taskRepository.findByOwnerIdAndCompleted(
                currentUser.getId(), completed
        );
    }

    public List<Task> searchTasksByCompleted(boolean completed, String title) {
        User currentUser = getCurrentUser();

        return taskRepository
                .findByOwnerIdAndCompletedAndTitleContainingIgnoreCase(
                        currentUser.getId(), completed, title
                );
    }

    private User getCurrentUser() {

        Authentication authentication
                = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));
    }

}
