package service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import constants.RequestParamConstants;
import context.PendingEmailChangeContext;
import dto.user.ChangeEmailVerificationRequest;
import dto.user.VerificationChallenge;

public class EmailChangeVerificationService {

	public static final int CODE_LENGTH = 6;
	public static final long EXPIRES_IN_MILLIS = 10L * 60L * 1000L;
	public static final int EXPIRES_IN_MINUTES = (int) (EXPIRES_IN_MILLIS / (60 * 1000));

	private static final SecureRandom RANDOM = new SecureRandom();

	private final EmailService emailService;

	public EmailChangeVerificationService() {
		this(new EmailService());
	}

	public EmailChangeVerificationService(EmailService emailService) {
		this.emailService = emailService;
	}

	public VerificationChallenge createVerificationChallenge(String currentEmail, String newEmail) {
		String currentEmailCode = generateNumericCode();
		String newEmailCode = generateNumericCode();
		PendingEmailChangeContext pendingEmailChange = new PendingEmailChangeContext(currentEmail, newEmail, hash(currentEmailCode), hash(newEmailCode),
				System.currentTimeMillis() + EXPIRES_IN_MILLIS);
		return new VerificationChallenge(pendingEmailChange, currentEmailCode, newEmailCode);
	}

	public boolean sendVerificationCodes(VerificationChallenge verificationChallenge) {
		if (verificationChallenge == null || verificationChallenge.getPendingEmailChange() == null) {
			return false;
		}

		PendingEmailChangeContext pendingEmailChange = verificationChallenge.getPendingEmailChange();
		boolean currentSent = emailService.send(
				pendingEmailChange.getCurrentEmail(),
				"Mã xác thực email hiện tại",
				buildCurrentEmailContent(verificationChallenge.getCurrentEmailCode()));
		boolean newSent = emailService.send(
				pendingEmailChange.getNewEmail(),
				"Mã xác thực email mới",
				buildNewEmailContent(verificationChallenge.getNewEmailCode()));
		return currentSent && newSent;
	}

	public Map<String, String> validateVerificationCodes(ChangeEmailVerificationRequest request, PendingEmailChangeContext pendingEmailChange) {
		Map<String, String> errors = new HashMap<>();
		if (request == null || pendingEmailChange == null) {
			return errors;
		}

		if (!matches(request.getCurrentEmailCode(), pendingEmailChange.getCurrentEmailCodeHash())) {
			errors.put(RequestParamConstants.User.CURRENT_EMAIL_CODE, "Mã xác thực email hiện tại không chính xác");
		}
		if (!matches(request.getNewEmailCode(), pendingEmailChange.getNewEmailCodeHash())) {
			errors.put(RequestParamConstants.User.NEW_EMAIL_CODE, "Mã xác thực email mới không chính xác");
		}
		return errors;
	}

	private String generateNumericCode() {
		int max = (int) Math.pow(10, CODE_LENGTH);
		int value = RANDOM.nextInt(max);
		return String.format("%0" + CODE_LENGTH + "d", Integer.valueOf(value));
	}

	private String buildCurrentEmailContent(String code) {
		return "Bạn đang yêu cầu thay đổi email hiện tại.\n\n"
				+ "Nhập mã xác thực email hiện tại để tiếp tục:\n"
				+ code
				+ "\n\nMã xác thực sẽ hết hạn trong "
				+ EXPIRES_IN_MINUTES
				+ " phút.";
	}

	private String buildNewEmailContent(String code) {
		return "Bạn đang xác nhận email mới cho tài khoản.\n\n"
				+ "Nhập mã xác thực email mới để xác nhận:\n"
				+ code
				+ "\n\nMã xác thực sẽ hết hạn trong "
				+ EXPIRES_IN_MINUTES
				+ " phút.";
	}

	private String hash(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (Exception e) {
			throw new RuntimeException("Xảy ra lỗi khi tạo mã xác thực", e);
		}
	}

	private boolean matches(String rawCode, String expectedHash) {
		if (rawCode == null || rawCode.isBlank() || expectedHash == null || expectedHash.isBlank()) {
			return false;
		}
		return hash(rawCode.trim()).equals(expectedHash);
	}
}
