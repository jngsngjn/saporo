package project.saporo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import project.saporo.dto.DailyRate;
import project.saporo.repository.RateFileRepository;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final MailService mailService;
    private final RateFileRepository rateFileRepository;
    private final ExchangeRateService exchangeRateService;

    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    public void sendDailyJpyRateEmail() {
        try {
            // 1. 환율 정보 얻기
            Double jpyToKrwRate = exchangeRateService.getJpyToKrwRate();

            // 2. 파일 저장
            DailyRate dailyRate = new DailyRate(LocalDate.now(), jpyToKrwRate);
            rateFileRepository.save(dailyRate);

            // 3. 메일 발송
            mailService.send(jpyToKrwRate);
            log.info("Daily JPY rate email triggered by scheduler.");
        } catch (Exception e) {
            log.error("Failed to send daily JPY rate email", e);
        }
    }
}