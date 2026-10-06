package servlet.general;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.ConstantUtils;
import utils.ImageUtils;

@WebServlet(name = "ImageServlet", value = "/image/*")
public class ImageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private String imageBaseDir;

    @Override
    public void init() throws ServletException {
        ImageUtils.init(getServletContext());

        String realPath = getServletContext().getRealPath("/images");
        if (realPath != null && !realPath.trim().isEmpty()) {
            this.imageBaseDir = realPath.trim();
        } else {
            this.imageBaseDir = ConstantUtils.getImageStoragePath().trim();
        }

        Path imageDir = Paths.get(this.imageBaseDir);
        if (!Files.exists(imageDir)) {
            try {
                Files.createDirectories(imageDir);
            } catch (IOException e) {
                throw new ServletException("Cannot create images directory: " + imageDir.toAbsolutePath(), e);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String fileName = URLDecoder.decode(pathInfo, "UTF-8");
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        if (fileName.contains("..")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Path imagePath = Paths.get(imageBaseDir, fileName).normalize().toAbsolutePath();
        Path basePath = Paths.get(imageBaseDir).normalize().toAbsolutePath();

        if (!imagePath.startsWith(basePath)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (!Files.exists(imagePath) || !Files.isRegularFile(imagePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = getServletContext().getMimeType(imagePath.getFileName().toString());
        if (mimeType == null || !mimeType.startsWith("image/")) {
            mimeType = "application/octet-stream";
        }

        response.reset();
        response.setContentType(mimeType);
        response.setContentLengthLong(Files.size(imagePath));

        Files.copy(imagePath, response.getOutputStream());
        response.getOutputStream().flush();
    }
}
