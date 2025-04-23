package com.example.demo.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("EasySQL <your-email@gmail.com>");
            helper.setTo(to);
            helper.setSubject(subject);
            
            // HTML email with plain text alternative
            String htmlContent = "<html><body>" +
                "<p>" + body.replace("\n", "<br>") + "</p>" +
                "<p>—<br>Best regards,<br>Your Application Team</p>" +
                "</body></html>";
                
            helper.setText(body, htmlContent);
            
            mailSender.send(message);
        } catch (MessagingException ex) {
            // Handle exception
        }
    }
}
