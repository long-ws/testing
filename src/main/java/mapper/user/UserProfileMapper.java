package mapper.user;

import beans.common.Gender;
import beans.common.Language;
import beans.user.UserProfile;
import dto.user.UserCreateRequest;
import dto.user.UserProfileRequest;
import dto.user.UserUpdateRequest;

public class UserProfileMapper {

	public UserProfile toUserProfile(UserProfileRequest request) {
		if (request == null) {
			return null;
		}

		return createUserProfile(
				request.getFullname(),
				request.getPhoneNumber(),
				request.getAvatarUrl(),
				request.getGenderCode(),
				request.getPreferredLanguageCode());
	}

	public UserProfile toUserProfile(UserCreateRequest dto) {
		if (dto == null) {
			return null;
		}

		return createUserProfile(
				dto.getFullname(),
				dto.getPhoneNumber(),
				dto.getAvatarUrl(),
				dto.getGenderCode(),
				dto.getPreferredLanguageCode());
	}

	public UserProfile toUserProfile(UserUpdateRequest dto) {
		if (dto == null) {
			return null;
		}

		return createUserProfile(
				dto.getFullname(),
				dto.getPhoneNumber(),
				dto.getAvatarUrl(),
				dto.getGenderCode(),
				dto.getPreferredLanguageCode());
	}

	private UserProfile createUserProfile(String fullname, String phoneNumber, String avatarUrl,
			String genderCode, String preferredLanguageCode) {
		UserProfile profile = new UserProfile();
		profile.setFullname(fullname);
		profile.setPhoneNumber(phoneNumber);
		profile.setAvatarUrl(avatarUrl);

		if (genderCode != null && !genderCode.isEmpty()) {
			Gender gender = new Gender();
			gender.setCode(genderCode);
			profile.setGender(gender);
		}

		if (preferredLanguageCode != null && !preferredLanguageCode.isEmpty()) {
			Language language = new Language();
			language.setCode(preferredLanguageCode);
			profile.setPreferredLanguage(language);
		}

		return profile;
	}
}