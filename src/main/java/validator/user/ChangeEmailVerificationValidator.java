package validator.user;

import constants.RequestParamConstants;
import dto.user.ChangeEmailVerificationRequest;
import validator.core.BaseValidator;
import validator.core.ValidationResult;

public class ChangeEmailVerificationValidator extends BaseValidator<ChangeEmailVerificationRequest> {

	private static final String CODE_REGEX = "\\d{6}";

	@Override
	protected void validateFormat(ChangeEmailVerificationRequest dto, ValidationResult result) {
		validateCode(dto.getCurrentEmailCode(), RequestParamConstants.User.CURRENT_EMAIL_CODE, "Mã xác thực email hiện tại", result);
		validateCode(dto.getNewEmailCode(), RequestParamConstants.User.NEW_EMAIL_CODE, "Mã xác thực email mới", result);
	}

	private void validateCode(String code, String field, String fieldLabel, ValidationResult result) {
		if (code == null || code.trim().isEmpty()) {
			result.addError(field, fieldLabel + " không được để trống");
		} else if (!code.matches(CODE_REGEX)) {
			result.addError(field, fieldLabel + " phải gồm 6 chữ số");
		}
	}
}
