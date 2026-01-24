package mp.teamtask.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import mp.teamtask.domain.enums.TaskStage;

@Data
public class TaskDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private TaskStage stage;

    private Long assigneeId;

    private String assigneeName;
}