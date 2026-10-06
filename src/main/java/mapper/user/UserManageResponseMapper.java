package mapper.user;

import beans.User;
import dto.user.UserManageResponse;

public class UserManageResponseMapper {

    public UserManageResponse toUserManageResponse(User user) {
        if (user == null) {
            return null;
        }

        UserManageResponse.Builder builder = new UserManageResponse.Builder()
                .id(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt());

        if (user.getProfile() != null) {
            builder.fullname(user.getProfile().getFullname())
                    .email(user.getEmail())
                    .phoneNumber(user.getProfile().getPhoneNumber())
                    .gender(user.getProfile().getGender());
        } else {
            builder.email(user.getEmail());
        }

        if (user.getRole() != null) {
            builder.role(user.getRole().getCode());
            builder.isSystem(user.getRole().isSystem());
        }

        return builder.build();
    }
}