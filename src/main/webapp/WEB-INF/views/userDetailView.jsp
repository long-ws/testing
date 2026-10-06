<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt"%>
<%@ include file="_paramKeys.jsp" %>
<fmt:setLocale value="vi_VN" />
<!DOCTYPE html>
<html lang="vi">

<head>
<jsp:include page="_meta.jsp" />
<title>Thông tin người dùng #${requestScope[ATTR_USER].id}</title>
</head>

<body>
	<jsp:include page="_headerAdmin.jsp" />

	<section class="section-content">
		<div class="container">
			<header class="section-heading py-4">
				<h3 class="section-title">Thông tin người dùng</h3>
			</header>
			<!-- section-heading.// -->

			<div class="card mb-5">
				<div class="card-body">
					<dl class="row">
						<dt class="col-md-3">ID</dt>
						<dd class="col-md-9">${requestScope[ATTR_USER].id}</dd>

						<dt class="col-md-3">Tên đăng nhập</dt>
						<dd class="col-md-9">${requestScope[ATTR_USER].username}</dd>

						<dt class="col-md-3">Họ và tên</dt>
						<dd class="col-md-9">${requestScope[ATTR_USER].fullname}</dd>

						<dt class="col-md-3">Email</dt>
						<dd class="col-md-9">${requestScope[ATTR_USER].email}</dd>

						<dt class="col-md-3">Số điện thoại</dt>
						<dd class="col-md-9">${requestScope[ATTR_USER].phoneNumber}</dd>

						<dt class="col-md-3">Giới tính</dt>
						<dd class="col-md-9">${requestScope[ATTR_USER].gender.name}</dd>
					</dl>
					<div class="mt-4">
						<a class="btn btn-primary"
							href="${pageContext.request.contextPath}${RT_ADMIN_USER}"> <i
							class="bi bi-arrow-left me-1"></i> Quay lại
						</a>
					</div>
				</div>
			</div>
			<!-- card.// -->
		</div>
		<!-- container.// -->
	</section>
	<!-- section-content.// -->

	<jsp:include page="_footerAdmin.jsp" />
</body>

</html>
