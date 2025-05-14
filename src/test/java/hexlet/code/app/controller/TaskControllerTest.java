package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.TaskCreateDTO;
import hexlet.code.app.dto.TaskDTO;
import hexlet.code.app.dto.TaskUpdateDTO;
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
        this.mockMvc.perform(request)
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
                .andExpect(jsonPath("$.[*].assigneeId").value(hasItem(task.getAssignee().getId().intValue())))
                .andExpect(jsonPath("$.[*].title").value(hasItem(task.getName())))
                .andExpect(jsonPath("$.[*].content").value(hasItem(task.getDescription())))
                .andExpect(jsonPath("$.[*].status").value(hasItem(task.getTaskStatus().getSlug())));

    }

    @Test
    void createTest() throws Exception {
        TaskCreateDTO newTask = new TaskCreateDTO();
        newTask.setIndex(1);
        newTask.setAssigneeId(user.getId());
        newTask.setTitle("New very important Task");
        newTask.setContent("New very important Task content");
        newTask.setStatus("draft");
        MockHttpServletRequestBuilder createRequest = post(API_TASKS).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newTask));
        String response = this.mockMvc.perform(createRequest)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.index").value(newTask.getIndex()))
                .andExpect(jsonPath("$.assigneeId").value(newTask.getAssigneeId()))
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
    void updateAndDdeleteNonExistingTaskShould404Test() throws Exception {
        MockHttpServletRequestBuilder request = delete(API_TASKS + "/" + NON_EXISTING_TASK).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isNotFound());

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
                .andExpect(jsonPath("$.assigneeId").value(updateDTO.getAssigneeId().get()))
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
}
