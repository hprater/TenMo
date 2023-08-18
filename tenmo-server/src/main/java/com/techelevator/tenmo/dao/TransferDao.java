package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.util.List;

public interface TransferDao {

    Transfer updateTransfer(Transfer transfer);
    Transfer createTransfer(Transfer transfer);
    List<Transfer> allTransfersByAccountId(int accountId);
    Transfer getTransferByTransferId(int id);

}

