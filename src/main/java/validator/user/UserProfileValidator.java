package validator.user;

import constants.RequestParamConstants;
import domain.user.UserValidation;
import dto.user.UserProfileRequest;
import validator.core.BaseValidator;
import validator.core.ValidationResult;

public class UserProfileValidator extends BaseValidator<UserProfileRequest> {

	@Override
	protected void validateFormat(UserProfileRequest dto, ValidationResult result) {
		String fullname = dto.getFullname();
		if (fullname == null || fullname.trim().isEmpty()) {
			result.addError(RequestParamConstants.User.FULLNAME, "Họ và tên không được để trống");
		} else if (fullname.length() > UserValidation.FULLNAME_MAX_LENGTH) {
			result.addError(RequestParamConstants.User.FULLNAME, "Họ và tên tối đa " + UserValidation.FULLNAME_MAX_LENGTH + " ký tự");
		}

		String phone = dto.getPhoneNumber();
		if (phone != null && !phone.trim().isEmpty()) {
			if (!phone.matches("^\\d{" + UserValidation.PHONE_MIN_LENGTH + "," + UserValidation.PHONE_MAX_LENGTH + "}$")) {
				result.addError(RequestParamConstants.User.PHONE_NUMBER, "Số điện thoại không hợp lệ");
			}
		}
	}
}