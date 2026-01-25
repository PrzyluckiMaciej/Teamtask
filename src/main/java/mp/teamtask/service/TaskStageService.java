package mp.teamtask.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.TaskStage;
import mp.teamtask.repository.TaskRepository;
import mp.teamtask.repository.TaskStageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskStageService {
    private final TaskStageRepository taskStageRepository;
    private final TaskRepository taskRepository;

    public List<TaskStage> getAllStages() {
        return taskStageRepository.findAllByOrderByPositionAsc();
    }

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

    public TaskStage getOrCreateStage(String name, String color, Integer position, boolean isDefault) {
        return taskStageRepository.findByName(name)
                .orElseGet(() -> taskStageRepository.save(new TaskStage(null, name, color, isDefault, position)));
    }

    public TaskStage getStageById(Long id) {
        return taskStageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Stage not found with ID: " + id));
    }

    @Transactional
    public void updateStage(Long id, String name, String color, Integer position) {
        TaskStage stage = getStageById(id);
        stage.setName(name);
        stage.setColor(color);
        stage.setPosition(position); // Update position
        taskStageRepository.save(stage);
    }

    @Transactional
    public void deleteStage(Long id) {
        TaskStage stage = getStageById(id);

        if (stage.isDefault()) {
            throw new IllegalStateException("The default stage cannot be deleted.");
        }

        if (taskRepository.existsByStage(stage)) {
            throw new IllegalStateException("Cannot delete a stage that is currently assigned to tasks.");
        }

        taskStageRepository.delete(stage);
    }

    public boolean isStageInUse(Long id) {
        TaskStage stage = taskStageRepository.findById(id).orElseThrow();
        return taskRepository.existsByStage(stage);
    }
}
