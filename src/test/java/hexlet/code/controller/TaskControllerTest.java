package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import static org.hamcrest.Matchers.hasItem;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "sentry.enabled=false",
        "sentry.dsn="
})
class TaskControllerTest {
    public static final String API_TASKS = "/api/tasks";
    public static final int NON_EXISTING_TASK = 9999;
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
    @Autowired
    private LabelRepository labelRepository;
    private User user;
    private Task task;
    private Task filteredTask;
    private TaskStatus status;
    private Label label;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;


    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity()) // добавляем Spring Security
                .build();

        user = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(user);
        status = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(status);
        label = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(label);

        task = Instancio.of(modelGenerator.getTaskModel()).create();
        task.setAssignee(user);
        task.setTaskStatus(status);
        task.setLabels(Set.of(label));
        taskRepository.save(task);

        filteredTask = Instancio.of(modelGenerator.getTaskModel()).create();
        filteredTask.setAssignee(user);
        filteredTask.setTaskStatus(status);
        taskRepository.save(filteredTask);

        token = jwt().jwt(builder -> builder.subject(user.getEmail()));
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void showTest() throws Exception {
        MockHttpServletRequestBuilder request = get(API_TASKS + "/" + task.getId()).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.index").value(task.getIndex()))
                .andExpect(jsonPath("$.createdAt").value(task.getCreatedAt().toString()))
                .andExpect(jsonPath("$.assignee_id").value(task.getAssignee().getId()))
                .andExpect(jsonPath("$.title").value(task.getName()))
                .andExpect(jsonPath("$.content").value(task.getDescription()))
                .andExpect(jsonPath("$.status").value(task.getTaskStatus().getSlug()));

    }

    @Test
    void unauthorizedActionsShouldFailTest() throws Exception {
        Task newTask = Instancio.of(modelGenerator.getTaskModel()).create();
        MockHttpServletRequestBuilder showRequest = get(API_TASKS + "/" + task.getId());
        MockHttpServletRequestBuilder createRequest = post(API_TASKS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTask));
        MockHttpServletRequestBuilder deleteRequest = delete(API_TASKS + "/" + task.getId());
        MockHttpServletRequestBuilder updateRequest = put(API_TASKS + "/" + task.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTask));
        this.mockMvc.perform(showRequest).andExpect(status().isUnauthorized());
        this.mockMvc.perform(createRequest).andExpect(status().isUnauthorized());
        this.mockMvc.perform(updateRequest).andExpect(status().isUnauthorized());
        this.mockMvc.perform(deleteRequest).andExpect(status().isUnauthorized());
    }

    @Test
    void indexTest() throws Exception {
        MockHttpServletRequestBuilder request = get(API_TASKS).with(token);
        this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.[*].id").value(hasItem(task.getId().intValue())))
                .andExpect(jsonPath("$.[*].index").value(hasItem(task.getIndex())))
                .andExpect(jsonPath("$.[*].assignee_id").value(hasItem(task.getAssignee().getId().intValue())))
                .andExpect(jsonPath("$.[*].title").value(hasItem(task.getName())))
                .andExpect(jsonPath("$.[*].content").value(hasItem(task.getDescription())))
                .andExpect(jsonPath("$.[*].status").value(hasItem(task.getTaskStatus().getSlug())));

    }

    @Test
    void createTest() throws Exception {
        String slug = taskStatusRepository.findBySlug(status.getSlug())
                .orElseThrow(() -> new AssertionError("TaskStatus not found")).getSlug();
        TaskCreateDTO newTask = new TaskCreateDTO();
        newTask.setIndex(1);
        newTask.setAssigneeId(user.getId());
        newTask.setTitle("New very important Task");
        newTask.setContent("New very important Task content");
        newTask.setStatus(slug);
        newTask.getTaskLabelIds().add(label.getId());
        String content = om.writeValueAsString(newTask);
        MockHttpServletRequestBuilder createRequest = post(API_TASKS).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
        String response = this.mockMvc.perform(createRequest)
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.index").value(newTask.getIndex()))
                .andExpect(jsonPath("$.assignee_id").value(newTask.getAssigneeId()))
                .andExpect(jsonPath("$.title").value(newTask.getTitle()))
                .andExpect(jsonPath("$.content").value(newTask.getContent()))
                .andExpect(jsonPath("$.status").value(newTask.getStatus()))
                .andReturn().getResponse().getContentAsString();
        TaskDTO taskDTO = om.readValue(response, TaskDTO.class);
        assertTrue(taskRepository.existsById(taskDTO.getId()));
        Task taskFromDB = taskRepository.findById(taskDTO.getId()).
                orElseThrow(() -> new AssertionError("Task not found"));
        assertEquals(taskDTO.getId(), taskFromDB.getId());
        assertEquals(taskDTO.getStatus(), taskFromDB.getTaskStatus().getSlug());
        assertEquals(taskDTO.getIndex(), taskFromDB.getIndex());
        assertEquals(taskDTO.getAssigneeId(), taskFromDB.getAssignee().getId());
        assertEquals(taskDTO.getTitle(), taskFromDB.getName());
        assertEquals(taskDTO.getContent(), taskFromDB.getDescription());
        assertEquals(taskDTO.getCreatedAt(), taskFromDB.getCreatedAt());
    }

    @Test
    void deleteTest() throws Exception {
        MockHttpServletRequestBuilder request = delete(API_TASKS + "/" + task.getId()).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isNoContent());
        assertTrue(taskRepository.findById(task.getId()).isEmpty());
    }

    @Test
    void deleteNonExistentTaskShould404Test() throws Exception {
        MockHttpServletRequestBuilder request = delete(API_TASKS + "/" + NON_EXISTING_TASK).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    void updateNonExistingTaskShould404Test() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setIndex(JsonNullable.of(2));
        updateDTO.setAssigneeId(JsonNullable.of(user.getId()));
        updateDTO.setTitle(JsonNullable.of("Updated title"));
        updateDTO.setContent(JsonNullable.of("Updated content"));
        updateDTO.setStatus(JsonNullable.of("published"));
        MockHttpServletRequestBuilder updateRequest = put(API_TASKS + "/" + NON_EXISTING_TASK).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));
        this.mockMvc.perform(updateRequest).andExpect(status().isNotFound());
    }

    @Test
    void updateTest() throws Exception {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        updateDTO.setIndex(JsonNullable.of(2));
        updateDTO.setAssigneeId(JsonNullable.of(user.getId()));
        updateDTO.setTitle(JsonNullable.of("Updated title"));
        updateDTO.setContent(JsonNullable.of("Updated content"));
        updateDTO.setStatus(JsonNullable.of("published"));
        MockHttpServletRequestBuilder request = put(API_TASKS + "/" + task.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateDTO));
        String response = this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.index").value(updateDTO.getIndex().get()))
                .andExpect(jsonPath("$.assignee_id").value(updateDTO.getAssigneeId().get()))
                .andExpect(jsonPath("$.title").value(updateDTO.getTitle().get()))
                .andExpect(jsonPath("$.content").value(updateDTO.getContent().get()))
                .andExpect(jsonPath("$.status").value(updateDTO.getStatus().get()))
                .andReturn().getResponse().getContentAsString();
        TaskDTO taskDTO = om.readValue(response, TaskDTO.class);
        assertTrue(taskRepository.existsById(taskDTO.getId()));
        Task taskFromDB = taskRepository.findById(taskDTO.getId()).
                orElseThrow(() -> new AssertionError("Task not found"));
        assertEquals(taskDTO.getId(), taskFromDB.getId());
        assertEquals(taskDTO.getStatus(), taskFromDB.getTaskStatus().getSlug());
        assertEquals(taskDTO.getIndex(), taskFromDB.getIndex());
        assertEquals(taskDTO.getAssigneeId(), taskFromDB.getAssignee().getId());
        assertEquals(taskDTO.getTitle(), taskFromDB.getName());
        assertEquals(taskDTO.getContent(), taskFromDB.getDescription());
        assertEquals(taskDTO.getCreatedAt(), taskFromDB.getCreatedAt());
    }

    @Test
    void filteredIndexTest() throws Exception {
        // /api/tasks?titleCont=create&assigneeId=1&status=to_be_fixed&labelId=1

        MockHttpServletRequestBuilder request = get("/api/tasks").with(token)
                .param("titleCont", task.getName())
                .param("assigneeId", user.getId().toString())
                .param("status", status.getSlug())
                .param("labelId", label.getId().toString());
        this.mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(task.getId()))
                .andExpect(jsonPath("$[0].index").value(task.getIndex()))
                .andExpect(jsonPath("$[0].assignee_id").value(task.getAssignee().getId()))
                .andExpect(jsonPath("$[0].title").value(task.getName()))
                .andExpect(jsonPath("$[0].content").value(task.getDescription()))
                .andExpect(jsonPath("$[0].status").value(task.getTaskStatus().getSlug()));
    }
}
