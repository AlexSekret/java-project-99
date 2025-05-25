package hexlet.code.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TaskDTO {

    private Long id;

    private Integer index;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    @JsonProperty(value = "assignee_id")
    private Long assigneeId;

    @Size(min = 1)
    private String title;

    private String content;

    private String status;
}
