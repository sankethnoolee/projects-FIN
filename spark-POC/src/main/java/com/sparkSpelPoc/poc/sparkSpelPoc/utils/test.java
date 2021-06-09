/**
 * 
 */
package com.sparkSpelPoc.poc.sparkSpelPoc.utils;

/**
 * @author Sanketh Noolee
 *
 */
public class test {
	public static void main(String[] args) {
		String s= "/opt/test/sanketh.jpg";
		System.out.println(s.trim().substring(s.lastIndexOf("/")+1));
	}
}
