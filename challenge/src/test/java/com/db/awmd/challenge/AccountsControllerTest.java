package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }
  
  @Test
  public void transferMoney() throws Exception {
    String uniqueAccountId1 = "Id1-" + System.currentTimeMillis();
    Account account1 = new Account(uniqueAccountId1, new BigDecimal("123.45"));
    this.accountsService.createAccount(account1);
    String uniqueAccountId2 = "Id2-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueAccountId2, new BigDecimal("444.45"));
    this.accountsService.createAccount(account2);
    BigInteger amountToBeTransferred = new BigInteger("100");
     
    this.mockMvc.perform(post("/v1/accounts/transfer/"+uniqueAccountId1+"/"+uniqueAccountId2+"/"+amountToBeTransferred)).andExpect(status().isOk())
    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
    .andExpect(content().string("{\"newDepositedAccountBalance\":544.45,\"newWithdrawnAccountBalance\":23.45}"));

  }
  
  @Test
  public void transferMoneyFailsWithInvalidAccountIdException() throws Exception {
    String uniqueAccountId1 = "Id1-" + System.currentTimeMillis();
    Account account1 = new Account(uniqueAccountId1, new BigDecimal("123.45"));
    this.accountsService.createAccount(account1);
    String uniqueAccountId2 = "Id2-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueAccountId2, new BigDecimal("444.45"));
    this.accountsService.createAccount(account2);
    BigInteger amountToBeTransferred = new BigInteger("100");
    
    String invalidAccountId = "Id3-" + System.currentTimeMillis();
    
    this.mockMvc.perform(post("/v1/accounts/transfer/"+uniqueAccountId1+"/"+invalidAccountId+"/"+amountToBeTransferred)).andExpect(status().isBadRequest());
  }
  
  @Test
  public void transferMoneyFailsWithInsuffiecientBalanceForTransfer() throws Exception {
    String uniqueAccountId1 = "Id1-" + System.currentTimeMillis();
    Account account1 = new Account(uniqueAccountId1, new BigDecimal("123.45"));
    this.accountsService.createAccount(account1);
    String uniqueAccountId2 = "Id2-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueAccountId2, new BigDecimal("444.45"));
    this.accountsService.createAccount(account2);
    BigInteger amountToBeTransferred = new BigInteger("5000");
        
    this.mockMvc.perform(post("/v1/accounts/transfer/"+uniqueAccountId1+"/"+uniqueAccountId2+"/"+amountToBeTransferred)).andExpect(status().isBadRequest());
  }
   
  
  
  @Test
  public void transferMoneyWithMultipleAccounts() throws Exception {
    String uniqueAccountId1 = "Id1-" + System.currentTimeMillis();
    Account account1 = new Account(uniqueAccountId1, new BigDecimal("100"));
    this.accountsService.createAccount(account1);
    String uniqueAccountId2 = "Id2-" + System.currentTimeMillis();
    Account account2 = new Account(uniqueAccountId2, new BigDecimal("200"));
    this.accountsService.createAccount(account2);
    BigInteger amountToBeTransferred = new BigInteger("100");
    String uniqueAccountId3 = "Id3-" + System.currentTimeMillis();
    Account account3 = new Account(uniqueAccountId3, new BigDecimal("100"));
    this.accountsService.createAccount(account3);
    
    this.mockMvc.perform(post("/v1/accounts/transfer/"+uniqueAccountId1+"/"+uniqueAccountId2+"/"+amountToBeTransferred)).andExpect(status().isOk())
    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(content().string("{\"newDepositedAccountBalance\":300,\"newWithdrawnAccountBalance\":0}"));
    this.mockMvc.perform(post("/v1/accounts/transfer/"+uniqueAccountId3+"/"+uniqueAccountId2+"/"+amountToBeTransferred)).andExpect(status().isOk())
    .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8)).andExpect(content().string("{\"newDepositedAccountBalance\":400,\"newWithdrawnAccountBalance\":0}"));
  }
}
