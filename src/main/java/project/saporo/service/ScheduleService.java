package project.saporo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import project.saporo.dto.DailyRate;
import project.saporo.repository.RateFileRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;
import static project.saporo.code.TimeConst.KST;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final MailService mailService;
    private final RateFileRepository rateFileRepository;
    private final ExchangeRateService exchangeRateService;

    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    public void sendDailyJpyRateEmail() {
        LocalDate today = LocalDate.now(KST);

        try {
            // 1) 환율 조회
            Double jpyToKrwRate = exchangeRateService.getJpyToKrwRate();

            // 2) 오늘 파일 저장
            DailyRate todayRate = new DailyRate(today, jpyToKrwRate);
            rateFileRepository.save(todayRate);

            // 매주 월요일
            if (today.getDayOfWeek() == DayOfWeek.MONDAY) {

                // 최근 7일 (오늘 포함, 과거 6일)
                LocalDate start = today.minusDays(6);

                List<DailyRate> week = rateFileRepository.findRange(start, today);

                if (isEmpty(week)) {
                    log.info("No weekly data for {} ~ {} - sending daily mail instead", start, today);
                    mailService.sendDaily(jpyToKrwRate);
                } else {
                    try {
                        mailService.sendWeekly(week);
                        log.info("Weekly mail sent for {} ~ {}", start, today);
                    } catch (Exception e) {
                        log.error("Failed to send weekly mail, falling back to daily mail: {}", e.getMessage(), e);
                        mailService.sendDaily(jpyToKrwRate);
                    }
                }

                // 삭제: today와 yesterday(오늘·전일)만 남기고 이전 파일 삭제
                LocalDate keepFrom = today.minusDays(1);
                try {
                    List<LocalDate> allDates = rateFileRepository.listAllDates();
                    for (LocalDate date : allDates) {
                        if (date.isBefore(keepFrom)) {
                            boolean deleted = rateFileRepository.delete(date);
                            if (deleted) {
                                log.info("Deleted old rate file for {}", date);
                            } else {
                                log.warn("Could not delete file for {} (maybe not exist)", date);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Cleanup (delete old files) failed: {}", e.getMessage(), e);
                }

            } else {
                // 월요일이 아니면
                mailService.sendDaily(jpyToKrwRate);
                log.info("Daily mail sent for {}", today);
            }

        } catch (Exception e) {
            log.error("Failed to run scheduled job: {}", e.getMessage(), e);
        }
    }
}