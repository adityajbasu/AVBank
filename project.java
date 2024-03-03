import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Scanner;

class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String s) {
        super(s);
    }
}

class AccountNotFoundException extends Exception {
    public AccountNotFoundException(String s) {
        super(s);
    }
}

class Account implements Serializable {
    double balance;
    long number;
    String name;
    String password;

    public Account(String name, String password, long number) {
        this.name = name;
        this.password = password;
        this.number = number;
        this.balance = 0.0;
    }

    public double getBalance() {
        return balance;
    }

    public long getNumber() {
        return number;
    }

    void deposit(double amount) {
        this.balance += amount;
    }

    void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= this.balance) {
            this.balance -= amount;
        } else {
            throw new InsufficientFundsException("Insufficient Balance to perform this transaction");
        }
    }

    boolean login(String pass) {
        if (this.password.equals(pass)) {
            return true;
        } else {
            return false;
        }
    }

}

class Bank {
    static Scanner scanner = new Scanner(System.in);
    Account customerAccount;

    public Account getCustomerAccount() {
        return customerAccount;
    }

    public void setCustomerAccount(Account customerAccount) {
        this.customerAccount = customerAccount;
    }

    int showLoginMenu() {
        // Show the create or login menu options and return the user response
        System.out.println("\n\n1.Create new Account\n2.Log in to an existing Account\n3.Quit");
        int choice = scanner.nextInt();
        scanner.nextLine();
        return choice;
    }

    int showAccountMenu() {
        // Show the account menu options and return the user response
        System.out.println("\n\n1.Deposit\n2.Withdrawal\n3.View Balance\n4.Transfer Funds\n5.Back to main menu");
        int choice = scanner.nextInt();
        return choice;
    }

    Account getAccount(long number) throws AccountNotFoundException {
        // Read file named number.acct and return Account object
        String fileName = String.valueOf(number) + ".acct";
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            final Account acct = (Account) in.readObject();
            return acct;
        } catch (IOException | ClassNotFoundException e) {
            throw new AccountNotFoundException("Account with number " + number + " not found");
        }
    }

    void saveAccountToFile(Account acct) throws IOException {
        // Save Account object to file
        String fileName = String.valueOf(acct.getNumber()) + ".acct";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(acct);
        } catch (IOException e) {
            throw e;
        }
    }

    public static void main(String[] args) {
        Bank bank = new Bank();
        int failedAttempts = 0;
        while (true) {
            while (failedAttempts < 3) {
                int choice = bank.showLoginMenu();
                if (choice == 1) {
                    if (createAccount(bank)) {
                        break;
                    }
                } else if (choice == 2) {
                    if (loginAccount(bank)) {
                        failedAttempts = 0;
                        break;
                    } else {
                        failedAttempts += 1;
                    }
                } else if (choice == 3) {
                    System.exit(0);
                }
            }
            Account acct = bank.getCustomerAccount();
            boolean backToMainMenu = false;
            while (!backToMainMenu) {
                int choice = bank.showAccountMenu();
                switch (choice) {
                    case 1:
                        // deposit
                        System.out.println("Enter Amount:");
                        double dAmount = scanner.nextDouble();
                        scanner.nextLine();
                        acct.deposit(dAmount);
                        System.out.println("Deposit Successful!\nBalance: " + acct.getBalance());
                        break;
                    case 2:
                        System.out.println("Enter Ammount");
                        double wAmount = scanner.nextDouble();
                        scanner.nextLine();
                        try {
                            acct.withdraw(wAmount);
                            System.out.println("Withdrawal Successful\nBalance: " + acct.getBalance());
                        } catch (InsufficientFundsException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    case 3:
                        System.out.println("Balance: " + acct.getBalance());
                        break;
                    case 4:
                        System.out.println("Accoutn Number of the account to transfer to");
                        long accno1 = scanner.nextLong();
                        scanner.nextLine();
                        try {
                            Account toAccount = bank.getAccount(accno1);
                            System.out.println("Amount to tranfer:");
                            double transferAmount = scanner.nextDouble();
                            scanner.nextLine();
                            try {
                                acct.withdraw(transferAmount);
                                toAccount.deposit(transferAmount);
                                System.out.println("Balance: " + acct.getBalance());
                                bank.saveAccountToFile(acct);
                                bank.saveAccountToFile(toAccount);
                            } catch (InsufficientFundsException e) {
                                System.out.println(e.getMessage());
                            } catch (IOException e) {
                                try {
                                    toAccount.withdraw(transferAmount);
                                } catch (InsufficientFundsException e1) {
                                    System.out.println(
                                            "Error Occured, funds can not be recovered, contact the bank immediately!!");
                                    System.exit(2);
                                }
                                acct.deposit(transferAmount);
                            }

                        } catch (AccountNotFoundException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    case 5:
                        backToMainMenu = true;
                        bank.setCustomerAccount(null);
                        break;
                    default:
                        System.out.println("invalid choice");
                        break;
                }
                try {
                    bank.saveAccountToFile(acct);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error occured! stopping program");
                    System.exit(1);
                }
            }
        }
    }

    private static boolean loginAccount(Bank bank) {
        System.out.println("Account Number:");
        long accno = scanner.nextLong();
        scanner.nextLine();
        System.out.println("Password:");
        String password = scanner.nextLine();
        try {
            Account acct = bank.getAccount(accno);
            if (acct.login(password)) {
                System.out.println("Logged in");
                bank.setCustomerAccount(acct);
                return true;
            } else {
                System.out.println("Invalid Password!");
                return false;
            }
        } catch (AccountNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private static boolean createAccount(Bank bank) {
        System.out.println("Name:");
        String name = scanner.nextLine();
        System.out.println("Password:");
        String password = scanner.nextLine();
        long accno = System.currentTimeMillis();
        Account acct = new Account(name, password, accno);
        try {
            bank.saveAccountToFile(acct);
            System.out.println("Account created successfully!");
            System.out.println("Your account number is: " + accno);
            bank.setCustomerAccount(acct);
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}
