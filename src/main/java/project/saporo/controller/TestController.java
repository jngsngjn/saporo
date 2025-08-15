package project.saporo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import project.saporo.service.MailService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {

    private final MailService mailService;

    @PostMapping("/api/send-email")
    public void send() {
        long startTimeMillis = System.currentTimeMillis();
        mailService.send();
        long endTimeMillis = System.currentTimeMillis();
        log.info("Email sent in {} ms", endTimeMillis - startTimeMillis);
    }
}