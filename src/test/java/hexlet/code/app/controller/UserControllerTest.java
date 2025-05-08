package hexlet.code.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelGenerator;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Description;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
    public static final String API_USERS = "/api/users";
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper om;
    private User fakeUser;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .apply(springSecurity()) // добавляем Spring Security
                .build();

        fakeUser = Instancio.of(modelGenerator.getUserModel()).create();
        userRepository.save(fakeUser);
        token = jwt().jwt(builder -> builder.subject(fakeUser.getEmail()));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
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
        long id = fakeUser.getId();
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
    @Description("when GET to API_USERS check response status, content type, repository")
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
    @Description("when GET to API_USERS/{id} check response status, content type, repository")
    void testShow() throws Exception {
//        TODO: rewrite
        MockHttpServletRequestBuilder request = get(API_USERS + "/" + fakeUser.getId()).with(token);
        MockHttpServletResponse result = this.mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();
        String body = result.getContentAsString();
        assertThatJson(body).isObject().containsKeys("id", "email", "firstName", "lastName");
        assertThatJson(body).and(
                v -> v.node("id").isEqualTo(fakeUser.getId()),
                v -> v.node("email").isEqualTo(fakeUser.getEmail()),
                v -> v.node("firstName").isEqualTo(fakeUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(fakeUser.getLastName()));
    }

    @Test
    @Description("POST to API_USERS should create new user and return 201 status")
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
    @Description("PUT to API_USERS/{id} should update user and return 200 status")
    void testUpdate() throws Exception {
        UserUpdateDTO updateData = new UserUpdateDTO();
        updateData.setEmail(JsonNullable.of("updated@example.com"));
        long id = fakeUser.getId();
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
    @Description("DELETE to API_USERS/{id} should remove user and return 204 status")
    void testDelete() throws Exception {
        mockMvc.perform(delete(API_USERS + "/" + fakeUser.getId()).with(token))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(fakeUser.getId())).isFalse();

        mockMvc.perform(get(API_USERS + "/" + fakeUser.getId()).with(token))
                .andExpect(status().isNotFound());
    }

    @Test
    @Description("when GET to API_USERS/{id} check that there is no password in the body of the response")
    void testPasswordNotExposed() throws Exception {
        mockMvc.perform(get(API_USERS + "/" + fakeUser.getId()))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
