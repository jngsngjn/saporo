package project.saporo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import project.saporo.dto.DailyRate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Component
public class RateFileRepository {

    private final Path baseDir;
    private final ObjectMapper objectMapper;

    public RateFileRepository(@Value("${rate-file-path}") String dir, ObjectMapper objectMapper) throws IOException {
        this.baseDir = Paths.get(dir);
        Files.createDirectories(this.baseDir);

        this.objectMapper = objectMapper;
    }

    public void save(DailyRate dailyRate) throws IOException {
        Path target = baseDir.resolve(dailyRate.date().toString() + ".json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), dailyRate);
        log.info("Daily rate saved to {}", target);
    }

    public Optional<DailyRate> find(LocalDate date) throws IOException {
        Path target = baseDir.resolve(date.toString() + ".json");
        if (!Files.exists(target)) return Optional.empty();
        DailyRate dr = objectMapper.readValue(target.toFile(), DailyRate.class);
        return Optional.of(dr);
    }
}