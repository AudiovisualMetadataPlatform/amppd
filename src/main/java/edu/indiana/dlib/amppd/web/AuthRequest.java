package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class AuthRequest {
	private String username;
	private String password;
}
