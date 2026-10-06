package mapper.user;

import beans.User;
import dto.user.UserProfileResponse;

public class UserProfileResponseMapper {

    public UserProfileResponse toUserProfileResponse(User user) {
        if (user == null) {
            return null;
        }

        UserProfileResponse.Builder builder = new UserProfileResponse.Builder().userId(user.getId());

        if (user.getProfile() != null) {
            builder.fullname(user.getProfile()
                    .getFullname()).phoneNumber(user.getProfile()
                    .getPhoneNumber())
                    .email(user.getEmail())
                    .avatarUrl(user.getProfile().getAvatarUrl());

            if (user.getProfile().getGender() != null) {
                builder.genderCode(user.getProfile().getGender().getCode());
            }

            if (user.getProfile().getPreferredLanguage() != null) {
                builder.languageCode(user.getProfile().getPreferredLanguage().getCode());
            }
        } else {
            builder.email(user.getEmail());
        }

        return builder.build();
    }
}