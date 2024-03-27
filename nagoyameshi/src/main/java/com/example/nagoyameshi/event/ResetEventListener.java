package com.example.nagoyameshi.event;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.VerificationTokenRepository;

@Component
public class ResetEventListener {
	private final VerificationTokenRepository verificationTokenRepository;
	private final JavaMailSender javaMailSender;

	public ResetEventListener(VerificationTokenRepository verificationTokenRepository, JavaMailSender mailSender) {
		this.verificationTokenRepository = verificationTokenRepository;
		this.javaMailSender = mailSender;
	}

	@EventListener
	private void onResetEvent(ResetEvent resetEvent) {
		User user = resetEvent.getUser();
		String token = verificationTokenRepository.findByUserId(user.getId()).getToken();

		String recipientAddress = user.getEmail();
		String subject = "メール認証";
		String confirmationUrl = resetEvent.getRequestUrl() + "/verify?token=" + token;
		String message = "以下のリンクをクリックしてパスワードを再発行してください。";

		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(recipientAddress);
		mailMessage.setSubject(subject);
		mailMessage.setText(message + "\n" + confirmationUrl);
		javaMailSender.send(mailMessage);
	}
}