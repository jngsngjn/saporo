package project.saporo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final MailService mailService;
    private final ExchangeRateService exchangeRateService;

    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    public void sendDailyJpyRateEmail() {
        try {
            Double jpyToKrwRate = exchangeRateService.getJpyToKrwRate();
            mailService.send(jpyToKrwRate);
            log.info("Daily JPY rate email triggered by scheduler.");
        } catch (Exception e) {
            log.error("Failed to send daily JPY rate email", e);
        }
    }
}