package com.reliefbridge.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.reliefbridge.auth.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

	
	@NotBlank(message="Name is required")
	@Size(max=100,message="Name should not exceep 100 characters")
	private String name;
	
	@NotBlank(message="Email is required")
	@Email(message="Please provide a valid email address")
	@Size(max=150,message="Email cannot exceed 150 characters")
	private String email;
	
	@NotBlank(message="Password is Required")
	@Size(min=12,max=72,message="Password must contain between 8 & 72 characters")
	private String password;
	
	@NotNull(message="Role is Required")
	private Role role;
	
	@JsonProperty("isDonor")
	private boolean isDonor;
	
	@JsonProperty("isVolunteer")
	private boolean isVolunteer;
	
	private String skills;
	
	private String orgName;
	
	private String regNo;
	
	public RegisterRequest() {
		
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public boolean isDonor() {
		return isDonor;
	}

	public void setDonor(boolean isDonor) {
		this.isDonor = isDonor;
	}

	public boolean isVolunteer() {
		return isVolunteer;
	}

	public void setVolunteer(boolean isVolunteer) {
		this.isVolunteer = isVolunteer;
	}

	public String getSkills() {
		return skills;
	}

	public void setSkills(String skills) {
		this.skills = skills;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getRegNo() {
		return regNo;
	}

	public void setRegNo(String regNo) {
		this.regNo = regNo;
	}
	
}
