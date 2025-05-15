package hexlet.code.app.repository;

import hexlet.code.app.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, Long>, PagingAndSortingRepository<Label, Long> {
    Optional<Label> findByName(String name);
}
