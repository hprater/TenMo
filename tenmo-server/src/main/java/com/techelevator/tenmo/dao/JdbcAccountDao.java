package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JdbcAccountDao implements AccountDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Account getAccount(int id) {
        Account account = null;
        String sql = "SELECT account_id, user_id, balance FROM account WHERE account_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
            if (results.next())
                account = mapRowToAccount(results);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return account;
    }

    @Override
    public Account getAccountByUserId(int id) {
        Account account = null;
        String sql = "SELECT account_id, user_id, balance FROM account WHERE user_id = ?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
            if (results.next())
                account = mapRowToAccount(results);
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return account;
    }

    @Override
    public int addFunds(int id, BigDecimal depositAmount) {
        int rowsAffected = 0;
        String sql = "UPDATE account SET balance = balance + ? " +
                "WHERE account_id = ?;";
        try {
            rowsAffected = jdbcTemplate.update(sql, depositAmount, id);
        } catch (CannotGetJdbcConnectionException error) {
            throw new DaoException("Unable to connect to server or database", error);
        } catch (DataIntegrityViolationException error) {
            throw new DaoException("Data integrity violation", error);
        }
        return rowsAffected;
    }

    @Override
    public int withdrawFunds(int id, BigDecimal withdrawalAmount) {
        int rowsAffected = 0;
        String sql = "UPDATE account SET balance = balance - ? " +
                "WHERE account_id = ?;";
        try {
            rowsAffected = jdbcTemplate.update(sql, withdrawalAmount, id);
        } catch (CannotGetJdbcConnectionException error) {
            throw new DaoException("Unable to connect to server or database", error);
        } catch (DataIntegrityViolationException error) {
            throw new DaoException("Data integrity violation", error);
        }
        return rowsAffected;
    }

    @Override
    public Account updateAccount(Account account) {
        Account updatedAccount;
        String sql = "UPDATE account SET account_id = ?, user_id = ?, balance = ? " +
                "WHERE account_id = ?;";
        try {
            jdbcTemplate.update(sql, account.getAccountId(), account.getUserId(), account.getBalance(), account.getAccountId());
            updatedAccount = getAccount(account.getAccountId());
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return updatedAccount;
    }


    private Account mapRowToAccount(SqlRowSet rs) {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setUserId(rs.getInt("user_id"));
        account.setBalance(rs.getBigDecimal("balance"));
        return account;
    }
}
