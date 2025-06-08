package hexlet.code.component;

import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final CustomUserDetailsService userDetailsService;
    private final TaskStatusRepository taskRepository;
    private final LabelRepository labelRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String email = "hexlet@example.com";
        if (!userDetailsService.userExists(email)) {
            String password = "qwerty";
            User userData = new User();
            userData.setEmail(email);
            userData.setPasswordDigest(password);
            userDetailsService.createUser(userData);
        }

        ArrayList<String> defaultStatuses = new ArrayList<>(List.of("draft", "to_review",
                "to_be_fixed", "to_publish", "published"));
        List<String> currentStatuses = taskRepository.findAll().stream()
                .map(TaskStatus::getSlug)
                .toList();
        defaultStatuses.forEach(status -> {
            if (!currentStatuses.contains(status)) {
                TaskStatus taskStatus = new TaskStatus();
                taskStatus.setName(status);
                taskStatus.setSlug(status);
                taskRepository.save(taskStatus);
            }
        });
        Set<String> labels = new HashSet<>(Set.of("feature", "bug"));
        labels.forEach(label -> {
            if (labelRepository.findByName(label).isEmpty()) {
                Label l = new Label();
                l.setName(label);
                labelRepository.save(l);
            }
        });
    }
}
