package hexlet.code.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Description;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper om;
    private User fakeUser;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8).build();

        fakeUser = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPassword), () -> faker.internet().password(6, 100))
                .create();
        userRepository.save(fakeUser);
    }

    @Test
    public void testWelcome() throws Exception {
        MockHttpServletRequestBuilder request = get("/welcome");
        MockHttpServletResponse response = this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse();
        String content = response.getContentAsString();
        String expected = "Welcome to Spring!";
        assertThat(content).isEqualTo(expected);
    }

    @Test
    @Description("when GET to /api/users check response status, content type, repository")
    public void testIndex() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/users");
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
        // Проверяем, что все ожидаемые пользователи присутствуют в ответе
        expected.forEach(expectedUser -> {
            UserDTO act = actual.stream()
                    .filter(dto -> dto.getId().equals(expectedUser.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("User not found in response: " + expectedUser.getId()));
            // Проверяем соответствие полей
            assertThat(act.getEmail()).isEqualTo(expectedUser.getEmail());
            assertThat(act.getFirstName()).isEqualTo(expectedUser.getFirstName());
            assertThat(act.getLastName()).isEqualTo(expectedUser.getLastName());
        });
    }

    @Test
    @Description("when GET to /api/users/{id} check response status, content type, repository")
    public void testShow() throws Exception {
        MockHttpServletRequestBuilder request = get("/api/users/" + fakeUser.getId());
        MockHttpServletResponse result = this.mockMvc.perform(request)
                .andExpect(status().isOk()).andReturn().getResponse();
        String body = result.getContentAsString();
        UserDTO actual = om.readValue(body, new TypeReference<>() {
        });
        assertThatJson(body).isObject().containsKeys("id", "email", "firstName", "lastName");
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(fakeUser.getId()),
                v -> v.node("email").isEqualTo(fakeUser.getEmail()),
                v -> v.node("firstName").isEqualTo(fakeUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(fakeUser.getLastName()));
    }

    @Test
    @Description("POST to /api/users should create new user and return 201 status")
    public void testCreate() throws Exception {
        UserCreateDTO newUser = new UserCreateDTO();
        newUser.setEmail("test@example.com");
        newUser.setFirstName("Test");
        newUser.setLastName("User");
        newUser.setPassword("password123");
        MockHttpServletRequestBuilder request = post("/api/users")
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
    @Description("PUT to /api/users/{id} should update user and return 200 status")
    public void testUpdate() throws Exception {
        UserUpdateDTO updateData = new UserUpdateDTO();
        updateData.setEmail(JsonNullable.of("updated@example.com"));
        long id = fakeUser.getId();
        MockHttpServletRequestBuilder request = put("/api/users/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

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
    @Description("DELETE to /api/users/{id} should remove user and return 204 status")
    public void testDelete() throws Exception {
        mockMvc.perform(delete("/api/users/" + fakeUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(fakeUser.getId())).isFalse();

        mockMvc.perform(get("/api/users/" + fakeUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @Description("when GET to /api/users/{id} check that there is no password in the body of the response")
    public void testPasswordNotExposed() throws Exception {
        mockMvc.perform(get("/api/users/" + fakeUser.getId()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
