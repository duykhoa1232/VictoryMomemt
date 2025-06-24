package com.example.victorymoments.service;

import com.example.victorymoments.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.logging.Logger;

@Service
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendWelcomeEmail(User user) {
        sendEmailInternal(user);
    }

    @Async
    public void sendWelcomeEmailAsync(User user) {
        sendEmailInternal(user);
    }

    private void sendEmailInternal(User user) {
        if (user.getEmail() == null) {
            throw new IllegalArgumentException("User email cannot be null");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to VictoryMoments!");
            helper.setFrom("nguyenxuanbao031202@gmail.com");

            Context context = new Context();
            context.setVariable("name", user.getName() != null ? user.getName() : "User");
            context.setVariable("verificationToken",
                    user.getVerificationToken() != null ? user.getVerificationToken() : "");

            String htmlContent = templateEngine.process("welcome-email", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            LOGGER.info("Welcome email sent successfully to " + user.getEmail());
        } catch (MessagingException e) {
            LOGGER.severe("Failed to send welcome email: " + e.getMessage());
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }
}
