package hexlet.code.service;

import hexlet.code.dto.user.UserCreateDTO;
import hexlet.code.dto.user.UserDTO;
import hexlet.code.dto.user.UserUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDTO getById(Long id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(id, "User"));
        return userMapper.map(user);
    }

    public void deleteById(Long id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(id, "User"));
        userRepository.delete(user);
    }

    public UserDTO create(UserCreateDTO userDTO) {
        User user = userMapper.map(userDTO);
        userRepository.save(user);
        return userMapper.map(user);
    }

    public UserDTO update(Long id, UserUpdateDTO userDTO) {
        User userData = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "User"));
        userMapper.update(userDTO, userData);
        userRepository.save(userData);
        return userMapper.map(userData);
    }

    public Page<UserDTO> getPage(Pageable page) {
        Page<User> users = userRepository.findAll(page);
        return users.map(userMapper::map);
    }
}
