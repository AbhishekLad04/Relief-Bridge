package com.reliefbridge.auth.entity;

import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name="users")
public class User {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false,length=100)
	private String name;
	
	@Column(nullable=false,unique=true,length=120)
	private String email;
	
	@Column(name="password",nullable=false)
	private String password;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false,length=20)
	private Role role;
	
	@Column(name="is_donor",nullable=false)
	private boolean isDonor=false;
	
	@Column(name="is_volunteer",nullable=false)
	private boolean isVolunteer=false;
	
	@Column(columnDefinition="TEXT")
	private String skills;
	
	@Column(name="org_name",length=150)
	private String orgName;
	
	@Column(name="reg_no",length=150)
	private String regNo;
	
	@Column(nullable=false)
	private boolean approved=false;
	
	@Column(name="created_at",nullable=false,updatable=false)
	private LocalDateTime createdAt;
	
	@Column(name = "token_version", nullable = false)
	private int tokenVersion = 0;
	
	public User() {
		
	}
	
	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
	
	public int getTokenVersion() {
	    return tokenVersion;
	}

	public void setTokenVersion(int tokenVersion) {
	    this.tokenVersion = tokenVersion;
	}
	
}
