package project.saporo.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import project.saporo.dto.DailyRate;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import static project.saporo.code.FileConst.JSON_FILE_SUFFIX;

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

    public void save(DailyRate dailyRate) {
        Path target = baseDir.resolve(dailyRate.date().toString() + JSON_FILE_SUFFIX);
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), dailyRate);
            log.info("Daily rate saved to {}", target);
        } catch (IOException e) {
            log.error("Failed to save daily rate to {}: {}", target, e.getMessage());
        }
    }

    public Optional<DailyRate> find(LocalDate date) {
        Path target = baseDir.resolve(date.toString() + JSON_FILE_SUFFIX);
        if (!Files.exists(target)) return Optional.empty();
        try {
            DailyRate dr = objectMapper.readValue(target.toFile(), DailyRate.class);
            return Optional.ofNullable(dr);
        } catch (IOException e) {
            log.error("Failed to read daily rate from {}: {}", target, e.getMessage());
            return Optional.empty();
        }
    }

    public List<DailyRate> findRange(LocalDate startInclusive, LocalDate endInclusive) {
        if (startInclusive == null || endInclusive == null) {
            return Collections.emptyList();
        }

        if (endInclusive.isBefore(startInclusive)) {
            return Collections.emptyList();
        }

        List<DailyRate> out = new ArrayList<>();
        for (LocalDate date = startInclusive; !date.isAfter(endInclusive); date = date.plusDays(1)) {
            Path path = baseDir.resolve(date + JSON_FILE_SUFFIX);
            if (!Files.exists(path)) continue;
            try {
                DailyRate dailyRate = objectMapper.readValue(path.toFile(), DailyRate.class);
                if (dailyRate != null) out.add(dailyRate);
            } catch (IOException e) {
                log.warn("Skipping date {} due to read error: {}", date, e.getMessage());
            }
        }
        out.sort(Comparator.comparing(DailyRate::date));
        return out;
    }

    public List<LocalDate> listAllDates() {
        List<LocalDate> list = new ArrayList<>();
        if (!Files.exists(baseDir)) {
            return list;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir, "*" + JSON_FILE_SUFFIX)) {
            for (Path path : stream) {
                String name = path.getFileName().toString();
                if (!name.endsWith(JSON_FILE_SUFFIX)) continue;
                String datePart = name.substring(0, name.length() - 5);
                try {
                    LocalDate date = LocalDate.parse(datePart);
                    list.add(date);
                } catch (Exception ignore) {
                    // ignore invalid filenames
                }
            }
        } catch (IOException e) {
            log.error("Failed to list files in {}: {}", baseDir, e.getMessage());
            return Collections.emptyList();
        }

        list.sort(Comparator.naturalOrder());
        return list;
    }

    public boolean delete(LocalDate date) {
        if (date == null) {
            return false;
        }
        Path target = baseDir.resolve(date + JSON_FILE_SUFFIX);
        try {
            return Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Failed to delete file {}: {}", target, e.getMessage());
            return false;
        }
    }
}