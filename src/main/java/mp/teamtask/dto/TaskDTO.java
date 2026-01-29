package mp.teamtask.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private Long stageId;

    private Long assigneeId;

    private String assigneeName;

    private Long severityId;
    private String severityName;

    private Long fixVersionId;
    private String fixVersionName;

    private Long taskTypeId;
    private String taskTypeName;
}