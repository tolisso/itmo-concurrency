import java.util.concurrent.locks.*;

/**
 * Bank implementation.
 *
 * <p>:TODO: This implementation has to be made thread-safe.
 *
 * @author Malko Egor
 */
public class BankImpl implements Bank {
    /**
     * An array of accounts by index.
     */
    private final Account[] accounts;
    /**
     * Creates new bank instance.
     * @param n the number of accounts (numbered from 0 to n-1).
     */
    public BankImpl(int n) {
        accounts = new Account[n];
        for (int i = 0; i < n; i++) {
            accounts[i] = new Account();
        }
    }

    @Override
    public int getNumberOfAccounts() {
        return accounts.length;
    }

    /**
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getAmount(int index) {
	Account account = accounts[index];
	account.lock();
        long res = account.amount;
	account.unlock();
        return res;
    }

    /**
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long getTotalAmount() {
        long sum = 0;
	for (int i = 0; i < getNumberOfAccounts(); i++) {
	    accounts[i].lock();	
	}
        for (int i = 0; i < getNumberOfAccounts(); i++) {
            sum += accounts[i].amount;
        }
	for (int i = getNumberOfAccounts() - 1; i >= 0; i--) {
 	    accounts[i].unlock();
	}
        return sum;
    }

    /**
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long deposit(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        Account account = accounts[index];
	long res;
	account.lock();
	try {
            if (amount > MAX_AMOUNT || account.amount + amount > MAX_AMOUNT)
                throw new IllegalStateException("Overflow");
	    res = account.amount + amount;
            account.amount = res;
	} finally { 
	    account.unlock();
	}
        return res;
    }

    /**
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public long withdraw(int index, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        Account account = accounts[index];
	account.lock();
	long res;
	try {
            if (account.amount - amount < 0)
                throw new IllegalStateException("Underflow");
            account.amount -= amount;
	    res = account.amount;
	} finally {
	    account.unlock();
	}
        return res;
    }

    /**
     * <p>:TODO: This method has to be made thread-safe.
     */
    @Override
    public void transfer(int fromIndex, int toIndex, long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Invalid amount: " + amount);
        if (fromIndex == toIndex)
            throw new IllegalArgumentException("fromIndex == toIndex");
        Account from = accounts[fromIndex];
        Account to = accounts[toIndex];

	if (fromIndex < toIndex) {
	    from.lock();
	    try {
	        to.lock();
		try {
		    transferLockless(to, from, amount);
		} finally {
		    to.unlock();
		}
	    } finally {
		from.unlock();
	    }
	} else {
	    to.lock();
	    try {
	        from.lock();
		try {
		    transferLockless(to, from, amount);
		} finally {
		    from.unlock();
		}
	    } finally {
		to.unlock();
	    }
	}
    }

    private void transferLockless(Account to, Account from, long amount) {
	if (amount > from.amount)
            throw new IllegalStateException("Underflow");
        else if (amount > MAX_AMOUNT || to.amount + amount > MAX_AMOUNT)
            throw new IllegalStateException("Overflow");
        from.amount -= amount;
        to.amount += amount;
    }

    /**
     * Private account data structure.
     */
    static class Account {
        /**
         * Amount of funds in this account.
         */
	private ReentrantLock lock = new ReentrantLock();
        long amount;

	private void lock() {
	    lock.lock();
	}

	private void unlock() {
	    lock.unlock();
	}
    }
}
