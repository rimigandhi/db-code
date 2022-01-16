package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountIdException;
import com.db.awmd.challenge.exception.LowAccountBalanceException;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  @Order(1)
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }
  
  @Test
  public void withdrawMoney_failsOnInvalidAccountId() throws Exception {
    String fromAccountId = "Id-" + System.currentTimeMillis();
    BigDecimal amount = new BigDecimal("3000.45");
    try {
      this.accountsService.withdraw(fromAccountId, amount);
      fail("Should have failed when invalid account id is provided in transfer process");
    } catch (InvalidAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Please enter a valid account id!");
    }

  }
  
  @Test
  public void withdrawMoney_failsOnLowAccountBalanceException() throws Exception {
	  Account account = new Account("Id-234");
	  account.setBalance(new BigDecimal(1000));
	  this.accountsService.createAccount(account);
	  BigDecimal amount = new BigDecimal("3000.45");
    try {
      this.accountsService.withdraw(account.getAccountId(), amount);
      fail("Should have failed when invalid account id is provided in transfer process");
    } catch (LowAccountBalanceException ex) {
      assertThat(ex.getMessage()).isEqualTo("Unable to withdraw funds due to Low Account Balance for Account Id "+account.getAccountId());
    }

  }
  
  
  @Test
  public void depositMoney_failsOnInvalidAccountId() throws Exception {
    String toAccountId = "Id-" + System.currentTimeMillis();
    BigDecimal amount = new BigDecimal("3000.45");
    try {
      this.accountsService.deposit(toAccountId, amount);
      fail("Should have failed when invalid account id is provided in transfer process");
    } catch (InvalidAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Please enter a valid account id!");
    }

  }
}
