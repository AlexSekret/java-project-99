package hexlet.code.app.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {

    private JsonNullable<Integer> index;

    // assignee.id in the Task entity
    private JsonNullable<Long> assigneeId;

    // name in Task entity
    @Size(min = 1)
    private JsonNullable<String> title;

    // description in Task entity
    private JsonNullable<String> content;

    // taskStatus.slug in the Task entity
    private JsonNullable<String> status;
    private JsonNullable<Set<Long>> taskLabelIds;
}
