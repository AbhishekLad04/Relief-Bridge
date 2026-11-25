package com.reliefbridge.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

	@NotBlank(message="Email is Required")
	@Email(message="Please Provide a Valid Email Address")
	private String email;
	
	@NotBlank(message="Password is Required")
	private String password;
	
	public LoginRequest() {
		
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
	
}
