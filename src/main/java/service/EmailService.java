package service;

import java.io.InputStream;
import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

	private static Properties EMAIL_PROPERTIES = loadEmailProperties();

	public boolean send(String to, String subject, String content) {
		if (to == null || to.isBlank()) {
			return false;
		}

		String host = get("email.smtp.host", "smtp.gmail.com");
		String port = get("email.smtp.port", "587");
		String username = get("email.username", null);
		String password = get("email.password", null);
		String from = get("email.from", username);

		if (username == null || username.isBlank() || password == null || password.isBlank()) {
			return false;
		}

		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");

		Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);
			message.setText(content);
			Transport.send(message);
			return true;
		} catch (MessagingException e) {
			return false;
		}
	}

	private static String get(String key, String defaultValue) {
		if (key == null || key.isBlank()) {
			return defaultValue;
		}
		String value = EMAIL_PROPERTIES.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		String normalized = value.trim();
		return normalized.isEmpty() ? defaultValue : normalized;
	}

	private static Properties loadEmailProperties() {
		Properties props = new Properties();
		try (InputStream in = EmailService.class.getClassLoader().getResourceAsStream("email.properties")) {
			if (in != null) {
				props.load(in);
			}
		} catch (Exception e) {
			return props;
		}
		return props;
	}
}