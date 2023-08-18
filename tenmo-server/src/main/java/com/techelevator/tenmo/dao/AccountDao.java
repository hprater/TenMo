package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {

    Account getAccount(int id);
    Account updateAccount(Account account);
    Account getAccountByUserId(int id);
    int addFunds(int id, BigDecimal depositAmount);
    int withdrawFunds(int id, BigDecimal withdrawalAmount);
}
