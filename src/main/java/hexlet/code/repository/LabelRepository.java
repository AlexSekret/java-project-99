package hexlet.code.repository;

import hexlet.code.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LabelRepository extends JpaRepository<Label, Long>, PagingAndSortingRepository<Label, Long> {
    Optional<Label> findByName(String name);

    Set<Label> findByIdIn(Collection<Long> ids);
}
