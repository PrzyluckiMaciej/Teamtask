package mp.teamtask.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "task_stages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private boolean isDefault = false;

    private Integer position;
}