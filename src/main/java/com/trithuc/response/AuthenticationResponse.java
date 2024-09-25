package com.trithuc.response;


import com.trithuc.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

	private static final long serialVersionUID = -8091254;
	private String token;
	private Role role;
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	
}
