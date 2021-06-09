package com.fintellix.dldPasswordEncryption;

import java.util.Scanner;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


public class DLDPasswordEncoder implements PasswordEncoder{

	
	
	public static void main(String[] args) {
		
		DLDPasswordEncoder test= new DLDPasswordEncoder();
		Scanner in = new Scanner(System.in);
		
		System.out.println("Enter password to be encrypted - ");
		
		String text = in.nextLine();
		System.out.println("Encrypted password - "+test.encode(text));
		in.close();
	}

	public String encode(CharSequence rawPassword) {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
		String hashed = passwordEncoder.encode(rawPassword);
		 
        return hashed;
	}

	public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return new BCryptPasswordEncoder(12).matches(rawPassword, encodedPassword);

	}

}
