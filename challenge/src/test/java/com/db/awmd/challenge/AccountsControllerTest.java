package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountIdException;
import com.db.awmd.challenge.exception.LowAccountBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.repository.AccountsRepositoryInMemory;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.NotificationService;
import com.db.awmd.challenge.web.AccountsController;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class AccountsControllerTest {

//  private MockMvc mockMvc;

	@Mock
	AccountsController accountsController;

	@Mock
	NotificationService notificationService;

	@InjectMocks
	AccountsRepository accountsRepository = new AccountsRepositoryInMemory(notificationService);

	@Mock
	AccountsService accountsService = new AccountsService(accountsRepository);

	@Before
	public void prepareMockMvc() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void createAccount() throws Exception {
		Account account = new Account("Id-456");
		account.setBalance(new BigDecimal("1000"));
		Mockito.when(accountsController.createAccount(account))
				.thenReturn(new ResponseEntity<Object>(HttpStatus.CREATED));
	}

	@Test
	public void createDuplicateAccount() throws Exception {
		Account account = new Account("Id-456");
		account.setBalance(new BigDecimal("1000"));
		Mockito.when(accountsController.createAccount(account)).thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
		Mockito.doThrow(DuplicateAccountIdException.class).when(accountsController).createAccount(account);
	}

	@Test
	public void createAccountNoAccountId() throws Exception {
		Account account = new Account(null);
		account.setBalance(new BigDecimal("1000"));
		Mockito.when(accountsController.createAccount(account))
				.thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void createAccountNoBalance() throws Exception {
		Account account = new Account(null);
		Mockito.when(accountsController.createAccount(account))
				.thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

	}

	@Test
	public void createAccountNoBody() throws Exception {
		Mockito.when(accountsController.createAccount(null)).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void createAccountNegativeBalance() throws Exception {
		Account account = new Account(null);
		account.setBalance(new BigDecimal("-500"));
		Mockito.when(accountsController.createAccount(account))
				.thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void createAccountEmptyAccountId() throws Exception {
		Account account = new Account("");
		account.setBalance(new BigDecimal("1000"));
		Mockito.when(accountsController.createAccount(account))
				.thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
	}

	@Test
	public void getAccount() throws Exception {
		String uniqueAccountId = "Id5-" + System.currentTimeMillis();
		Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
		Mockito.doNothing().when(accountsService).createAccount(account);
		Mockito.when(accountsController.getAccount(uniqueAccountId)).thenReturn(account);
	}

	@Test
	public void transferMoney() throws Exception {

		String uniqueAccountId1 = "Id1-" + System.currentTimeMillis();
		String uniqueAccountId2 = "Id2-" + System.currentTimeMillis();
		BigDecimal amountToBeTransferred = new BigDecimal("100");
		Map<String, BigDecimal> balances = new ConcurrentHashMap<>();
		balances.put("newWithdrawnAccountBalance", new BigDecimal("23.45"));
		balances.put("newDepositedAccountBalance", new BigDecimal("544.45"));
		ResponseEntity<Object> r = new ResponseEntity<>(balances, HttpStatus.OK);
		Mockito.when(accountsService.withdraw(uniqueAccountId1, uniqueAccountId2, amountToBeTransferred))
				.thenReturn(new BigDecimal("23.45"));
		Mockito.when(accountsService.deposit(uniqueAccountId1, uniqueAccountId2, amountToBeTransferred))
				.thenReturn(new BigDecimal("23.45"));
		Mockito.when(accountsController.transferMoney(uniqueAccountId1, uniqueAccountId2, amountToBeTransferred))
				.thenReturn(r);

	}

	@Test
	public void transferMoneyFailsWithInvalidAccountIdException() throws Exception {
		String uniqueAccountId1 = "Id1-" + System.currentTimeMillis();
		Account account1 = new Account(uniqueAccountId1, new BigDecimal("123.45"));
		this.accountsService.createAccount(account1);
		String uniqueAccountId2 = "Id2-" + System.currentTimeMillis();
		Account account2 = new Account(uniqueAccountId2, new BigDecimal("444.45"));
		this.accountsService.createAccount(account2);
		BigDecimal amountToBeTransferred = new BigDecimal("100");
		Mockito.doThrow(InvalidAccountIdException.class).when(accountsController).transferMoney(uniqueAccountId1,
				uniqueAccountId2, amountToBeTransferred);
	}

	@Test
	public void transferMoneyFailsWithInsuffiecientBalanceForTransfer() throws Exception {
		String uniqueAccountId1 = "Id1-" + System.currentTimeMillis();
		Account account1 = new Account(uniqueAccountId1, new BigDecimal("123.45"));
		Mockito.doNothing().when(accountsService).createAccount(account1);
		String uniqueAccountId2 = "Id2-" + System.currentTimeMillis();
		Account account2 = new Account(uniqueAccountId2, new BigDecimal("444.45"));
		Mockito.doNothing().when(accountsService).createAccount(account2);
		BigDecimal amountToBeTransferred = new BigDecimal("5000");
		Mockito.doThrow(LowAccountBalanceException.class).when(accountsController).transferMoney(uniqueAccountId1,
				uniqueAccountId2, amountToBeTransferred);
	}

	@Test
	public void transferMoneyWithMultipleAccounts() throws Exception {
		String uniqueAccountId1 = "Id1-" + System.currentTimeMillis();
		Account account1 = new Account(uniqueAccountId1, new BigDecimal("100"));
		String uniqueAccountId2 = "Id2-" + System.currentTimeMillis();
		Account account2 = new Account(uniqueAccountId2, new BigDecimal("200"));
		BigDecimal amountToBeTransferred = new BigDecimal("100");
		String uniqueAccountId3 = "Id3-" + System.currentTimeMillis();
		Account account3 = new Account(uniqueAccountId3, new BigDecimal("100"));
		Mockito.doNothing().when(accountsService).createAccount(account1);
		Mockito.doNothing().when(accountsService).createAccount(account2);
		Mockito.doNothing().when(accountsService).createAccount(account3);

		Map<String, BigDecimal> balances1 = new ConcurrentHashMap<>();
		balances1.put("newWithdrawnAccountBalance", new BigDecimal("0"));
		balances1.put("newDepositedAccountBalance", new BigDecimal("300"));

		Map<String, BigDecimal> balances2 = new ConcurrentHashMap<>();
		balances2.put("newWithdrawnAccountBalance", new BigDecimal("0"));
		balances2.put("newDepositedAccountBalance", new BigDecimal("400"));

		Mockito.when(accountsController.transferMoney(uniqueAccountId1, uniqueAccountId2, amountToBeTransferred))
				.thenReturn(new ResponseEntity<Object>(balances1, HttpStatus.OK));
		Mockito.when(accountsController.transferMoney(uniqueAccountId3, uniqueAccountId2, amountToBeTransferred))
				.thenReturn(new ResponseEntity<Object>(balances2, HttpStatus.OK));

	}
}
