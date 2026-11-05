package cz.home.task_manager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import cz.home.task_manager.model.Task;
import cz.home.task_manager.service.TaskService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // GET – načtení všech úkolů
    @GetMapping
    public List<Task> getTasks() {
        return taskService.getAllTasks();
    }

    // GET – načtení úkolu podle ID
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    // POST – vytvoření nového úkolu
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        Task createdTask = taskService.createTask(task);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdTask);
    }

    // PUT – aktualizace úkolu
    @PutMapping("/{id}")
    public Task updateTask(
            @PathVariable Long id,
            @Valid @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    @PatchMapping("/{id}/completed")
    public Task updateTaskCompletion(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {

        Boolean completed = request.get("completed");

        if (completed == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missing completed field"
            );
        }

        return taskService.updateTaskCompletion(id, completed);
    }

    // DELETE – odstranění úkolu
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<Task> searchTasks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean completed) {

        if (title != null && completed != null) {
            return taskService.searchTasksByCompleted(completed, title);
        }

        if (title != null) {
            return taskService.searchTasks(title);
        }

        if (completed != null) {
            return taskService.getTasksByCompleted(completed);
        }

        return taskService.getAllTasks();
    }
}
