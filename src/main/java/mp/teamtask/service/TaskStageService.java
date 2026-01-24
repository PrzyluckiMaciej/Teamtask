package mp.teamtask.service;

import jakarta.transaction.Transactional;
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

    public TaskStage getDefaultStage() {
        return taskStageRepository.findAll().stream()
                .filter(TaskStage::isDefault)
                .findFirst()
                .orElseGet(() -> taskStageRepository.findByName("NEW")
                        .orElseThrow(() -> new RuntimeException("No default stage configured")));
    }

    @Transactional
    public void setDefaultStage(Long id) {
        taskStageRepository.findAll().forEach(s -> s.setDefault(false));
        TaskStage newDefault = taskStageRepository.findById(id).orElseThrow();
        newDefault.setDefault(true);
        taskStageRepository.save(newDefault);
    }

    public TaskStage getOrCreateStage(String name, String color, boolean isDefault) {
        return taskStageRepository.findByName(name)
                .orElseGet(() -> taskStageRepository.save(new TaskStage(null, name, color, isDefault)));
    }
}
