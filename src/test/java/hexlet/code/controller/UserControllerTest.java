package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
        "sentry.enabled=false",
        "sentry.dsn="
})
class UserControllerTest {
    public static final String API_USERS = "/api/users";
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
    private User userWithNoTasks;
    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userWithNoTasksToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity()) // добавляем Spring Security
                .build();

        user = Instancio.of(modelGenerator.getUserModel()).create();
        userWithNoTasks = Instancio.of(modelGenerator.getUserModel()).create();

        status = Instancio.of(modelGenerator.getTaskStatusModel()).create();

        task = Instancio.of(modelGenerator.getTaskModel())
                .set(Select.field(Task::getTaskStatus), status)
                .set(Select.field(Task::getAssignee), user)
                .create();
        task.addAssignee(user);
        task.addTaskStatus(status);
        taskRepository.save(task);
        userRepository.save(user);
        taskStatusRepository.save(status);
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));
        userWithNoTasksToken = jwt().jwt(builder -> builder.subject(userWithNoTasks.getEmail()));
        userRepository.save(userWithNoTasks);
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll(); // Сначала удаляем Task
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }

    @Test
    void unauthenticatedAccessShouldFail() throws Exception {
        MockHttpServletRequestBuilder request = get(API_USERS);
        this.mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

    }

    @Test
    void authenticatedAccessShouldSuccess() throws Exception {
        MockHttpServletRequestBuilder request = get(API_USERS).with(token);
        this.mockMvc.perform(request)
                .andExpect(status().isOk());

    }

    @Test
    void unAuthenticatedActionsShouldFail() throws Exception {
        long id = user.getId();
        MockHttpServletRequestBuilder request1 = delete(API_USERS + "/" + id);
        this.mockMvc.perform(request1)
                .andExpect(status().isUnauthorized());
        this.mockMvc.perform(post(API_USERS))
                .andExpect(status().isUnauthorized());

        UserUpdateDTO updateData = new UserUpdateDTO();
        updateData.setEmail(JsonNullable.of("updated@example.com"));
        String jsonData = om.writeValueAsString(updateData);
        MockHttpServletRequestBuilder request2 = put(API_USERS + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonData);

        this.mockMvc.perform(request2)
                .andExpect(status().isUnauthorized());
        UserCreateDTO newUser = new UserCreateDTO();
        newUser.setEmail("test@example.com");
        newUser.setFirstName("Test");
        newUser.setLastName("User");
        newUser.setPassword("password123");
        MockHttpServletRequestBuilder request3 = post(API_USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newUser));
        this.mockMvc.perform(request3)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testWelcome() throws Exception {
        MockHttpServletRequestBuilder request = get("/welcome");
        MockHttpServletResponse response = this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse();
        String content = response.getContentAsString();
        String expected = "Welcome to Spring!";
        assertThat(content).isEqualTo(expected);
    }

    @Test
    void testIndex() throws Exception {
        //        TODO: rewrite
        MockHttpServletRequestBuilder request = get(API_USERS).with(token);
        MockHttpServletResponse result = this.mockMvc.perform(request)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        String body = result.getContentAsString();
        List<UserDTO> actual = om.readValue(body, new TypeReference<>() {
        }); // конвертирует JSON в список UserDTO

        List<User> expected = userRepository.findAll();
        assertEquals(expected.size(), actual.size()); //а нахер это надо?

        expected.forEach(expectedUser -> {
            UserDTO act = actual.stream()
                    .filter(dto -> dto.getId().equals(expectedUser.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("User not found in response: " + expectedUser.getId()));

            assertThat(act.getEmail()).isEqualTo(expectedUser.getEmail());
            assertThat(act.getFirstName()).isEqualTo(expectedUser.getFirstName());
            assertThat(act.getLastName()).isEqualTo(expectedUser.getLastName());
        });
    }

    @Test
    void testShow() throws Exception {
//        TODO: rewrite
        MockHttpServletRequestBuilder request = get(API_USERS + "/" + user.getId()).with(token);
        MockHttpServletResponse result = this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        String body = result.getContentAsString();
        assertThatJson(body).isObject().containsKeys("id", "email", "firstName", "lastName");
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(user.getId()),
                v -> v.node("email").isEqualTo(user.getEmail()),
                v -> v.node("firstName").isEqualTo(user.getFirstName()),
                v -> v.node("lastName").isEqualTo(user.getLastName()));
    }

    @Test
    void testCreate() throws Exception {
        UserCreateDTO newUser = new UserCreateDTO();
        newUser.setEmail("test@example.com");
        newUser.setFirstName("Test");
        newUser.setLastName("User");
        newUser.setPassword("password123");
        MockHttpServletRequestBuilder request = post(API_USERS).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(newUser));

        MockHttpServletResponse response = this.mockMvc.perform(request)
                .andExpectAll(status().isCreated(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        String body = response.getContentAsString();
        UserDTO createdUser = om.readValue(body, UserDTO.class);
        assertThat(createdUser.getEmail()).isEqualTo(newUser.getEmail());
        assertThat(createdUser.getFirstName()).isEqualTo(newUser.getFirstName());
        assertThat(createdUser.getLastName()).isEqualTo(newUser.getLastName());

        User dbUser = userRepository.findById(createdUser.getId())
                .orElseThrow(() -> new AssertionError("User with id " + createdUser.getId() + " not found"));
        assertThat(dbUser.getEmail()).isEqualTo(newUser.getEmail());
    }

    @Test
    void testUpdate() throws Exception {
        UserUpdateDTO updateData = new UserUpdateDTO();
        User u = user;
        updateData.setEmail(JsonNullable.of("updated@example.com"));
        long id = user.getId();
        String jsonData = om.writeValueAsString(updateData);
        MockHttpServletRequestBuilder request = put(API_USERS + "/" + id).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonData);

        MockHttpServletResponse response = mockMvc.perform(request)
                .andExpectAll(status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        String body = response.getContentAsString();
        UserDTO updatedUser = om.readValue(body, UserDTO.class);

        User actual = userRepository.findById(id).get();
        assertThat(updatedUser.getEmail()).isEqualTo(actual.getEmail());

    }

    @Test
    void testDelete() throws Exception {
        userRepository.save(userWithNoTasks);
        mockMvc.perform(delete(API_USERS + "/" + userWithNoTasks.getId()).with(userWithNoTasksToken))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(userWithNoTasks.getId())).isFalse();

        mockMvc.perform(get(API_USERS + "/" + userWithNoTasks.getId()).with(userWithNoTasksToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteWithTasks() throws Exception {
        mockMvc.perform(delete(API_USERS + "/" + user.getId()).with(token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPasswordNotExposed() throws Exception {
        mockMvc.perform(get(API_USERS + "/" + user.getId()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
