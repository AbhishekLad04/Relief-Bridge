package com.reliefbridge.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.reliefbridge.auth.entity.Role;

public class AuthResponse {

	private String token;
	private Long userId;
	private String name;
	private String email;
	private Role role;
	
	@JsonProperty("isDonor")
	private boolean isDonor;
	@JsonProperty("isVolunteer")
	private boolean isVolunteer;
	
	
	public AuthResponse() {
		
	}


	public AuthResponse(String token, Long userId, String name, String email, Role role, boolean isDonor,
			boolean isVolunteer) {
		super();
		this.token = token;
		this.userId = userId;
		this.name = name;
		this.email = email;
		this.role = role;
		this.isDonor = isDonor;
		this.isVolunteer = isVolunteer;
	}


	public String getToken() {
		return token;
	}


	public void setToken(String token) {
		this.token = token;
	}


	public Long getUserId() {
		return userId;
	}


	public void setUserId(Long userId) {
		this.userId = userId;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public Role getRole() {
		return role;
	}


	public void setRole(Role role) {
		this.role = role;
	}

	@JsonProperty("isDonor")
	public boolean isDonor() {
		return isDonor;
	}


	public void setDonar(boolean isDonor) {
		this.isDonor = isDonor;
	}

	@JsonProperty("isVolunteer")
	public boolean isVolunteer() {
		return isVolunteer;
	}


	public void setVolunteer(boolean isVolunteer) {
		this.isVolunteer = isVolunteer;
	}

}
