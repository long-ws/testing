package mapper.user;

import beans.user.UserEmail;

public class UserEmailMapper {

	public UserEmail toUserEmail(String email) {
		UserEmail userEmail = new UserEmail();
		userEmail.setEmail(email);
		return userEmail;
	}
}