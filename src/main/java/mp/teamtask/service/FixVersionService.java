package mp.teamtask.service;

import lombok.RequiredArgsConstructor;
import mp.teamtask.domain.FixVersion;
import mp.teamtask.repository.FixVersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FixVersionService {

    private final FixVersionRepository fixVersionRepository;

    public List<FixVersion> getAllFixVersions() {
        return fixVersionRepository.findAll();
    }

    public FixVersion getFixVersionById(Long id) {
        return fixVersionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FixVersion not found with ID: " + id));
    }

    public Optional<FixVersion> findByVersion(String version) {
        return fixVersionRepository.findByVersion(version);
    }

    public FixVersion saveFixVersion(FixVersion fixVersion) {
        return fixVersionRepository.save(fixVersion);
    }

    public FixVersion getOrCreateFixVersion(String version) {
        return fixVersionRepository.findByVersion(version)
                .orElseGet(() -> fixVersionRepository.save(new FixVersion(null, version)));
    }

    public boolean isFixVersionInUse(Long id) {
        return fixVersionRepository.countTasksByFixVersionId(id) > 0;
    }

    public void deleteFixVersion(Long id) {
        if (isFixVersionInUse(id)) {
            throw new IllegalStateException("Fix version is currently assigned to tasks and cannot be deleted.");
        }
        fixVersionRepository.deleteById(id);
    }
}