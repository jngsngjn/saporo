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

            /*
            여기에 추가
            - 만약 오늘이 월요일이라면 한 주간 데이터 집계 정보도 함께 전송 (이메일 템플릿이 다름)
            - 월요일이 아니면 mailService.send(jpyToKrwRate);
            - 월요일이면 다른 메서드 사용
            - 그리고 나서 지난 일주일 정보는 삭제 (월요일까지 일주일치 정보 모으고, 월요일이 되면 오늘, 전일 정보만 남기고 삭제)
             */

            // 3. 메일 발송
            mailService.send(jpyToKrwRate);
            log.info("Daily JPY rate email triggered by scheduler.");
        } catch (Exception e) {
            log.error("Failed to send daily JPY rate email", e);
        }
    }
}