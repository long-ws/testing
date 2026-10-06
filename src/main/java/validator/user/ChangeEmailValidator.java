package validator.user;

import constants.RequestParamConstants;
import domain.user.UserValidation;
import dto.user.ChangeEmailRequest;
import validator.core.BaseValidator;
import validator.core.ValidationResult;

public class ChangeEmailValidator extends BaseValidator<ChangeEmailRequest> {

	public ChangeEmailValidator() {
	}

	@Override
	protected void validateFormat(ChangeEmailRequest dto, ValidationResult result) {
		String currentEmail = dto.getCurrentEmail();
		if (currentEmail == null || currentEmail.trim().isEmpty()) {
			result.addError(RequestParamConstants.User.CURRENT_EMAIL, "Email hiện tại không được để trống");
		} else if (!currentEmail.equals(currentEmail.trim())) {
			result.addError(RequestParamConstants.User.CURRENT_EMAIL, "Email hiện tại không có dấu cách ở hai đầu");
		} else if (!currentEmail.matches("^[^@]+@[^@]+\\.[^@]+$")) {
			result.addError(RequestParamConstants.User.CURRENT_EMAIL, "Email hiện tại không hợp lệ");
		}

		String currentPassword = dto.getCurrentPassword();
		if (currentPassword == null || currentPassword.trim().isEmpty()) {
			result.addError(RequestParamConstants.User.CURRENT_PASSWORD, "Mật khẩu hiện tại không được để trống");
		}

		String newEmail = dto.getNewEmail();
		if (newEmail == null || newEmail.trim().isEmpty()) {
			result.addError(RequestParamConstants.User.NEW_EMAIL, "Email mới không được để trống");
		} else if (!newEmail.equals(newEmail.trim())) {
			result.addError(RequestParamConstants.User.NEW_EMAIL, "Email mới không có dấu cách ở hai đầu");
		} else if (newEmail.length() > UserValidation.EMAIL_MAX_LENGTH) {
			result.addError(RequestParamConstants.User.NEW_EMAIL, "Email mới tối đa " + UserValidation.EMAIL_MAX_LENGTH + " ký tự");
		} else if (!newEmail.matches("^[^@]+@[^@]+\\.[^@]+$")) {
			result.addError(RequestParamConstants.User.NEW_EMAIL, "Email mới không hợp lệ");
		}
	}
}
