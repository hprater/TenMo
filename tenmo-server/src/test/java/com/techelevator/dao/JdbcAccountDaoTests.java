package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.model.Account;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.math.BigDecimal;

public class JdbcAccountDaoTests extends BaseDaoTests{

    private static final Account ACCOUNT_1 = new Account(2001,1001, new BigDecimal("1000.00") );
    private static final Account ACCOUNT_2 = new Account(2002,1002, new BigDecimal("1000.00") );
    private static final Account ACCOUNT_3 = new Account(2003,1003, new BigDecimal("1000.00") );

    private JdbcAccountDao sut;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcAccountDao(jdbcTemplate);
        jdbcTemplate.update("INSERT INTO tenmo_user (username, password_hash, role) VALUES ('Hayden', 'helloHAY1124', 'ADMIN');");
        jdbcTemplate.update("INSERT INTO tenmo_user (username, password_hash, role) VALUES ('Denys', 'tempPass1234', 'ADMIN');");
        jdbcTemplate.update("INSERT INTO account (user_id, balance) VALUES (1001, 1000);");
        jdbcTemplate.update("INSERT INTO account (user_id, balance) VALUES (1002, 1000);");
    }

    @Test
    public void getAccountById_given_valid_account_id_returns_account() {
        Account actualAccount = sut.getAccount(ACCOUNT_1.getAccountId());
        Assert.assertEquals(ACCOUNT_1.getUserId(), actualAccount.getUserId());
    }

    @Test
    public void addFunds_increment_given_balance_given_valid_account_id(){
        int check = sut.addFunds(ACCOUNT_2.getAccountId(), new BigDecimal("300.00"));
        Account newBalanceAccount = sut.getAccount(ACCOUNT_2.getAccountId());
        Assert.assertEquals("Did not update any balance", 1, check);
        Assert.assertEquals("Did not return the correct value", new BigDecimal("1300.00"), newBalanceAccount.getBalance());
    }

    @Test
    public void withdrawFunds_increment_given_balance_given_valid_account_id(){
        int check = sut.withdrawFunds(ACCOUNT_2.getAccountId(), new BigDecimal("300.00"));
        Account newBalanceAccount = sut.getAccount(ACCOUNT_2.getAccountId());
        Assert.assertEquals("Did not update any balance", 1, check);
        Assert.assertEquals("Did not return the correct value", new BigDecimal("700.00"), newBalanceAccount.getBalance());
    }

    @Test
    public void updated_employee_has_expected_values_when_retrieved() {
        Account existingAccount = new Account();
        existingAccount.setAccountId(ACCOUNT_2.getAccountId());
        existingAccount.setUserId(ACCOUNT_2.getUserId());
        existingAccount.setBalance(new BigDecimal("1230.00"));

        Account updatedAccount = sut.updateAccount(existingAccount);

        Assert.assertNotNull("updateAccount should return the updated account", updatedAccount);
        Assert.assertEquals("updateAccount did not return an object with the same ID", existingAccount.getAccountId(), updatedAccount.getAccountId());
        Assert.assertEquals("updateAccount failed to assign new departmentId to account", 1002, updatedAccount.getUserId());
        Assert.assertEquals("updateAccount failed to assign new firstName to account", new BigDecimal("1230.00"), updatedAccount.getBalance());

        // verify value was saved to database, retrieve it and compare values
        Account retrievedAccount = getAccountByIdForTestVerification(ACCOUNT_2.getAccountId());
        assertAccountMatch("updateAccount failed to change account in database", updatedAccount, retrievedAccount);
    }

    public static void assertAccountMatch(String message, Account expected, Account actual) {
        Assert.assertEquals(message, expected.getAccountId(), actual.getAccountId());
        Assert.assertEquals(message, expected.getUserId(), actual.getUserId());
        Assert.assertEquals(message, expected.getBalance(), actual.getBalance());
    }

    private Account getAccountByIdForTestVerification(int id) {
        Account account = null;
        String sql = "SELECT account_id, user_id, balance " +
                "FROM account WHERE account_id=?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
        if (results.next()) {
            Account mappedAccount = new Account();
            mappedAccount.setAccountId(results.getInt("account_id"));
            mappedAccount.setUserId(results.getInt("user_id"));
            mappedAccount.setBalance(results.getBigDecimal("balance"));
            account = mappedAccount;
        }
        return account;
    }

}
