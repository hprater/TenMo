package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;


@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/account")
public class AccountController {

    private AccountDao accountDao;
    private TransferDao transferDao;
    private UserDao userDao;

    public AccountController(AccountDao accountDao, TransferDao transferDao, UserDao userDao) {
        this.accountDao = accountDao;
        this.transferDao = transferDao;
        this.userDao = userDao;
    }

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public BigDecimal getBalanceByAccountId(Principal principal) {
        int userId = userDao.findIdByUsername(principal.getName());
        Account account = accountDao.getAccountByUserId(userId);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found.");
        } else {
            return account.getBalance();
        }
    }
    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Account getAccountById(@PathVariable int id) {
        Account account = accountDao.getAccount(id);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found.");
        } else {
            return account;
        }
    }

    @RequestMapping(path = "user/{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Account getAccountByUserId(@PathVariable int id) {
        Account account = accountDao.getAccountByUserId(id);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found.");
        } else {
            return account;
        }
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Account updateAccountById(@PathVariable int id, @Valid @RequestBody Account account) {
        account.setAccountId(id);
        try {
            Account updatedAccount = accountDao.updateAccount(account);
            return updatedAccount;
        } catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found.");
        }
    }
}
