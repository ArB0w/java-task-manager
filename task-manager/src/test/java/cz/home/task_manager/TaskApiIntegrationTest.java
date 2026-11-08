package cz.home.task_manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import cz.home.task_manager.model.User;
import cz.home.task_manager.repository.UserRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "integration@test.com")
class TaskApiIntegrationTest {

    private static final String TEST_EMAIL = "integration@test.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.findByEmail(TEST_EMAIL)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(TEST_EMAIL);
                    user.setPassword("$2a$10$test");
                    return userRepository.save(user);
                });
    }

    @Test
    void taskLifecycle_shouldWorkCorrectly() throws Exception {

        // 1. Vytvoření úkolu
        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                .with(user(TEST_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Integrační test",
                            "description": "Test celé aplikace"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integrační test"))
                .andReturn();

        JsonNode createdTask = objectMapper.readTree(
                createResult.getResponse().getContentAsString()
        );

        long taskId = createdTask.get("id").asLong();

        // 2. Načtení úkolu
        mockMvc.perform(get("/api/tasks/" + taskId)
                .with(user(TEST_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integrační test"));

        // 3. Aktualizace úkolu
        mockMvc.perform(put("/api/tasks/" + taskId)
                .with(user(TEST_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Aktualizovaný úkol",
                            "description": "Nový popis",
                            "completed": false
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Aktualizovaný úkol"));

        // 4. Označení úkolu jako dokončeného
        mockMvc.perform(patch("/api/tasks/" + taskId + "/completed")
                .with(user(TEST_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "completed": true
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        // 5. Smazání úkolu
        mockMvc.perform(delete("/api/tasks/" + taskId)
                .with(user(TEST_EMAIL)))
                .andExpect(status().isNoContent());

        // 6. Ověření, že úkol už neexistuje
        mockMvc.perform(get("/api/tasks/" + taskId)
                .with(user(TEST_EMAIL)))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchTasks_shouldFindTaskInDatabase() throws Exception {

        mockMvc.perform(post("/api/tasks")
                .with(user(TEST_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Vyhledávání Java"
                        }
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/search")
                .with(user(TEST_EMAIL))
                .param("title", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title")
                        .value("Vyhledávání Java"));
    }

    @Test
    void createTask_shouldRejectEmptyTitle() throws Exception {

        mockMvc.perform(post("/api/tasks")
                .with(user(TEST_EMAIL))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "",
                            "description": "Neplatný úkol"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }
}
