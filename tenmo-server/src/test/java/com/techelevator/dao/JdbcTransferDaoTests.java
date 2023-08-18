package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.math.BigDecimal;

public class JdbcTransferDaoTests extends BaseDaoTests{
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
    public void allTransfersByUserId_given_valid_user_id_returns_correct_list() {
        //TODO
    }

    @Test
    public void sendTransfer_performs_correctly() {
        //TODO
    }

    @Test
    public void detailsOfTransfer_returns_correct_details_for_valid_transfer_id() {
        //TODO
    }

    public static void assertTransferMatch(String message, Transfer expected, Transfer actual) {
        Assert.assertEquals(message, expected.getTransferId(), actual.getTransferId());
        Assert.assertEquals(message, expected.getTransferTypeId(), actual.getTransferTypeId());
        Assert.assertEquals(message, expected.getTransferStatusId(), actual.getTransferStatusId());
        Assert.assertEquals(message, expected.getAccountFrom(), actual.getAccountFrom());
        Assert.assertEquals(message, expected.getAccountTo(), actual.getAccountTo());
        Assert.assertEquals(message, expected.getAmount(), actual.getAmount());
    }

    private Transfer getTransferByIdForTestVerification(int id) {
        Transfer transfer = null;
        String sql = "SELECT transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount " +
                "FROM transfer WHERE transfer_id=?";
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
        if (results.next()) {
            Transfer mappedTransfer = new Transfer();
            mappedTransfer.setTransferId(results.getInt("transfer_id"));
            mappedTransfer.setTransferTypeId(results.getInt("transfer_type_id"));
            mappedTransfer.setTransferStatusId(results.getInt("transfer_status_id"));
            mappedTransfer.setAccountFrom(results.getInt("account_from"));
            mappedTransfer.setAccountTo(results.getInt("account_to"));
            mappedTransfer.setAmount(results.getBigDecimal("amount"));
            transfer = mappedTransfer;
        }
        return transfer;
    }
}
