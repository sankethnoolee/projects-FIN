package com.fintellix.dld.authentication.authhelper;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DLDPasswordEncoder implements PasswordEncoder{

	@Override
	public String encode(CharSequence rawPassword) {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
		String hashed = passwordEncoder.encode(rawPassword);
		 
        return hashed;
	}

	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return new BCryptPasswordEncoder(12).matches(rawPassword, encodedPassword);

	}
	
	public static void main(String[] args) {
		DLDPasswordEncoder test= new DLDPasswordEncoder();
		System.err.println( test.matches("Welcome1", "$2a$12$YNKjX7WYUoialDEVYf7kbe474BJEql7RWxCZOI8SET9kF1frnxLp6"));
	}

}
