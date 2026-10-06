package servlet.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.sql.SQLException;

import beans.common.Role;
import constants.RequestParamConstants;
import constants.RouteConstants;
import constants.ViewAttributeConstants;
import constants.ViewPathConstants;
import domain.user.UserDefaults;
import dto.user.LocalUserRegistrationRequest;
import dto.user.UserCreateRequest;
import dto.user.UserDetailResponse;
import exception.BusinessException;
import helpers.ErrorHelper;
import helpers.FormRenderHelper;
import helpers.MessageHelper;
import helpers.SessionHelper;
import helpers.UrlHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.AuthenticationService;
import service.AuthenticationServiceImpl;
import service.EmailVerificationService;
import service.EmailVerificationService.SendVerificationStatus;
import validator.core.ValidationResult;
import validator.user.LocalUserRegistrationValidator;

@WebServlet(name = "SignupServlet", value = "/signup")
public class SignupServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String VERIFY_EMAIL_INIT_FAILED_MESSAGE = "Đăng ký thành công nhưng không thể khởi tạo xác thực email.";
	private static final String VERIFY_EMAIL_SENT_MESSAGE = "Email xác thực đã được gửi. Vui lòng kiểm tra hộp thư.";
	private static final String VERIFY_EMAIL_RATE_LIMIT_MESSAGE = "Bạn gửi yêu cầu quá nhanh. Vui lòng thử lại sau.";
	private static final String VERIFY_EMAIL_TOO_MANY_REQUESTS_MESSAGE = "Bạn đã yêu cầu gửi email xác thực quá nhiều lần. Vui lòng thử lại sau.";
	private static final String VERIFY_EMAIL_SEND_FAILED_MESSAGE = "Không thể gửi email xác thực lúc này. Bạn có thể thử gửi lại.";

	private final AuthenticationService authenticationService = new AuthenticationServiceImpl();
	private final EmailVerificationService emailVerificationService = new EmailVerificationService();
	private final LocalUserRegistrationValidator localUserRegistrationValidator = new LocalUserRegistrationValidator();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		FormRenderHelper.forward(request, response, ViewPathConstants.Client.SIGNUP);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String username = request.getParameter(RequestParamConstants.User.USERNAME);
		String password = request.getParameter(RequestParamConstants.User.PASSWORD);
		String fullname = request.getParameter(RequestParamConstants.User.FULLNAME);
		String email = request.getParameter(RequestParamConstants.User.EMAIL);

		Map<String, String> values = new HashMap<>();
		values.put(RequestParamConstants.User.USERNAME, username);
		values.put(RequestParamConstants.User.FULLNAME, fullname);
		values.put(RequestParamConstants.User.EMAIL, email);

		Map<String, String> errors = new HashMap<>();

		Role defaultRole = new Role();
		defaultRole.setCode(UserDefaults.DEFAULT_ROLE_CODE);

		LocalUserRegistrationRequest localRegistrationRequest = new LocalUserRegistrationRequest.Builder()
				.username(username)
				.password(password)
				.fullname(fullname)
				.email(email)
				.build();
		UserCreateRequest userCreateRequest = new UserCreateRequest.Builder().username(username).password(password).fullname(fullname).email(email).role(defaultRole)
				.build();

		ValidationResult validationResult = localUserRegistrationValidator.validate(localRegistrationRequest);
		if (validationResult.hasErrors()) {
			errors.putAll(validationResult.getErrors());
		}

		if (!errors.isEmpty()) {
			redirectWithErrors(request, response, values, errors);
			return;
		}

		try {
			UserDetailResponse createdUser = authenticationService.signupUser(userCreateRequest);
			Long userId = createdUser != null ? createdUser.getId() : null;
			String createdEmail = createdUser != null ? createdUser.getEmail() : null;

			if (userId == null || createdEmail == null || createdEmail.isBlank()) {
				redirectWithErrors(request, response, values, ErrorHelper.withGlobal(VERIFY_EMAIL_INIT_FAILED_MESSAGE));
				return;
			}

			HttpSession session = request.getSession(true);
			SessionHelper.setPendingVerification(session, userId, createdEmail);

			String baseUrl = UrlHelper.buildBaseUrl(request);
			SendVerificationStatus status = emailVerificationService.sendVerificationEmail(userId, createdEmail, baseUrl);
			if (status == SendVerificationStatus.SENT) {
				MessageHelper.setSuccessMessage(session, VERIFY_EMAIL_SENT_MESSAGE);
			} else if (status == SendVerificationStatus.RATE_LIMITED) {
				MessageHelper.setErrorMessage(session, VERIFY_EMAIL_RATE_LIMIT_MESSAGE);
			} else if (status == SendVerificationStatus.TOO_MANY_REQUESTS) {
				MessageHelper.setErrorMessage(session, VERIFY_EMAIL_TOO_MANY_REQUESTS_MESSAGE);
			} else {
				MessageHelper.setErrorMessage(session, VERIFY_EMAIL_SEND_FAILED_MESSAGE);
			}

			response.sendRedirect(request.getContextPath() + RouteConstants.Client.VERIFY_EMAIL);
		} catch (SQLException e) {
			redirectWithErrors(request, response, values, ErrorHelper.withGlobal(VERIFY_EMAIL_SEND_FAILED_MESSAGE));
		} catch (BusinessException e) {
			redirectWithErrors(request, response, values, ErrorHelper.fromBusiness(e));
		}
	}

	private void redirectWithErrors(HttpServletRequest request, HttpServletResponse response, Map<String, String> values, Map<String, String> errors)
			throws IOException {
		HttpSession session = request.getSession(true);
		session.setAttribute(ViewAttributeConstants.VALUES, values);
		session.setAttribute(ViewAttributeConstants.ERRORS, errors);
		response.sendRedirect(request.getContextPath() + RouteConstants.Client.SIGNUP);
	}
}