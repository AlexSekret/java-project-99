package hexlet.code.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import static jakarta.persistence.GenerationType.IDENTITY;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class Task implements BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Size(min = 1)
    private String name;
    private Integer index;
    private String description;

    @ManyToOne
    @JoinColumn(name = "task_status_id")
    private TaskStatus taskStatus;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "task_label",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "label_id"))
    private Set<Label> labels = new HashSet<>();
    @CreatedDate
    private LocalDate createdAt;

    public void addTaskStatus(TaskStatus newStatus) {
        // Удаляем связь с текущим статусом
        if (this.taskStatus != null) {
            this.taskStatus.getTasks().remove(this);
        }

        // Устанавливаем новую связь
        this.taskStatus = newStatus;

        // Обновляем обратную сторону
        if (newStatus != null) {
            newStatus.getTasks().add(this);
        }
    }

    public void addAssignee(User newAssignee) {
        // Удаляем связь с текущим пользователем
        if (this.assignee != null) {
            this.assignee.getTasks().remove(this);
        }

        // Устанавливаем новую связь
        this.assignee = newAssignee;

        // Обновляем обратную сторону
        if (newAssignee != null) {
            newAssignee.getTasks().add(this);
        }
    }

    public void addLabel(Label label) {
        Objects.requireNonNull(label, "Label cannot be null");
        if (!this.labels.contains(label)) {
            this.labels.add(label);
            if (!label.getTasks().contains(this)) {
                label.getTasks().add(this);
            }
        }
    }

    public void removeLabel(Label label) {
        Objects.requireNonNull(label, "Label cannot be null");
        if (this.labels.remove(label)) {
            label.getTasks().remove(this);
        }
    }
}
