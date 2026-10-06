package service;

import beans.User;
import dto.user.AdminAuthenticationResult;
import dto.user.AdminSigninRequest;
import dto.user.ChangePasswordRequest;
import dto.user.SigninRequest;
import dto.user.UserAuthenticationResult;
import dto.user.UserCreateRequest;
import dto.user.UserDetailResponse;
import exception.BusinessException;

public interface AuthenticationService {

	UserDetailResponse signupUser(UserCreateRequest dto) throws BusinessException;

	void changePassword(long userId, ChangePasswordRequest request) throws BusinessException;

	boolean incrementTokenVersion(long userId) throws BusinessException;

	int getTokenVersion(long userId) throws BusinessException;

	User authenticate(SigninRequest request) throws BusinessException;

	User authenticateAdmin(AdminSigninRequest request) throws BusinessException;

	UserAuthenticationResult authenticateWithResult(SigninRequest request) throws BusinessException;

	AdminAuthenticationResult authenticateAdminWithResult(AdminSigninRequest request) throws BusinessException;
}
