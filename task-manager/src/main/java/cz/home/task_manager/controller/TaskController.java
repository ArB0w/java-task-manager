package cz.home.task_manager.controller;

import cz.home.task_manager.model.Task;
import cz.home.task_manager.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public Task createTask(@RequestBody Task task) {
        return taskService.createTask(task);
    }

    // PUT – aktualizace úkolu
    @PutMapping("/{id}")
    public Task updateTask(
            @PathVariable Long id,
            @RequestBody Task task) {
        return taskService.updateTask(id, task);
    }

    // DELETE – odstranění úkolu
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}