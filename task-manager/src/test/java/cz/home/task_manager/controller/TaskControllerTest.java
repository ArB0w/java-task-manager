package cz.home.task_manager.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cz.home.task_manager.model.Task;
import cz.home.task_manager.service.TaskService;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void getTasks_shouldReturnAllTasks() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Testovací úkol");

        when(taskService.getAllTasks())
                .thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title")
                        .value("Testovací úkol"));
    }

    @Test
    void searchTasks_shouldPassFiltersToService() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Java projekt");
        task.setCompleted(true);

        when(taskService.searchTasksByCompleted(true, "Java"))
                .thenReturn(List.of(task));

        mockMvc.perform(get("/api/tasks/search")
                .param("title", "Java")
                .param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title")
                        .value("Java projekt"))
                .andExpect(jsonPath("$[0].completed")
                        .value(true));

        verify(taskService)
                .searchTasksByCompleted(true, "Java");
    }

    @Test
    void createTask_shouldReturnCreated() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Nový úkol");

        when(taskService.createTask(any(Task.class)))
                .thenReturn(task);

        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content("""
                                {
                                    "title": "Nový úkol",
                                    "description": "Testovací popis"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Nový úkol"));

        verify(taskService).createTask(any(Task.class));
    }

    @Test
    void updateTaskCompletion_shouldReturnUpdatedTask() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Testovací úkol");
        task.setCompleted(true);

        when(taskService.updateTaskCompletion(1L, true))
                .thenReturn(task);

        mockMvc.perform(patch("/api/tasks/1/completed")
                .contentType("application/json")
                .content("""
                                {
                                    "completed": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        verify(taskService).updateTaskCompletion(1L, true);
    }

    @Test
    void deleteTask_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L);
    }

    @Test
    void createTask_shouldReturnBadRequestForEmptyTitle()
            throws Exception {

        mockMvc.perform(post("/api/tasks")
                .contentType("application/json")
                .content("""
                                {
                                    "title": "",
                                    "description": "Testovací popis"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any(Task.class));
    }
}
