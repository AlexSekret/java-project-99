package hexlet.code.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.LabelCreateDTO;
import hexlet.code.app.dto.LabelUpdateDTO;
import hexlet.code.app.model.Label;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LabelControllerTest {
    public static final String API_LABELS = "/api/labels";
    public static final int NON_EXIST_LABEL = 9999;
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;
    private Label label;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private User user;
    private TaskStatus status;
    private Task task;
    @Autowired
    private ModelGenerator modelGenerator;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ObjectMapper om;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity())
                .build();
        user = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(user);


        status = Instancio.of(modelGenerator.getTaskStatusModel()).create();
        taskStatusRepository.save(status);
        label = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(label);

        task = Instancio.of(modelGenerator.getTaskModel())
                .set(Select.field(Task::getTaskStatus), status)
                .set(Select.field(Task::getAssignee), user)
                .create();


        task.addLabel(label);
        user.addTask(task);
        status.addTask(task);
        taskRepository.save(task);
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }

    @Test
    void showTest() throws Exception {
        MockHttpServletRequestBuilder request = get(API_LABELS + "/" + label.getId()).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(label.getId()))
                .andExpect(jsonPath("$.name").value(label.getName()));
    }

    @Test
    void showNonExistentTest() throws Exception {
        MockHttpServletRequestBuilder request = get(API_LABELS + "/" + NON_EXIST_LABEL).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isNotFound());

    }

    @Test
    void indexTest() throws Exception {
        MockHttpServletRequestBuilder request = get(API_LABELS).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(label.getId()))
                .andExpect(jsonPath("$[0].name").value(label.getName()));
    }

    @Test
    void createTest() throws Exception {
        LabelCreateDTO newLabel = new LabelCreateDTO();
        newLabel.setName("newLabel");
        MockHttpServletRequestBuilder request = post(API_LABELS).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newLabel));
        this.mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(newLabel.getName()));
        assertTrue(labelRepository.findByName(newLabel.getName()).isPresent());
        Long id = labelRepository.findByName(newLabel.getName()).get().getId();
        assertEquals(labelRepository.findById(id).get().getName(), newLabel.getName());

    }

    @Test
    void updateTest() throws Exception {
        String oldName = label.getName();
        LabelUpdateDTO update = new LabelUpdateDTO();
        update.setName("newLabel");
        MockHttpServletRequestBuilder request = put(API_LABELS + "/" + label.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(update));
        this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(label.getId()))
                .andExpect(jsonPath("$.name").value(update.getName()));
        assertEquals(labelRepository.findById(label.getId()).get().getName(), update.getName());
    }

    @Test
    void updateNonExistingLabelTest() throws Exception {
        String oldName = label.getName();
        LabelUpdateDTO update = new LabelUpdateDTO();
        update.setName("newLabel");
        MockHttpServletRequestBuilder request = put(API_LABELS + "/" + NON_EXIST_LABEL).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(update));
        this.mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTest() throws Exception {
        Label newLabel = Instancio.of(modelGenerator.getLabelModel()).create();
        labelRepository.save(newLabel);
        MockHttpServletRequestBuilder request = delete(API_LABELS + "/" + newLabel.getId()).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isNoContent());
        assertTrue(labelRepository.findById(newLabel.getId()).isEmpty());
    }

    @Test
    void deleteLabelWithTaskTest() throws Exception {
        MockHttpServletRequestBuilder request = delete(API_LABELS + "/" + label.getId()).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isBadRequest());
        assertTrue(labelRepository.findById(label.getId()).isPresent());
    }

    @Test
    void deleteNonExistentLabelTest() throws Exception {
        MockHttpServletRequestBuilder request = delete(API_LABELS + "/" + NON_EXIST_LABEL).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthorizedActionsShouldFailTest() throws Exception {
        Label label = Instancio.of(modelGenerator.getLabelModel()).create();
        MockHttpServletRequestBuilder showRequest = get(API_LABELS + "/" + label.getId());
        MockHttpServletRequestBuilder createRequest = post(API_LABELS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(label));
        MockHttpServletRequestBuilder deleteRequest = delete(API_LABELS + "/" + label.getId());
        MockHttpServletRequestBuilder updateRequest = put(API_LABELS + "/" + label.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(label));
        this.mockMvc.perform(showRequest).andExpect(status().isUnauthorized());
        this.mockMvc.perform(createRequest).andExpect(status().isUnauthorized());
        this.mockMvc.perform(updateRequest).andExpect(status().isUnauthorized());
        this.mockMvc.perform(deleteRequest).andExpect(status().isUnauthorized());
    }
}
