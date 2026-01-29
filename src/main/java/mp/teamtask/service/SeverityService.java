package mp.teamtask.service;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.Severity;
import mp.teamtask.repository.SeverityRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeverityService {

    private final SeverityRepository severityRepository;

    public List<Severity> getAllSeverities() {
        return severityRepository.findAll();
    }

    public Severity getSeverityById(Long id) {
        return severityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Severity not found with ID: " + id));
    }

    public Optional<Severity> findBySeverity(String severity) {
        return severityRepository.findBySeverity(severity);
    }

    public Severity saveSeverity(Severity severity) {
        return severityRepository.save(severity);
    }

    public Severity getOrCreateSeverity(String severity) {
        return severityRepository.findBySeverity(severity)
                .orElseGet(() -> severityRepository.save(new Severity(null, severity)));
    }

    public void deleteSeverity(Long id) {
        severityRepository.deleteById(id);
    }
}