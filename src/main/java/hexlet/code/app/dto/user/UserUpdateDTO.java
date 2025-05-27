package hexlet.code.app.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Setter
@Getter
public class UserUpdateDTO {

    @Email
    @NotNull
    private JsonNullable<String> email;

    @NotNull
    @NotBlank
    private JsonNullable<String> firstName;

    @NotNull
    @NotBlank
    private JsonNullable<String> lastName;

    @Size(min = 3)
    @NotNull
    private JsonNullable<String> password;
}
