package service.user;

import beans.User;
import beans.common.Gender;
import beans.user.UserProfile;
import dao.common.GenderDAO;
import domain.user.UserDefaults;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class UserGenderService {

	private final GenderDAO genderDAO;

	public UserGenderService(GenderDAO genderDAO) {
		this.genderDAO = genderDAO;
	}

	public void applyGender(Connection conn, User user, User existing) throws SQLException {
		UserProfile profile = user.getProfile();
		if (profile == null) {
			return;
		}

		Gender gender = profile.getGender();
		if (gender != null && gender.getCode() != null && !gender.getCode().isEmpty()) {
			Optional<Gender> genderOptional = genderDAO.findByCode(conn, gender.getCode());
			if (genderOptional.isPresent()) {
				profile.setGender(genderOptional.get());
			}
		} else if (existing == null || existing.getProfile() == null || existing.getProfile().getGender() == null) {
			Optional<Gender> defaultGender = genderDAO.findByCode(conn, UserDefaults.DEFAULT_GENDER_CODE);
			if (defaultGender.isPresent()) {
				profile.setGender(defaultGender.get());
			}
		}
	}
}
