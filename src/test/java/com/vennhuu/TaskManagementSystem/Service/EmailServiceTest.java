package com.vennhuu.TaskManagementSystem.Service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.vennhuu.TaskManagementSystem.Utils.errors.BadRequestException;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromAddress", "no-reply@test.com");
    }

    @Test
    @DisplayName("Should send assign task email successfully")
    void shouldSendAssignTaskEmailSuccessfully() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");

        emailService.sendAssignTaskEmail("user@test.com", "Test User", "Test Project", "Task Title");

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should throw BadRequestException when email sending fails")
    void shouldThrowBadRequestExceptionWhenSendingFails() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test</html>");
        doThrow(new RuntimeException("SMTP Connection failed")).when(javaMailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> emailService.sendAssignTaskEmail("user@test.com", "Test User", "Test Project", "Task Title"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Không gửi được email");
    }
}
