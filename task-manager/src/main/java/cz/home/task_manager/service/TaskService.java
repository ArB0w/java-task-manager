package cz.home.task_manager.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cz.home.task_manager.dto.TaskEvent;
import cz.home.task_manager.model.Task;
import cz.home.task_manager.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final SimpMessagingTemplate messagingTemplate;

    public TaskService(
            TaskRepository taskRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.taskRepository = taskRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // GET – načtení všech úkolů
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // GET – načtení úkolu podle ID
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found with id: " + id
                ));
    }

    // POST – vytvoření nového úkolu
    public Task createTask(Task task) {
        Task savedTask = taskRepository.save(task);

        sendTaskEvent("TASK_CREATED", savedTask.getId());

        return savedTask;
    }

    // PUT – aktualizace úkolu
    public Task updateTask(Long id, Task task) {
        Task existingTask = getTaskById(id);

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setCompleted(task.isCompleted());
        existingTask.setDueDate(task.getDueDate());

        Task updatedTask = taskRepository.save(existingTask);

        sendTaskEvent("TASK_UPDATED", updatedTask.getId());

        return updatedTask;
    }

    // DELETE – odstranění úkolu
    public void deleteTask(Long id) {
        Task task = getTaskById(id);

        taskRepository.delete(task);

        sendTaskEvent("TASK_DELETED", id);
    }

    private void sendTaskEvent(String type, Long taskId) {
        TaskEvent event = new TaskEvent(type, taskId);

        messagingTemplate.convertAndSend("/topic/tasks", event);
    }
}
