package mp.teamtask.service;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.TaskStage;
import mp.teamtask.repository.TaskStageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskStageService {
    private final TaskStageRepository taskStageRepository;

    public List<TaskStage> getAllStages() { return taskStageRepository.findAll(); }

    public TaskStage getOrCreateStage(String name) {
        return taskStageRepository.findByName(name)
                .orElseGet(() -> taskStageRepository.save(new TaskStage(null, name)));
    }
}
