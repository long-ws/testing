package beans.common;

import java.sql.Timestamp;

public class Gender {
	private int id;
	private String code;
	private String name;
	private String description;
	private Timestamp createdAt;
	private boolean isActive;

	public Gender() {
	}

	public Gender(int id, String code, String name, String description, Timestamp createdAt, boolean isActive) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.createdAt = createdAt;
		this.isActive = isActive;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}
}
