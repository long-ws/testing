package servlet.client;

import java.io.IOException;
import constants.ViewPathConstants;
import helpers.FormRenderHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "SecurityServlet", value = "/security")
public class SecurityServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		FormRenderHelper.forward(request, response, ViewPathConstants.Client.SECURITY);
	}
}
