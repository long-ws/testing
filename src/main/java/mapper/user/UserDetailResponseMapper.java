package mapper.user;

import beans.User;
import beans.common.Gender;
import beans.common.Language;
import dto.user.UserCreateRequest;
import dto.user.UserDetailResponse;
import dto.user.UserUpdateRequest;

public class UserDetailResponseMapper {

	public UserDetailResponse toUserDetailResponse(User user) {
		if (user == null) {
			return null;
		}

		UserDetailResponse.Builder builder = new UserDetailResponse.Builder()
				.id(user.getId())
				.username(user.getUsername())
				.tokenVersion(user.getTokenVersion())
				.lastLoginAt(user.getLastLoginAt())
				.createdAt(user.getCreatedAt())
				.updatedAt(user.getUpdatedAt())
				.isDeleted(user.isDeleted())
				.isLocked(user.isLocked())
				.status(user.getStatus())
				.role(user.getRole());

		if (user.getProfile() != null) {
			builder.fullname(user.getProfile().getFullname())
					.email(user.getEmail())
					.phoneNumber(user.getProfile().getPhoneNumber())
					.gender(user.getProfile().getGender())
					.preferredLanguage(user.getProfile().getPreferredLanguage())
					.avatarUrl(user.getProfile().getAvatarUrl());
		} else {
			builder.email(user.getEmail());
		}

		return builder.build();
	}

	public UserCreateRequest toUserCreateRequest(UserDetailResponse detail) {
		if (detail == null) {
			return null;
		}

		String genderCode = getGenderCode(detail.getGender());
		String preferredLanguageCode = getLanguageCode(detail.getPreferredLanguage());

		return new UserCreateRequest.Builder()
				.id(detail.getId())
				.username(detail.getUsername())
				.fullname(detail.getFullname())
				.email(detail.getEmail())
				.phoneNumber(detail.getPhoneNumber())
				.genderCode(genderCode)
				.preferredLanguageCode(preferredLanguageCode)
				.avatarUrl(detail.getAvatarUrl())
				.role(detail.getRole())
				.build();
	}

	public UserUpdateRequest toUserUpdateRequest(UserDetailResponse detail) {
		if (detail == null) {
			return null;
		}

		String genderCode = getGenderCode(detail.getGender());
		String preferredLanguageCode = getLanguageCode(detail.getPreferredLanguage());

		return new UserUpdateRequest.Builder()
				.id(detail.getId())
				.username(detail.getUsername())
				.fullname(detail.getFullname())
				.email(detail.getEmail())
				.phoneNumber(detail.getPhoneNumber())
				.genderCode(genderCode)
				.preferredLanguageCode(preferredLanguageCode)
				.avatarUrl(detail.getAvatarUrl())
				.role(detail.getRole())
				.build();
	}

	private String getGenderCode(Gender gender) {
		return gender != null ? gender.getCode() : null;
	}

	private String getLanguageCode(Language language) {
		return language != null ? language.getCode() : null;
	}
}