package mp.teamtask.service;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.TaskType;
import mp.teamtask.repository.TaskTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskTypeService {

    private final TaskTypeRepository taskTypeRepository;

    public List<TaskType> getAllTaskTypes() {
        return taskTypeRepository.findAll();
    }

    public TaskType getTaskTypeById(Long id) {
        return taskTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("TaskType not found with ID: " + id));
    }

    public Optional<TaskType> findByType(String type) {
        return taskTypeRepository.findByType(type);
    }

    public TaskType saveTaskType(TaskType taskType) {
        return taskTypeRepository.save(taskType);
    }

    public TaskType getOrCreateTaskType(String type) {
        return taskTypeRepository.findByType(type)
                .orElseGet(() -> taskTypeRepository.save(new TaskType(null, type)));
    }

    public void deleteTaskType(Long id) {
        taskTypeRepository.deleteById(id);
    }
}