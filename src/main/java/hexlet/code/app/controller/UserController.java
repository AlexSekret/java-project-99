package hexlet.code.app.controller;


import hexlet.code.app.dto.UserCreateDTO;
import hexlet.code.app.dto.UserDTO;
import hexlet.code.app.dto.UserUpdateDTO;
import hexlet.code.app.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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


    @GetMapping(path = "/users")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<UserDTO>> index(
            @RequestParam(name = "_end", defaultValue = "5") int end,
            @RequestParam(name = "_start", defaultValue = "0") int start,
            @RequestParam(name = "_sort", defaultValue = "id") String sort,
            @RequestParam(name = "_order", defaultValue = "ASC") String order) {
        int perPage = end - start;
        int pageNumber = (int) Math.ceil((double) start / perPage);
        Sort.Direction sotrDirection = Sort.Direction.valueOf(order);
        Pageable pageRequest = PageRequest.of(pageNumber, perPage, sotrDirection, sort);
        Page<UserDTO> userPages = userService.getPage(pageRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(userPages.getTotalElements()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(userPages.getContent());
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
