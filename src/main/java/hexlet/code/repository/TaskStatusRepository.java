package hexlet.code.repository;

import hexlet.code.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long>,
        PagingAndSortingRepository<TaskStatus, Long> {
    Optional<TaskStatus> findBySlug(String slug);
}
