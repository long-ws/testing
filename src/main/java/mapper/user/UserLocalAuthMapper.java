package mapper.user;

import beans.user.UserLocalAuth;
import dto.user.LocalUserRegistrationRequest;

public class UserLocalAuthMapper {

	public UserLocalAuth toUserLocalAuth(LocalUserRegistrationRequest request, String passwordHash) {
		if (request == null) {
			return null;
		}

		UserLocalAuth localAuth = new UserLocalAuth();
		localAuth.setUsername(request.getUsername());
		localAuth.setPasswordHash(passwordHash);

		return localAuth;
	}
}