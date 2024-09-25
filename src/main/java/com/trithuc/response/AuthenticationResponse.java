package com.trithuc.response;


import com.trithuc.model.Role;

public class AuthenticationResponse {

	private static final long serialVersionUID = -8091254;
	private String token;
	private Role role;
	public AuthenticationResponse(String token,Role role) {
		this.token = token;
		this.role=role;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	
}
