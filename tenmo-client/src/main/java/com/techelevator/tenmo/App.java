package com.techelevator.tenmo;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransferService;
import com.techelevator.tenmo.services.UserService;

import java.math.BigDecimal;
import java.util.InputMismatchException;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AccountService accountService = new AccountService();
    private final UserService userService = new UserService();
    private final TransferService transferService = new TransferService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }

    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        } else {
            String token = currentUser.getToken();
            accountService.setAuthToken(token);
            transferService.setAuthToken(token);
            userService.setAuthToken(token);
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

    private void viewCurrentBalance() {
        BigDecimal balance = accountService.getCurrentBalance();
        System.out.println("\n-------------------------------------------");
        System.out.println("\tCurrent balance: $" + balance);
        System.out.println("-------------------------------------------");
    }

    private void viewTransferHistory() {
        System.out.println("\n-------------------------------------------");
        System.out.println("Transfers\n");
        System.out.printf("%-10s %-20s %-7s", "ID", "From/To", "Amount");
        System.out.println("\n-------------------------------------------");
        Transfer[] transfers = transferService.transfersHistoryForAccount(accountService.getAccountIdByUserId(currentUser.getUser().getId()));
        for (Transfer transfer : transfers) {
            System.out.printf("%-10s %-20s %-7s", transfer.getTransferId(),
                    transfer.getAccountFrom() == accountService.getAccountIdByUserId(currentUser.getUser().getId()) ?
                            "To:   " + userService.getUsernameById(accountService.getAccountByAccountId(transfer.getAccountTo()).getUserId()).toUpperCase() :
                            "From: " + userService.getUsernameById(accountService.getAccountByAccountId(transfer.getAccountFrom()).getUserId()).toUpperCase(),
                    "$" + transfer.getAmount());
            System.out.print("\n");
        }
        System.out.println("-------------------------------------------");
        int transferId = consoleService.promptForInt("\nPlease enter transfer ID to view details (0 to exit): ");
        if (transferId == 0) {
            System.out.println("\nReturning to Main Menu");
            return;
        }
        System.out.println("\n");
        Transfer transfer = transferService.getTransferByTransferId(transferId);
        if (transfer != null) {
            System.out.printf("%-14s %-10s %-12s %-17s %-15s %-10s", "TransferID", "Type", "Status", "AccountFromID", "AccountToID", "Amount");
            System.out.print("\n");
            System.out.printf("%-14s %-10s %-12s %-17s %-15s %-10s", transfer.getTransferId(), transfer.getTransferTypeId() == 2 ? "Send" : "Request", transfer.statusOutput(transfer.getTransferStatusId()),
                    transfer.getAccountFrom(), transfer.getAccountTo(), "$" + transfer.getAmount());
            System.out.println("\n");
        } else
            System.out.println("\nInvalid Transaction Id");
    }

    private void viewPendingRequests() {
        System.out.println("\n-------------------------------------------");
        System.out.println("Pending Request\n");
        System.out.printf("%-10s %-20s %-7s", "ID", "To", "Amount");
        System.out.println("\n-------------------------------------------");
        Transfer[] allTransfers = transferService.transfersHistoryForAccount(accountService.getAccountIdByUserId(currentUser.getUser().getId()));
        for (Transfer transfer : allTransfers) {
            if (transfer.getTransferTypeId() == 1 && transfer.getTransferStatusId() == 1 && transfer.getAccountTo() == accountService.getAccountIdByUserId(currentUser.getUser().getId())) {
                System.out.printf("%-10s %-20s %-7s", transfer.getTransferId(),
                        "To: " + userService.getUsernameById(accountService.getAccountByAccountId(transfer.getAccountFrom()).getUserId()).toUpperCase(),
                        "$" + transfer.getAmount());
                System.out.print("\n");
            }
        }
        System.out.println("-------------------------------------------");
        int pendingTransferId = consoleService.promptForInt("\nPlease enter transfer ID to approve/reject (0 to exit): ");
        if (pendingTransferId == 0) {
            System.out.println("\nReturning to Main Menu");
            return;
        }
        Transfer pendingTransfer = transferService.getTransferByTransferId(pendingTransferId);
        if (pendingTransfer != null && pendingTransfer.getTransferStatusId() == 1 && pendingTransfer.getTransferTypeId() == 1) {
            int choice = consoleService.promptForInt("\n----------------------------" +
                    "\n1: Approve" +
                    "\n2: Reject" +
                    "\n3: Don't approve or reject" +
                    "\n----------------------------" +
                    "\nPlease choose an option: ");
            if (choice == 3) {
                System.out.println("\nReturning to Main Menu");
            } else if (choice == 1) {

                boolean check = false;
                pendingTransfer.setTransferStatusId(2);
                if (pendingTransfer.getAmount().compareTo(accountService.getCurrentBalance()) <= 0)
                    check = transferService.updateTransfer(pendingTransfer);
                else
                    System.out.println("\n **Insufficient Funds** ");

                if (check) {
                    System.out.println("\n*** Transaction Confirmation ***" +
                            "\nTransaction Id: " + pendingTransfer.getTransferId() +
                            "\nSet Status: " + "Approved" +
                            "\nAmount: " + pendingTransfer.getAmount());
                } else
                    System.out.println("\nCould Not Process Request");
            } else if (choice == 2) {
                pendingTransfer.setTransferStatusId(3);
                boolean check = transferService.updateTransfer(pendingTransfer);
                if (check) {
                    System.out.println("\n*** Transaction Confirmation ***" +
                            "\nTransaction Id: " + pendingTransfer.getTransferId() +
                            "\nSet Status: " + "Rejected" +
                            "\nAmount: " + pendingTransfer.getAmount());
                } else
                    System.out.println("\nCould Not Process Request");
            } else
                System.out.println("\nInvalid Choice");
        } else
            System.out.println("\nInvalid Transaction Id");
    }

    private void sendBucks() {
        System.out.println("\n-------------------------------------------");
        System.out.println("Valid Users\n");
        System.out.printf("%-15s", "USERNAME:");
        System.out.print("\n");

        User[] users = userService.getAllUsers();
        for (User user : users) {
            System.out.println("\t" + user.getUsername().toUpperCase());
        }
        System.out.println("\n-------------------------------------------");
        String recipientUsername = consoleService.promptForString("Please enter the username of the recipient (0 to exit): ");
        if (recipientUsername.equals("0"))
            return;
        int isValidUser = userService.getUserIdByUsername(recipientUsername);
        BigDecimal amount = consoleService.promptForBigDecimal("Please enter the amount you wish to send: ");

        if (amount.compareTo(accountService.getCurrentBalance()) <= 0 && isValidUser != 0 && isValidUser != currentUser.getUser().getId()) {
            Transfer sendTransfer = new Transfer(2, 2, accountService.getAccountIdByUserId(currentUser.getUser().getId()),
                    accountService.getAccountIdByUserId(userService.getUserIdByUsername(recipientUsername)), amount);
            Transfer recieptTransfer = transferService.sendTransfer(sendTransfer);

            if (recieptTransfer != null) {
                System.out.println("\n*** Transaction Confirmation ***" +
                        "\nTransaction Id: " + recieptTransfer.getTransferId() +
                        "\nSender: " + userService.getUsernameById(currentUser.getUser().getId()).toUpperCase() +
                        "\nRecipient: " + recipientUsername.toUpperCase() +
                        "\nAmount: " + recieptTransfer.getAmount() +
                        "\nStatus: " + recieptTransfer.statusOutput(recieptTransfer.getTransferStatusId()));

            } else {
                System.out.println("\nTransaction Terminated");
            }
        } else if (amount.compareTo(accountService.getCurrentBalance()) > 0)
            System.out.println("\n **Insufficient Funds** ");
        else
            System.out.println("\nInvalid Recipient");
    }

    private void requestBucks() {
        System.out.println("\n-------------------------------------------");
        System.out.println("Valid Users\n");
        System.out.printf("%-15s", "USERNAME:");
        System.out.print("\n");

        User[] users = userService.getAllUsers();
        for (User user : users) {
            System.out.println("\t" + user.getUsername().toUpperCase());
        }
        System.out.println("\n-------------------------------------------");
        String recipientUsername = consoleService.promptForString("Please enter the username of the recipient (0 to exit): ");
        if (recipientUsername.equals("0"))
            return;
        int isValidUser = userService.getUserIdByUsername(recipientUsername);
        BigDecimal amount = consoleService.promptForBigDecimal("Please enter the amount you wish to request: ");
        if (isValidUser != 0 && isValidUser != currentUser.getUser().getId() && amount.compareTo(BigDecimal.ZERO) > 0) {
            Transfer sendTransfer = new Transfer(1, 1, accountService.getAccountIdByUserId(currentUser.getUser().getId()),
                    accountService.getAccountIdByUserId(userService.getUserIdByUsername(recipientUsername)), amount);
            Transfer recieptTransfer = transferService.sendTransfer(sendTransfer);

            if (recieptTransfer != null) {
                System.out.println("\n*** Transaction Confirmation ***" +
                        "\nTransaction Id: " + recieptTransfer.getTransferId() +
                        "\nSender: " + userService.getUsernameById(currentUser.getUser().getId()).toUpperCase() +
                        "\nRecipient: " + recipientUsername.toUpperCase() +
                        "\nAmount: " + recieptTransfer.getAmount() +
                        "\nStatus: " + recieptTransfer.statusOutput(recieptTransfer.getTransferStatusId()));

            } else {
                System.out.println("\nTransaction Terminated");
            }
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0)
            System.out.println("\nInvalid Amount");
        else
            System.out.println("\nInvalid Recipient");
    }


}
