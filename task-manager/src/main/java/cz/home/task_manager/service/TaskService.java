package cz.home.task_manager.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import cz.home.task_manager.model.Task;
import cz.home.task_manager.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
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
        return taskRepository.save(task);
    }

    // PUT – aktualizace úkolu
    public Task updateTask(Long id, Task task) {
        Task existingTask = getTaskById(id);

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setCompleted(task.isCompleted());
        existingTask.setDueDate(task.getDueDate());

        return taskRepository.save(existingTask);
    }

    // DELETE – odstranění úkolu
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }
}