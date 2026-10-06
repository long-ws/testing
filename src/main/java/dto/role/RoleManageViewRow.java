package dto.role;

public class RoleManageViewRow {

	private final Integer id;
	private final String code;
	private final String name;
	private final String description;
	private final boolean system;
	private final boolean active;
	private final boolean editable;
	private final boolean selectable;

	public RoleManageViewRow(Integer id, String code, String name, String description, boolean system, boolean active, boolean editable, boolean selectable) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.system = system;
		this.active = active;
		this.editable = editable;
		this.selectable = selectable;
	}

	public Integer getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
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
