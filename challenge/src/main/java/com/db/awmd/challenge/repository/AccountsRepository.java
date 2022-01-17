package com.db.awmd.challenge.repository;

import java.math.BigDecimal;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountIdException;
import com.db.awmd.challenge.exception.LowAccountBalanceException;

public interface AccountsRepository {

  void createAccount(Account account) throws DuplicateAccountIdException;

  Account getAccount(String accountId);

  void clearAccounts();

  BigDecimal withdraw(String fromAccountId,String toAccountId, BigDecimal amount) throws InvalidAccountIdException,LowAccountBalanceException;
  
  BigDecimal deposit(String fromAccountId, String toAccountId, BigDecimal amount) throws InvalidAccountIdException;
  
 
  
}
