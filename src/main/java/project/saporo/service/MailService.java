package project.saporo.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import project.saporo.dto.DailyRate;
import project.saporo.repository.RateFileRepository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static project.saporo.code.TimeConst.KST;
import static project.saporo.code.TimeConst.KST_PRETTY;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final RateFileRepository rateFileRepository;

    @Value("${mail-receiver}")
    private String mailReceiver;

    @Value("${max-value}")
    private Double maxValue;

    @Value("${min-value}")
    private Double minValue;

    /** 공통 컨텍스트 */
    private Context getContext() {
        Context ctx = new Context();
        ctx.setVariable("sentAt", ZonedDateTime.now(KST).format(KST_PRETTY));
        ctx.setVariable("minValue", minValue);
        ctx.setVariable("maxValue", maxValue);
        return ctx;
    }

    /** 오늘/어제 환율 조회해서 컨텍스트에 추가 */
    private void attachTodayAndYesterday(Context ctx) {
        try {
            LocalDate today = LocalDate.now(KST);
            rateFileRepository.find(today).ifPresent(d -> ctx.setVariable("rate", d.rate()));
            rateFileRepository.find(today.minusDays(1)).ifPresent(d -> ctx.setVariable("yesterdayRate", d.rate()));
        } catch (Exception e) {
            log.warn("오늘/어제 환율 불러오기 실패: {}", e.getMessage());
        }
    }

    /** 공통 메일 전송 */
    private void sendEmail(String template, Context ctx) {
        String html = templateEngine.process(template, ctx);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(mailReceiver);
            helper.setSubject("[환율] 엔화(JPY) 환율 알림");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("메일 전송 실패(템플릿: {}): {}", template, e.getMessage(), e);
        }
    }

    /** 일일 메일 */
    public void sendDaily(Double jpyToKrwRate) {
        Context ctx = getContext();
        ctx.setVariable("rate", jpyToKrwRate); // 오늘 환율은 파라미터 우선
        rateFileRepository.find(LocalDate.now(KST).minusDays(1))
                .ifPresent(y -> ctx.setVariable("yesterdayRate", y.rate()));

        sendEmail("mail/jpy-rate-daily", ctx);
    }

    /** 주간 메일 (표 전용) */
    public void sendWeekly(List<DailyRate> week) {
        Context ctx = getContext();
        ctx.setVariable("days", week == null ? List.of() : week);
        attachTodayAndYesterday(ctx);

        sendEmail("mail/jpy-rate-weekly", ctx);
    }
}