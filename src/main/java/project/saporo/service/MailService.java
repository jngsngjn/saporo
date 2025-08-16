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

import java.time.ZonedDateTime;

import static project.saporo.code.TimeConst.KST;
import static project.saporo.code.TimeConst.KST_PRETTY;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${mail-receiver}")
    private String mailReceiver;

    @Value("${max-value}")
    private Double maxValue;

    @Value("${min-value}")
    private Double minValue;

    public void send(Double jpyToKrwRate) {
        String rateFormatted = String.format("1 JPY = %s KRW", jpyToKrwRate);
        String sentAt = ZonedDateTime.now(KST).format(KST_PRETTY);

        Context ctx = new Context();
        ctx.setVariable("rateFormatted", rateFormatted);
        ctx.setVariable("sentAt", sentAt);

        ctx.setVariable("rate", jpyToKrwRate);
        ctx.setVariable("minValue", minValue);
        ctx.setVariable("maxValue", maxValue);

        String html = templateEngine.process("mail/jpy-rate", ctx);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(mailReceiver);
            helper.setSubject("[환율] 엔화(JPY) 환율 알림");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("JPY rate email sent complete");
        } catch (Exception e) {
            log.error("Failed to send JPY rate email", e);
        }
    }
}