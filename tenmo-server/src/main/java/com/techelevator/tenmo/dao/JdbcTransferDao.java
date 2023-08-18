package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Transfer updateTransfer(Transfer transfer) {
        Transfer updatedTransfer = null;

        String sql = "UPDATE transfer SET transfer_status_id = ? " +
                "WHERE transfer_id = ?;";

        try {
            jdbcTemplate.update(sql, transfer.getTransferStatusId(),
                    transfer.getTransferId());
            updatedTransfer = getTransferByTransferId(transfer.getTransferId());

        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return updatedTransfer;
    }

    @Override
    public Transfer getTransferByTransferId(int id) {
        Transfer transfer = null;
        String sql = "SELECT * FROM transfer WHERE transfer_id=?";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, id);
            if (results.next()) {
                transfer = mapRowToTransfer(results);
            }
        } catch (CannotGetJdbcConnectionException error) {
            throw new DaoException("Unable to connect to server or database", error);
        }
        return transfer;
    }


    @Override
    public Transfer createTransfer(Transfer transfer) {
        Transfer newTransfer = null;
        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?, ?, ?, ?, ?) RETURNING transfer_id";
        try {
            int newTransferId = jdbcTemplate.queryForObject(sql, int.class, transfer.getTransferTypeId(), transfer.getTransferStatusId(), transfer.getAccountFrom(), transfer.getAccountTo(), transfer.getAmount());
            newTransfer = getTransferByTransferId(newTransferId);
        } catch (CannotGetJdbcConnectionException error) {
            throw new DaoException("Unable to connect to server or database", error);
        } catch (DataIntegrityViolationException error) {
            throw new DaoException("Data integrity violation", error);
        }
        return newTransfer;
    }

    @Override
    public List<Transfer> allTransfersByAccountId(int accountId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT * FROM transfer WHERE account_from = ? OR account_to = ?";

        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
        while (results.next()) {
            Transfer transfer = mapRowToTransfer(results);
            transfers.add(transfer);
        }
        return transfers;
    }

        private Transfer mapRowToTransfer (SqlRowSet rs){
            Transfer transfer = new Transfer();
            transfer.setTransferId(rs.getInt("transfer_id"));
            transfer.setTransferTypeId(rs.getInt("transfer_type_id"));
            transfer.setTransferStatusId(rs.getInt("transfer_status_id"));
            transfer.setAccountFrom(rs.getInt("account_from"));
            transfer.setAccountTo(rs.getInt("account_to"));
            transfer.setAmount(rs.getBigDecimal("amount"));
            return transfer;
        }
    }

