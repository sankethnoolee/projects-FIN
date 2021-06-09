/**
 * 
 */
package com.sparkSpelPoc.poc.sparkSpelPoc.utils;

/**
 * @author Sanketh Noolee
 *
 */
public class identifiers {
	
	
	public static void main(String[] args) {
		for (int i = 0;i<10;i++) {
			//System.out.println(i);
			if (i%2==0) {
				System.err.println("even ->" + i);
				if (i%4==0) {
					System.out.println("("+i+")" );
				}
				
			}
		}
	}
}
