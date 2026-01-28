package mp.teamtask.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileDTO {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String currentPassword = "";

    @Size(min = 0, max = 100, message = "Password cannot exceed 100 characters")
    @Pattern(
            regexp = "^(|(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+)$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String newPassword = "";

    private String confirmPassword = "";
}