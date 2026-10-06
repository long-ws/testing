package utils;

public final class ConstantUtils {
	private ConstantUtils() {
	}

	public static final int DB_PORT = 3306;
	public static final String SERVER_NAME = "localhost";
	public static final String DB_NAME = "bookshopdb";
	public static final String DB_USERNAME = "root";
	public static final String DB_PASSWORD = "12345";

	public static String getImageStoragePath() {
		String envPath = System.getenv("BOOKSHOP_IMAGE_PATH");
		if (envPath != null && !envPath.trim().isEmpty()) {
			return envPath.trim();
		}

		String jvmPath = System.getProperty("bookshop.image.path");
		if (jvmPath != null && !jvmPath.trim().isEmpty()) {
			return jvmPath.trim();
		}
		String os = System.getProperty("os.name").toLowerCase();
		if (os.contains("win")) {
			return "C:\\Users\\Thanh Ngan\\git\\WebBookShop3\\src\\main\\webapp\\images";
		} else {

		}
		return "/var/uploads/bookshop-images";
	}
}
