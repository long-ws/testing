<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@ taglib uri="jakarta.tags.functions" prefix="fn"%>
<%@ include file="_paramKeys.jsp" %>
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">

<head>
<jsp:include page="_meta.jsp" />
<title>Thông tin tài khoản</title>
</head>

<body>
	<jsp:include page="_header.jsp" />
	
	<section class="section-content padding-y">
		<div class="container">
			<div class="row">
				<c:choose>
					<c:when test="${empty sessionScope.currentUser}">
						<div class="col-12">
							<div class="alert alert-warning">
								Vui lòng <a href="${pageContext.request.contextPath}${RT_SIGNIN}">đăng nhập</a> để xem thông tin tài khoản.
							</div>
						</div>
					</c:when>
					<c:otherwise>
						<jsp:include page="_navPanel.jsp">
							<jsp:param name="active" value="USER" />
						</jsp:include>

						<main class="col-md-9">
							<!-- Profile Header Card -->
							<article class="card mb-4">
								<div class="card-body">
									<c:if test="${not empty requestScope[ATTR_SUCCESS_MESSAGE]}">
										<div class="alert alert-success">${requestScope[ATTR_SUCCESS_MESSAGE]}</div>
									</c:if>
									<c:if test="${not empty requestScope[ATTR_ERRORS][ERR_GLOBAL]}">
										<div class="alert alert-danger">${requestScope[ATTR_ERRORS][ERR_GLOBAL]}</div>
									</c:if>
									
									<div class="row align-items-center">
										<div class="col-lg-3 text-center mb-3">
											<img src="${requestScope[ATTR_AVATAR_SRC]}" alt="Avatar" class="rounded-circle" style="width: 150px; height: 150px; object-fit: cover; border: 3px solid #dee2e6;">
											<p class="small text-muted mt-2 mb-0">Ảnh đại diện</p>
										</div>
										<div class="col-lg-9">
											<h4 class="mb-1">${requestScope[ATTR_DISPLAY_NAME]}</h4>
											<p class="text-muted mb-2">
												<i class="bi bi-envelope me-2"></i>${sessionScope.currentUser.email}
											</p>
										</div>
									</div>
								</div>
							</article>

							<!-- Edit Profile Form -->
							<article class="card mb-4">
								<div class="card-header bg-white">
									<h5 class="card-title mb-0">Chỉnh sửa thông tin</h5>
								</div>
								<div class="card-body">
									<c:if test="${not empty requestScope[ATTR_ERRORS]}">
										<div class="alert alert-danger">
											<ul>
												<c:forEach var="error" items="${requestScope[ATTR_ERRORS]}">
													<li>${error.value}</li>
												</c:forEach>
											</ul>
										</div>
									</c:if>
									<form id="profileForm" action="${pageContext.request.contextPath}${RT_USER}" method="post">
										<div class="row">
											<div class="col-md-12 mb-3">
												<label for="fullname" class="form-label">Họ và tên <span class="text-danger">*</span></label>
												<input type="text" class="form-control ${not empty requestScope[ATTR_ERRORS][P_FULLNAME] ? 'is-invalid' : ''}" id="fullname" name="${P_FULLNAME}" 
													value="${requestScope[ATTR_USER] != null ? requestScope[ATTR_USER].fullname : sessionScope.currentUser.fullname}" required>
												<c:if test="${not empty requestScope[ATTR_ERRORS][P_FULLNAME]}">
													<div class="invalid-feedback">${requestScope[ATTR_ERRORS][P_FULLNAME]}</div>
												</c:if>
											</div>
										</div>
										<div class="row">
											<div class="col-md-6 mb-3">
												<label for="phoneNumber" class="form-label">Số điện thoại</label>
												<input type="tel" class="form-control ${not empty requestScope[ATTR_ERRORS][P_PHONE_NUMBER] ? 'is-invalid' : ''}" id="phoneNumber" name="${P_PHONE_NUMBER}" 
													value="${requestScope[ATTR_USER] != null ? requestScope[ATTR_USER].phoneNumber : sessionScope.currentUser.phoneNumber}">
												<c:if test="${not empty requestScope[ATTR_ERRORS][P_PHONE_NUMBER]}">
													<div class="invalid-feedback">${requestScope[ATTR_ERRORS][P_PHONE_NUMBER]}</div>
												</c:if>
											</div>
											<div class="col-md-6 mb-3">
												<label class="form-label">Ngôn ngữ ưa thích</label>
												<select class="form-select ${not empty requestScope[ATTR_ERRORS][P_PREFERRED_LANGUAGE] ? 'is-invalid' : ''}" name="${P_PREFERRED_LANGUAGE}">
													<c:forEach var="language" items="${requestScope[ATTR_ORDERED_LANGUAGES]}">
														<option value="${language.code}" ${requestScope[ATTR_SELECTED_LANGUAGE_CODE] eq language.code ? 'selected' : ''}>${language.name}</option>
													</c:forEach>
												</select>
												<c:if test="${not empty requestScope[ATTR_ERRORS][P_PREFERRED_LANGUAGE]}">
													<div class="invalid-feedback">${requestScope[ATTR_ERRORS][P_PREFERRED_LANGUAGE]}</div>
												</c:if>
											</div>
										</div>
										<div class="row">
											<div class="col-md-6 mb-3">
												<label class="form-label d-block">Giới tính</label>
												<c:forEach var="gender" items="${requestScope[ATTR_GENDERS]}">
													<div class="form-check form-check-inline">
														<input class="form-check-input" type="radio" name="${P_GENDER}" id="gender-${gender.code}" value="${gender.code}"
															${requestScope[ATTR_SELECTED_GENDER_CODE] eq gender.code ? 'checked' : ''}>
														<label class="form-check-label" for="gender-${gender.code}">${gender.name}</label>
													</div>
												</c:forEach>
												<c:if test="${not empty requestScope[ATTR_ERRORS][P_GENDER]}">
													<div class="text-danger small mt-1">${requestScope[ATTR_ERRORS][P_GENDER]}</div>
												</c:if>
											</div>
										</div>
										<button type="submit" class="btn btn-primary">
											<i class="bi bi-save me-2"></i>Lưu thay đổi
										</button>
									</form>
								</div>
							</article>


						</main>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</section>

	<jsp:include page="_footer.jsp" />
</body>

</html>
