package hexlet.code.app.controller;


import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserParamsDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.service.CustomUserDetailsService;
import hexlet.code.app.service.UserService;
import hexlet.code.app.specification.UserSpecification;
import hexlet.code.app.util.UserUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserUtils userUtils;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserSpecification userSpecification;

    @GetMapping(path = "/users")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserDTO>> index(UserParamsDTO params, @RequestParam(defaultValue = "1") int page) {
        Specification<User> spec = userSpecification.build(params);
        Page<UserDTO> userPage = userService.getPages(spec, PageRequest.of(page - 1, 10));
        // Добавляем заголовок X-Total-Count
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(userPage.getTotalElements()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(userPage.getContent());
    }

    @GetMapping(path = "/users/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO show(@PathVariable Long id) {
        return userService.getById(id);
    }

    @DeleteMapping(path = "/users/{id}")
    @PreAuthorize("@userUtils.isCurrentUser(#id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.deleteById(id);
    }

    @PostMapping(path = "/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO userDTO) {
        return userService.create(userDTO);
    }

    @PutMapping(path = "/users/{id}")
    @PreAuthorize("@userUtils.isCurrentUser(#id)")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userDTO) {
        return userService.update(id, userDTO);
    }
}
