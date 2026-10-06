<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@ include file="_paramKeys.jsp"%>
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">

<head>
<jsp:include page="_meta.jsp" />
<title>Bảo mật và thông báo</title>
</head>

<body>
	<jsp:include page="_header.jsp" />

	<section class="section-content padding-y">
		<div class="container">
			<div class="row">
				<c:choose>
					<c:when test="${empty sessionScope.currentUser}">
						<p>
							Vui lòng <a href="${pageContext.request.contextPath}${RT_SIGNIN}">đăng
								nhập</a> để sử dụng chức năng này.
						</p>
					</c:when>
					<c:otherwise>
						<jsp:include page="_navPanel.jsp">
							<jsp:param name="active" value="SECURITY" />
						</jsp:include>

						<main class="col-md-9">
							<!-- Alert Messages -->
							<c:if test="${not empty requestScope[ATTR_SUCCESS_MESSAGE]}">
								<div
									class="alert alert-success alert-dismissible fade show shadow-sm mb-3"
									role="alert">
									<i class="bi bi-check-circle-fill me-2"></i>${requestScope[ATTR_SUCCESS_MESSAGE]}
									<button type="button" class="btn-close" data-bs-dismiss="alert"
										aria-label="Close"></button>
								</div>
							</c:if>

							<!-- Global Error -->
							<c:if test="${not empty requestScope[ATTR_ERRORS][ERR_GLOBAL]}">
								<div
									class="alert alert-danger alert-dismissible fade show shadow-sm mb-3"
									role="alert">
									<i class="bi bi-exclamation-triangle-fill me-2"></i>${requestScope[ATTR_ERRORS][ERR_GLOBAL]}
									<button type="button" class="btn-close" data-bs-dismiss="alert"
										aria-label="Close"></button>
								</div>
							</c:if>

							<c:choose>
								<c:when
									test="${requestScope[ATTR_SECURITY_PAGE] eq SECURITY_PAGE_CHANGE_PASSWORD}">
									<article class="card mb-4">
										<div
											class="card-header bg-white d-flex align-items-center justify-content-between">
											<h5 class="card-title mb-0">Đổi mật khẩu</h5>
											<a class="btn btn-outline-secondary btn-sm"
												href="${pageContext.request.contextPath}${RT_SECURITY}">
												<i class="bi bi-arrow-left me-2"></i>Quay lại
											</a>
										</div>
										<div class="card-body">
											<form
												action="${pageContext.request.contextPath}${RT_SECURITY_CHANGE_PASSWORD}"
												method="post">
												<div class="mb-3">
													<label for="currentPassword" class="form-label">Mật
														khẩu hiện tại</label> <input type="password"
														class="form-control ${not empty requestScope[ATTR_ERRORS][P_CURRENT_PASSWORD] ? 'is-invalid' : ''}"
														id="currentPassword" name="${P_CURRENT_PASSWORD}" value=""
														required>
													<c:if
														test="${not empty requestScope[ATTR_ERRORS][P_CURRENT_PASSWORD]}">
														<div class="invalid-feedback">${requestScope[ATTR_ERRORS][P_CURRENT_PASSWORD]}</div>
													</c:if>
												</div>
												<div class="mb-3">
													<label for="newPassword" class="form-label">Mật
														khẩu mới</label> <input type="password"
														class="form-control ${not empty requestScope[ATTR_ERRORS][P_NEW_PASSWORD] ? 'is-invalid' : ''}"
														id="newPassword" name="${P_NEW_PASSWORD}" value=""
														required>
													<c:if
														test="${not empty requestScope[ATTR_ERRORS][P_NEW_PASSWORD]}">
														<div class="invalid-feedback">${requestScope[ATTR_ERRORS][P_NEW_PASSWORD]}</div>
													</c:if>
												</div>
												<div class="mb-3">
													<label for="newPasswordAgain" class="form-label">Nhập
														lại mật khẩu mới</label> <input type="password"
														class="form-control ${not empty requestScope[ATTR_ERRORS][P_CONFIRM_PASSWORD] ? 'is-invalid' : ''}"
														id="newPasswordAgain" name="${P_CONFIRM_PASSWORD}"
														value="" required>
													<c:if
														test="${not empty requestScope[ATTR_ERRORS][P_CONFIRM_PASSWORD]}">
														<div class="invalid-feedback">${requestScope[ATTR_ERRORS][P_CONFIRM_PASSWORD]}</div>
													</c:if>
												</div>
												<button type="submit" class="btn btn-primary">
													<i class="bi bi-key me-2"></i>Đổi mật khẩu
												</button>
											</form>
										</div>
									</article>
								</c:when>
								<c:when
									test="${requestScope[ATTR_SECURITY_PAGE] eq SECURITY_PAGE_CHANGE_EMAIL}">
									<article class="card mb-4">
										<div
											class="card-header bg-white d-flex align-items-center justify-content-between">
											<h5 class="card-title mb-0">
												<c:choose>
													<c:when
														test="${requestScope[ATTR_SECURITY_CHANGE_EMAIL_STEP] eq SECURITY_CHANGE_EMAIL_STEP_VERIFY}">
														Xác thực đổi email
													</c:when>
													<c:otherwise>Đổi email</c:otherwise>
												</c:choose>
											</h5>
											<a class="btn btn-outline-secondary btn-sm"
												href="${pageContext.request.contextPath}${requestScope[ATTR_SECURITY_BACK_ROUTE]}">
												<i class="bi bi-arrow-left me-2"></i>Quay lại
											</a>
										</div>
										<div class="card-body">
											<c:choose>
												<c:when
													test="${requestScope[ATTR_SECURITY_CHANGE_EMAIL_STEP] eq SECURITY_CHANGE_EMAIL_STEP_VERIFY}">
													<p class="text-muted mb-4">Hệ thống đã gửi mã xác thực
														tới email hiện tại và email mới</p>
													<form
														action="${pageContext.request.contextPath}${RT_SECURITY_CHANGE_EMAIL}"
														method="post">
														<input type="hidden" name="${P_STEP}" value="verify">
														<div class="mb-3">
															<label class="form-label">Email hiện tại</label> <input
																type="email" class="form-control"
																value="${requestScope[ATTR_VALUES][P_CURRENT_EMAIL]}"
																readonly>
														</div>
														<div class="mb-3">
															<label class="form-label">Email mới</label> <input
																type="email" class="form-control"
																value="${requestScope[ATTR_VALUES][P_NEW_EMAIL]}"
																readonly>
														</div>
														<div class="mb-3">
															<label for="currentEmailCode" class="form-label">Mã
																xác thực email hiện tại</label> <input type="text"
																class="form-control ${not empty requestScope[ATTR_ERRORS][P_CURRENT_EMAIL_CODE] ? 'is-invalid' : ''}"
																id="currentEmailCode" name="${P_CURRENT_EMAIL_CODE}"
																value="" inputmode="numeric" maxlength="6" required>
															<c:if
																test="${not empty requestScope[ATTR_ERRORS][P_CURRENT_EMAIL_CODE]}">
																<div class="invalid-feedback">${requestScope[ATTR_ERRORS][P_CURRENT_EMAIL_CODE]}</div>
															</c:if>
														</div>
														<div class="mb-3">
															<label for="newEmailCode" class="form-label">Mã
																xác thực email mới</label> <input type="text"
																class="form-control ${not empty requestScope[ATTR_ERRORS][P_NEW_EMAIL_CODE] ? 'is-invalid' : ''}"
																id="newEmailCode" name="${P_NEW_EMAIL_CODE}" value=""
																inputmode="numeric" maxlength="6" required>
															<c:if
																test="${not empty requestScope[ATTR_ERRORS][P_NEW_EMAIL_CODE]}">
																<div class="invalid-feedback">${requestScope[ATTR_ERRORS][P_NEW_EMAIL_CODE]}</div>
															</c:if>
														</div>
														<button type="submit" class="btn btn-primary">
															<i class="bi bi-shield-check me-2"></i>Xác nhận cập nhật
															email
														</button>
													</form>
												</c:when>
												<c:otherwise>
													<form
														action="${pageContext.request.contextPath}${RT_SECURITY_CHANGE_EMAIL}"
														method="post">
														<div class="mb-3">
															<label for="currentEmail" class="form-label">Email
																hiện tại</label> <input type="email"
																class="form-control ${not empty requestScope[ATTR_ERRORS][P_CURRENT_EMAIL] ? 'is-invalid' : ''}"
																id="currentEmail" name="${P_CURRENT_EMAIL}"
																value="${not empty requestScope[ATTR_VALUES][P_CURRENT_EMAIL] ? requestScope[ATTR_VALUES][P_CURRENT_EMAIL] : sessionScope.currentUser.email}"
																required>
															<c:if
																test="${not empty requestScope[ATTR_ERRORS][P_CURRENT_EMAIL]}">
																<div class="invalid-feedback">${requestScope[ATTR_ERRORS][P_CURRENT_EMAIL]}</div>
															</c:if>
														</div>
														<div class="mb-3">
															<label for="currentPasswordEmail" class="form-label">Mật
																khẩu hiện tại</label> <input type="password"
																class="form-control ${not empty requestScope[ATTR_ERRORS][P_CURRENT_PASSWORD] ? 'is-invalid' : ''}"
																id="currentPasswordEmail" name="${P_CURRENT_PASSWORD}"
																value="" required>
															<c:if
																test="${not empty requestScope[ATTR_ERRORS][P_CURRENT_PASSWORD]}">
																<div class="invalid-feedback">${requestScope[ATTR_ERRORS][P_CURRENT_PASSWORD]}</div>
															</c:if>
														</div>
														<div class="mb-3">
															<label for="newEmail" class="form-label">Email
																mới</label> <input type="email"
																class="form-control ${not empty requestScope[ATTR_ERRORS][P_NEW_EMAIL] ? 'is-invalid' : ''}"
																id="newEmail" name="${P_NEW_EMAIL}"
																value="${requestScope[ATTR_VALUES][P_NEW_EMAIL]}"
																required>
															<c:if
																test="${not empty requestScope[ATTR_ERRORS][P_NEW_EMAIL]}">
																<div class="invalid-feedback">${requestScope[ATTR_ERRORS][P_NEW_EMAIL]}</div>
															</c:if>
														</div>
														<button type="submit" class="btn btn-primary">
															<i class="bi bi-envelope me-2"></i>Tiếp tục xác thực
														</button>
													</form>
												</c:otherwise>
											</c:choose>
										</div>
									</article>
								</c:when>
								<c:otherwise>
									<article class="card mb-4">
										<div class="card-header bg-white">
											<h5 class="card-title mb-0">Bảo mật</h5>
										</div>
										<div class="card-body">
											<div
												class="d-flex align-items-center justify-content-between flex-wrap gap-3 mb-3">
												<div>
													<div class="fw-semibold">Đổi mật khẩu</div>
													<div class="text-muted small">Cập nhật mật khẩu để
														tăng bảo mật tài khoản</div>
												</div>
												<a class="btn btn-outline-primary"
													href="${pageContext.request.contextPath}${RT_SECURITY_CHANGE_PASSWORD}">
													<i class="bi bi-key me-2"></i>Thay đổi
												</a>
											</div>
											<hr class="my-3">
											<div
												class="d-flex align-items-center justify-content-between flex-wrap gap-3">
												<div>
													<div class="fw-semibold">Đổi email</div>
													<div class="text-muted small">Thay đổi email và xác
														thực lại email mới</div>
												</div>
												<a class="btn btn-outline-primary"
													href="${pageContext.request.contextPath}${RT_SECURITY_CHANGE_EMAIL}">
													<i class="bi bi-envelope me-2"></i>Thay đổi
												</a>
											</div>
										</div>
									</article>
								</c:otherwise>
							</c:choose>

						</main>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</section>

	<jsp:include page="_footer.jsp" />
</body>

</html>
