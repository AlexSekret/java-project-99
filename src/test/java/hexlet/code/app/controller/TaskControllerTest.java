package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import static org.hamcrest.Matchers.hasItem;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class TaskControllerTest {
    public static final String API_TASKS = "/api/tasks";
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    private User user;
    private Task task;
    private TaskStatus status;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;


    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity()) // добавляем Spring Security
                .build();

        user = Instancio.of(modelGenerator.getUserModel()).create();

        status = Instancio.of(modelGenerator.getTaskStatusModel()).create();

        task = Instancio.of(modelGenerator.getTaskModel())
                .set(Select.field(Task::getTaskStatus), status)
                .set(Select.field(Task::getAssignee), user)
                .create();
        user.addTask(task);
        status.addTask(task);
        taskRepository.save(task);
        userRepository.save(user);
        taskStatusRepository.save(status);

        token = jwt().jwt(builder -> builder.subject(user.getEmail()));
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll(); // Сначала удаляем Task
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }

    @Test
    void showTest() throws Exception {
        MockHttpServletRequestBuilder request = get(API_TASKS + "/" + task.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.index").value(task.getIndex()))
                .andExpect(jsonPath("$.createdAt").value(task.getCreatedAt().toString()))
                .andExpect(jsonPath("$.assigneeId").value(task.getAssignee().getId()))
                .andExpect(jsonPath("$.title").value(task.getName()))
                .andExpect(jsonPath("$.content").value(task.getDescription()))
                .andExpect(jsonPath("$.status").value(task.getTaskStatus().getSlug()));

    }

    @Test
    void indexTest() throws Exception {
        MockHttpServletRequestBuilder request = get(API_TASKS).with(token);
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[*].id").value(hasItem(task.getId().intValue())))
                .andExpect(jsonPath("$.[*].index").value(hasItem(task.getIndex())))
                .andExpect(jsonPath("$.[*].assigneeId").value(hasItem(task.getAssignee().getId().intValue())))
                .andExpect(jsonPath("$.[*].title").value(hasItem(task.getName())))
                .andExpect(jsonPath("$.[*].content").value(hasItem(task.getDescription())))
                .andExpect(jsonPath("$.[*].status").value(hasItem(task.getTaskStatus().getSlug())));

    }
//    просматривать, добавлять, редактировать задачи могли только залогиненные пользователи
}