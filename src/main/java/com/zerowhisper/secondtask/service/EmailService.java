package com.zerowhisper.secondtask.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender emailSender;

    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendVerificationEmail(
            String toEmail,
            String subject,
            String text
    ) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("zerowhisperconfirmationbot@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            System.out.println("Error sending email: " + e.getMessage());
        }
    }

}
