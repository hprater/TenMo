package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/transfer")
public class TransferController {

    private AccountDao accountDao;
    private TransferDao transferDao;
    private UserDao userDao;


    public TransferController(AccountDao accountDao, TransferDao transferDao, UserDao userDao) {
        this.accountDao = accountDao;
        this.transferDao = transferDao;
        this.userDao = userDao;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @RequestMapping(path = "/solo/{transferId}", method = RequestMethod.GET)
    public Transfer getTransferByTransferId(@PathVariable int transferId){
        Transfer transfer = transferDao.getTransferByTransferId(transferId);
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Auction Not Found");
        } else {
            return transfer;
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @RequestMapping(path = "/{accountId}", method = RequestMethod.GET)
    public List<Transfer> list(@PathVariable int accountId) {
        return transferDao.allTransfersByAccountId(accountId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "", method = RequestMethod.POST)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Transfer creatTransaction(@Valid @RequestBody Transfer transfer) {
        try {
            Transfer returnedTransfer = transferDao.createTransfer(transfer);
           if (transfer.getTransferStatusId() == 2) {
                accountDao.withdrawFunds(returnedTransfer.getAccountFrom(), returnedTransfer.getAmount());
                accountDao.addFunds(returnedTransfer.getAccountTo(), returnedTransfer.getAmount());
           }
            return returnedTransfer;
        }catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Auction Not Found");
        }
    }

    @RequestMapping(path = "/update/{transferId}", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Transfer updateTransaction(@Valid @RequestBody Transfer transfer, @PathVariable int transferId) {
        transfer.setTransferId(transferId);
        try{
            Transfer updatedTransfer = transferDao.updateTransfer(transfer);
            if(transfer.getTransferStatusId() == 2) {
                accountDao.withdrawFunds(updatedTransfer.getAccountTo(), updatedTransfer.getAmount());
                accountDao.addFunds(updatedTransfer.getAccountFrom(), updatedTransfer.getAmount());
            }
            return updatedTransfer;
        }catch (DaoException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Auction Not Found");
        }

    }
}

