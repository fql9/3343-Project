package dao;

import model.Transaction;
import java.util.List;

public interface TransactionDao {

    Transaction findById(Long id);

    List<Transaction> findAll();

    List<Transaction> findByBuyerId(Long buyerId);

    List<Transaction> findBySellerId(Long sellerId);

    List<Transaction> findByItemId(Long itemId);

    Transaction findByBuyerAndItem(Long buyerId, Long itemId);

    void save(Transaction transaction);

    void update(Transaction transaction);

    void delete(Long id);
}

