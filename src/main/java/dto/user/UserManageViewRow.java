package dto.user;

public class UserManageViewRow {

	private final Long id;
	private final String username;
	private final String fullname;
	private final String email;
	private final String phoneNumber;
	private final String role;
	private final boolean system;
	private final boolean active;
	private final boolean editable;
	private final boolean selectable;

	public UserManageViewRow(Long id, String username, String fullname, String email, String phoneNumber, String role, boolean system, boolean active, boolean editable, boolean selectable) {
		this.id = id;
		this.username = username;
		this.fullname = fullname;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.role = role;
		this.system = system;
		this.active = active;
		this.editable = editable;
		this.selectable = selectable;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getFullname() {
		return fullname;
	}

	public String getEmail() {
		return email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getRole() {
		return role;
	}

	public boolean isSystem() {
		return system;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isEditable() {
		return editable;
	}

	public boolean isSelectable() {
		return selectable;
	}
}
