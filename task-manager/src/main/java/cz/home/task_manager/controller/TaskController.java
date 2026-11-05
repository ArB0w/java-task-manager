package cz.home.task_manager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public Task createTask(@Valid @RequestBody Task task) {
        return taskService.createTask(task);
    }

    // PUT – aktualizace úkolu
    @PutMapping("/{id}")
    public Task updateTask(
            @PathVariable Long id,
            @Valid @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    // DELETE – odstranění úkolu
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
