package com.db.awmd.challenge.exception;

public class LowAccountBalanceException extends RuntimeException{
	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LowAccountBalanceException(String message) {
		    super(message);
		  }
}
