package hexlet.code.util;

import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserUtils {
    private final UserRepository userRepository;
    private static final String USER_WITH_EMAIL_NOT_FOUND = "User with email %s not found";

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).
                orElseThrow(() -> new ResourceNotFoundException(String.format(USER_WITH_EMAIL_NOT_FOUND, email)));
    }

    public boolean isCurrentUser(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).
                orElseThrow(() -> new ResourceNotFoundException(String.format(USER_WITH_EMAIL_NOT_FOUND, email)));
        Long authUserId = user.getId();
        User currentUser = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(String.format(USER_WITH_EMAIL_NOT_FOUND, email)));
        Long currentUserId = currentUser.getId();
        return authUserId.equals(currentUserId);
    }

}
