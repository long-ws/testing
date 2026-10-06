package mapper.user;

import beans.user.UserOAuthAuth;
import dto.user.OAuthUserRegistrationRequest;

public class UserOAuthAuthMapper {

	public UserOAuthAuth toUserOAuthAuth(OAuthUserRegistrationRequest request) {
		if (request == null) {
			return null;
		}

		UserOAuthAuth oauthAuth = new UserOAuthAuth();
		oauthAuth.setProviderId(request.getProviderId());
		oauthAuth.setProviderUserId(request.getProviderUserId());
		oauthAuth.setProviderEmail(request.getEmail());
		oauthAuth.setDisplayName(request.getFullname());
		oauthAuth.setAvatarUrl(request.getAvatarUrl());
		return oauthAuth;
	}
}