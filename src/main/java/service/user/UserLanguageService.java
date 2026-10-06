package service.user;

import beans.User;
import beans.common.Language;
import beans.user.UserProfile;
import dao.common.LanguageDAO;
import domain.user.UserDefaults;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class UserLanguageService {

	private final LanguageDAO languageDAO;

	public UserLanguageService(LanguageDAO languageDAO) {
		this.languageDAO = languageDAO;
	}

	public void applyLanguage(Connection conn, User user, User existing) throws SQLException {
		UserProfile profile = user.getProfile();
		if (profile == null) {
			return;
		}

		Language preferredLanguage = profile.getPreferredLanguage();
		if (preferredLanguage != null && preferredLanguage.getCode() != null && !preferredLanguage.getCode().isEmpty()) {
			Optional<Language> selectedLanguageOptional = languageDAO.findByCode(conn, preferredLanguage.getCode());
			if (selectedLanguageOptional.isPresent()) {
				Language selectedLanguage = selectedLanguageOptional.get();
				profile.setPreferredLanguage(selectedLanguage);
			}
		} else if (existing != null && existing.getProfile() != null && existing.getProfile().getPreferredLanguage() != null) {
			UserProfile existingProfile = existing.getProfile();
			Language existingPreferredLanguage = existingProfile.getPreferredLanguage();
			profile.setPreferredLanguage(existingPreferredLanguage);
		} else {
			Optional<Language> defaultLanguageOptional = languageDAO.findByCode(conn, UserDefaults.DEFAULT_LANGUAGE_CODE);
			if (defaultLanguageOptional.isPresent()) {
				Language defaultLanguage = defaultLanguageOptional.get();
				profile.setPreferredLanguage(defaultLanguage);
			}
		}
	}
}
