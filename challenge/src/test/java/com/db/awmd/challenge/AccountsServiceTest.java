package com.db.awmd.challenge;
import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountIdException;
import com.db.awmd.challenge.exception.LowAccountBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.annotation.Order;

@RunWith(MockitoJUnitRunner.class)
public class AccountsServiceTest {

	@Mock
	AccountsRepository accountsRepository;

	@Mock
	private AccountsService accountsService;

	@Before
	public void setUp() {
	}

	@Test
	@Order(1)
	public void addAccount() throws Exception {
		Account account = new Account("Id-123");
		account.setBalance(new BigDecimal(1000));
		Mockito.doNothing().when(accountsService).createAccount(account);
	}

	@Test
	public void addAccount_failsOnDuplicateId() throws Exception {
		String uniqueId = "Id-" + System.currentTimeMillis();
		Account account = new Account(uniqueId);
		this.accountsService.createAccount(account);

		try {
			this.accountsService.createAccount(account);
			Mockito.doThrow(DuplicateAccountIdException.class).when(accountsService).createAccount(account);
		} catch (DuplicateAccountIdException ex) {
		}

	}

	@Test
	public void withdrawMoney_failsOnInvalidAccountId() throws Exception {
		String fromAccountId = "Id-" + System.currentTimeMillis();
		String toAccountId = "Id2-" + System.currentTimeMillis();
		BigDecimal amount = new BigDecimal("3000.45");
		Mockito.doThrow(InvalidAccountIdException.class).when(accountsService).withdraw(fromAccountId, toAccountId,
				amount);
	}

	@Test
	public void withdrawMoney_failsOnLowAccountBalanceException() throws Exception {
		Account account = new Account("Id-234");
		account.setBalance(new BigDecimal(1000));
		this.accountsService.createAccount(account);
		String toAccountId = "Id2-" + System.currentTimeMillis();

		BigDecimal amount = new BigDecimal("3000.45");

		Mockito.doThrow(LowAccountBalanceException.class).when(accountsService).withdraw(account.getAccountId(),
				toAccountId, amount);
	}

	@Test
	public void depositMoney_failsOnInvalidAccountId() throws Exception {
		String toAccountId = "Id-" + System.currentTimeMillis();
		String fromAccountId = "Id2-" + System.currentTimeMillis();
		BigDecimal amount = new BigDecimal("3000.45");
		Mockito.doThrow(LowAccountBalanceException.class).when(accountsService).deposit(fromAccountId, toAccountId,
				amount);

	}
}
