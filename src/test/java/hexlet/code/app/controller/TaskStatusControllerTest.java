package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.TaskStatusCreateDTO;
import hexlet.code.app.dto.TaskStatusDTO;
import hexlet.code.app.dto.TaskStatusUpdateDTO;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class TaskStatusControllerTest {
    public static final String API_TASK_STATUSES = "/api/task_statuses";
    public static final String DEFAULT_QUERY = "?_end=10&_order=ASC&_sort=id&_start=0";
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ObjectMapper om;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private UserRepository userRepository;
    private TaskStatus status; // fake task status
    private Task task;
    private User user;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private TaskStatus status1;

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
        userRepository.save(user);
        taskStatusRepository.save(status);
        taskRepository.save(task);
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));
        status1 = new TaskStatus();
        status1.setName("Status 1");
        status1.setSlug("status_1");
        taskStatusRepository.save(status1);
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll(); // Сначала удаляем Task
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }

    @Test
    void testShow() throws Exception {
        MockHttpServletRequestBuilder request = get(API_TASK_STATUSES + "/" + status.getId()).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value(status.getName()))
                .andExpect(jsonPath("$.slug").value(status.getSlug()));
    }

    //    GET /api/task_statuses
    @Test
    void testIndex() throws Exception {

        List<TaskStatus> statuses = taskStatusRepository.findAll();
        assertTrue(statuses.stream().anyMatch(s -> s.getName().equals("Status 1")));
        assertTrue(statuses.stream().anyMatch(s -> s.getSlug().equals("status_1")));
        MockHttpServletRequestBuilder request = get(API_TASK_STATUSES + DEFAULT_QUERY).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].name").value(hasItem("Status 1")))
                .andExpect(jsonPath("$[*].slug").value(hasItem("status_1")))
                .andExpect(jsonPath("$[*].name").value(hasItem(status.getName())))
                .andExpect(jsonPath("$[*].slug").value(hasItem(status.getSlug())));
    }

    @Test
    void testCreate() throws Exception {
        TaskStatusCreateDTO status = new TaskStatusCreateDTO();
        status.setName("To Create");
        status.setSlug("to_Create");
        MockHttpServletRequestBuilder request = post(API_TASK_STATUSES).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(status));
        this.mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(status.getName()))
                .andExpect(jsonPath("$.slug").value(status.getSlug()));
        assertTrue(taskStatusRepository.findBySlug(status.getSlug()).isPresent());
    }

    @Test
    void testUpdate() throws Exception {
        Map<String, String> updates = new HashMap<>();
        updates.put("name", "New Name");
        updates.put("slug", "new_slug");
        MockHttpServletRequestBuilder request = put(API_TASK_STATUSES + "/" + status.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updates));

        this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.slug").value("new_slug"));
        String actualName = taskStatusRepository.findById(status.getId()).get().getName();
        String actualSlug = taskStatusRepository.findById(status.getId()).get().getSlug();
        Long actualId = taskStatusRepository.findById(status.getId()).get().getId();
        assertEquals(updates.get("name"), actualName);
        assertEquals(updates.get("slug"), actualSlug);
        assertEquals(status.getId(), actualId);
    }

    @Test
    void testPartialUpdate() throws Exception {
        Long actualId = taskStatusRepository.findById(status.getId()).get().getId();
        Map<String, String> updateName = new HashMap<>();
        updateName.put("name", "New Name");
        MockHttpServletRequestBuilder updateNameRequest = put(API_TASK_STATUSES + "/" + status.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateName));
        String originalSlug = taskStatusRepository.findById(status.getId()).get().getSlug();
        this.mockMvc.perform(updateNameRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateName.get("name")))
                .andExpect(jsonPath("$.slug").value(originalSlug));
        String actualName = taskStatusRepository.findById(status.getId()).get().getName();
        assertEquals(updateName.get("name"), actualName);
        Map<String, String> updateSlug = new HashMap<>();
        updateSlug.put("slug", "new_slug");
        MockHttpServletRequestBuilder updateSlugRequest = put(API_TASK_STATUSES + "/" + status.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateSlug));
        this.mockMvc.perform(updateSlugRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateName.get("name")))
                .andExpect(jsonPath("$.slug").value(updateSlug.get("slug")));

        String updatedSlug = taskStatusRepository.findById(status.getId()).get().getSlug();
        assertEquals(updateSlug.get("slug"), updatedSlug);
        assertEquals(status.getId(), actualId);
    }


    @Test
    void testDelete() throws Exception {
        TaskStatus status = new TaskStatus();
        status.setName("To Delete");
        status.setSlug("to_delete");
        TaskStatus savedStatus = taskStatusRepository.save(status);

        MockHttpServletRequestBuilder request = delete(API_TASK_STATUSES + "/" + savedStatus.getId())
                .with(token);

        this.mockMvc.perform(request)
                .andExpect(status().isNoContent());
        assertFalse(taskStatusRepository.existsById(savedStatus.getId()));
    }

    @Test
    void shouldNotAllowUnauthorizedAccess() throws Exception {
        //create
        TaskStatusDTO createDto = new TaskStatusDTO();
        createDto.setName("To Create");
        createDto.setSlug("to_Create");
        String createData = om.writeValueAsString(createDto);
        MockHttpServletRequestBuilder createRequest = post(API_TASK_STATUSES)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createData);
        this.mockMvc.perform(createRequest)
                .andExpect(status().isUnauthorized());
        //update
        TaskStatusUpdateDTO updateData = new TaskStatusUpdateDTO();
        updateData.setSlug(JsonNullable.of("new_slug"));
        String jsonData = om.writeValueAsString(updateData);
        MockHttpServletRequestBuilder updateRequest = put(API_TASK_STATUSES + "/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonData);
        this.mockMvc.perform(updateRequest)
                .andExpect(status().isUnauthorized());
        //delete
        MockHttpServletRequestBuilder deleteRequest = delete(API_TASK_STATUSES + "/" + user.getId());
        this.mockMvc.perform(deleteRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotCreateStatusWithDuplicateSlug() throws Exception {
        TaskStatusDTO createDto = new TaskStatusDTO();
        createDto.setName("To Create");
        createDto.setSlug(status.getSlug());
        this.mockMvc.perform(post(API_TASK_STATUSES).with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdateStatusByDuplicateSlug() throws Exception {
        String originName = taskStatusRepository.findById(status.getId()).get().getName();
        TaskStatusUpdateDTO updateDto = new TaskStatusUpdateDTO();
        updateDto.setName(JsonNullable.of("To Create"));
        updateDto.setSlug(JsonNullable.of(status.getSlug()));
        this.mockMvc.perform(put(API_TASK_STATUSES + "/" + status.getId()).with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
        String actualName = taskStatusRepository.findById(status.getId()).get().getName();
        assertEquals(originName, actualName);
    }

    @Test
    void shouldReturnNotFoundForNonExistingStatus() throws Exception {
        long nonExistingId = 999L;

        mockMvc.perform(get(API_TASK_STATUSES + "/" + nonExistingId).with(token))
                .andExpect(status().isNotFound());

        mockMvc.perform(put(API_TASK_STATUSES + "/" + nonExistingId).with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(Map.of("name", "new_name"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotCreateStatusWithEmptyName() throws Exception {
        TaskStatusCreateDTO status = new TaskStatusCreateDTO();
        status.setName("");  // Пустое имя
        status.setSlug("valid_slug");

        mockMvc.perform(post(API_TASK_STATUSES).with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(status)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotUpdateStatusWithEmptySlug() throws Exception {
        Map<String, String> updates = new HashMap<>();
        updates.put("slug", "");  // Пустой slug

        mockMvc.perform(put(API_TASK_STATUSES + "/" + status.getId()).with(token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updates)))
                .andExpect(status().isBadRequest());
    }
}
