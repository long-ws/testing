package helpers;

import java.util.ArrayList;
import java.util.List;

import beans.User;
import beans.common.Language;
import constants.ViewAttributeConstants;
import domain.user.UserDefaults;
import dto.user.UserProfileRequest;
import jakarta.servlet.http.HttpServletRequest;

public final class UserProfileViewHelper {

	private UserProfileViewHelper() {
	}

	public static void applyDisplayState(HttpServletRequest request, User currentUser, UserProfileRequest formValue, List<Language> languages, String defaultAvatarPath) {
		String selectedLanguageCode = resolveSelectedLanguageCode(currentUser, formValue);
		String selectedGenderCode = resolveSelectedGenderCode(currentUser, formValue);

		request.setAttribute(ViewAttributeConstants.User.DISPLAY_NAME, resolveDisplayName(currentUser));
		request.setAttribute(ViewAttributeConstants.User.AVATAR_SRC, resolveAvatarSrc(request, currentUser, defaultAvatarPath));
		request.setAttribute(ViewAttributeConstants.User.SELECTED_LANGUAGE_CODE, selectedLanguageCode);
		request.setAttribute(ViewAttributeConstants.User.SELECTED_GENDER_CODE, selectedGenderCode);
		request.setAttribute(ViewAttributeConstants.User.ORDERED_LANGUAGES, buildOrderedLanguages(languages, selectedLanguageCode));
	}

	private static String resolveDisplayName(User currentUser) {
		if (currentUser == null) {
			return null;
		}
		if (currentUser.getFullname() != null && !currentUser.getFullname().isBlank()) {
			return currentUser.getFullname();
		}
		return currentUser.getUsername();
	}

	private static String resolveAvatarSrc(HttpServletRequest request, User currentUser, String defaultAvatarPath) {
		if (currentUser != null && currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isBlank()) {
			return currentUser.getAvatarUrl();
		}
		return request.getContextPath() + defaultAvatarPath;
	}

	private static String resolveSelectedLanguageCode(User currentUser, UserProfileRequest formValue) {
		if (formValue != null && formValue.getPreferredLanguageCode() != null && !formValue.getPreferredLanguageCode().isBlank()) {
			return formValue.getPreferredLanguageCode();
		}
		if (currentUser != null && currentUser.getPreferredLanguage() != null && currentUser.getPreferredLanguage().getCode() != null && !currentUser.getPreferredLanguage().getCode().isBlank()) {
			return currentUser.getPreferredLanguage().getCode();
		}
		return UserDefaults.DEFAULT_LANGUAGE_CODE;
	}

	private static String resolveSelectedGenderCode(User currentUser, UserProfileRequest formValue) {
		if (formValue != null && formValue.getGenderCode() != null && !formValue.getGenderCode().isBlank()) {
			return formValue.getGenderCode();
		}
		if (currentUser != null && currentUser.getGender() != null && currentUser.getGender().getCode() != null && !currentUser.getGender().getCode().isBlank()) {
			return currentUser.getGender().getCode();
		}
		return UserDefaults.DEFAULT_GENDER_CODE;
	}

	private static List<Language> buildOrderedLanguages(List<Language> languages, String selectedLanguageCode) {
		List<Language> orderedLanguages = new ArrayList<>();
		if (languages == null) {
			return orderedLanguages;
		}

		for (int i = 0; i < languages.size(); i++) {
			Language language = languages.get(i);
			if (language != null && selectedLanguageCode != null && selectedLanguageCode.equals(language.getCode())) {
				orderedLanguages.add(language);
			}
		}

		for (int i = 0; i < languages.size(); i++) {
			Language language = languages.get(i);
			if (language != null && (selectedLanguageCode == null || !selectedLanguageCode.equals(language.getCode()))) {
				orderedLanguages.add(language);
			}
		}
		return orderedLanguages;
	}
}