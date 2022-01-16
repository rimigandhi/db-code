package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountIdException;
import com.db.awmd.challenge.exception.LowAccountBalanceException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class AccountsRepositoryInMemory implements AccountsRepository {

  private final Map<String, Account> accounts = new ConcurrentHashMap<>();

  @Override
  public void createAccount(Account account) throws DuplicateAccountIdException {
    Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
    if (previousAccount != null) {
      throw new DuplicateAccountIdException(
        "Account id " + account.getAccountId() + " already exists!");
    }
  }

  @Override
  public Account getAccount(String accountId) {
    return accounts.get(accountId);
  }

  @Override
  public void clearAccounts() {
    accounts.clear();
  }

@Override
public BigDecimal withdraw(String fromAccountId, BigDecimal amount) throws InvalidAccountIdException,LowAccountBalanceException{
	Account withDrawAccount  = getAccount(fromAccountId);
	
	// Check if account exists in order to withdraw funds
	if(withDrawAccount!=null) {
	BigDecimal accountBalance = withDrawAccount.getBalance();
		
		BigDecimal updateBalance = accountBalance.subtract(amount);
		//Check if updated balance greater than equal to zero
		if(updateBalance.signum() == 1 || updateBalance.signum() == 0) {
			withDrawAccount.setBalance(updateBalance);
			accounts.replace(fromAccountId, withDrawAccount);
			log.info("Updated balance after withdraw in Acoount Id {}", fromAccountId," is {}", withDrawAccount.getBalance());
			return updateBalance;
		}
		// If no sufficient account balance available to withdraw funds then throw new low account balance exception
		else {
			throw new LowAccountBalanceException("Unable to withdraw funds due to Low Account Balance for Account Id "+fromAccountId);
		}
	}
	// Throw invalid account id exception if wrong account id has been entered
	else {
		throw new InvalidAccountIdException("Please enter a valid account id!");
	}
}
  
@Override
public BigDecimal deposit(String toAccountId, BigDecimal amount) throws InvalidAccountIdException {
	Account depositAccount  = getAccount(toAccountId);
	// Check if account exists in order to deposit funds
	if(depositAccount!=null) {
	BigDecimal accountBalance = depositAccount.getBalance();
	
		BigDecimal updateBalance = accountBalance.add(amount);
		depositAccount.setBalance(updateBalance);
		accounts.replace(toAccountId, depositAccount);
		log.info("Updated balance after deposit in Account Id {}",toAccountId," is {}", depositAccount.getBalance());
		return updateBalance;
	}
	// Throw invalid account id exception if wrong account id has been entered
	else {
		throw new InvalidAccountIdException("Please enter a valid account id!");
	}
	}


}
