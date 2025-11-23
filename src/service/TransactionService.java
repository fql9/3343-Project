package service;

import dao.ItemDao;
import dao.TransactionDao;
import dao.impl.ItemDaoImpl;
import dao.impl.TransactionDaoImpl;
import model.Item;
import model.Transaction;
import model.TransactionStatus;
import model.DeliveryMethod;
import util.ValidationUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Transaction service class - handles transaction-related business logic
 */
public class TransactionService {

    private final TransactionDao transactionDao;
    private final ItemDao itemDao;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public TransactionService() {
        this.transactionDao = new TransactionDaoImpl();
        this.itemDao = new ItemDaoImpl();
    }
    
    /**
     * Initiate transaction (buyer starts a transaction)
     * @param buyerId Buyer ID
     * @param itemId Item ID
     * @param proposedPrice Proposed price (can be different from item price)
     * @return Transaction result message
     */
    public String initiateTransaction(Long buyerId, Long itemId, Double proposedPrice) {
        // Check if item exists
        Item item = itemDao.findById(itemId);
        if (item == null) {
            return "Item does not exist";
        }
        
        if (!item.isActive()) {
            return "Item is no longer available";
        }
        
        // Check if buyer is the seller
        if (item.getSellerId().equals(buyerId)) {
            return "Cannot buy your own item";
        }
        
        // Check if transaction already exists
        Transaction existing = transactionDao.findByBuyerAndItem(buyerId, itemId);
        if (existing != null && !existing.getStatus().equals(TransactionStatus.CANCELLED.name())) {
            return "Transaction already exists for this item";
        }
        
        // Use proposed price or item price
        double agreedPrice = (proposedPrice != null && proposedPrice > 0) ? proposedPrice : item.getPrice();
        
        // Validate price
        if (!ValidationUtils.isValidPrice(agreedPrice)) {
            return "Price must be greater than 0";
        }
        
        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setItemId(itemId);
        transaction.setBuyerId(buyerId);
        transaction.setSellerId(item.getSellerId());
        transaction.setAgreedPrice(agreedPrice);
        transaction.setStatus(TransactionStatus.PENDING.name());
        transaction.setItemReceived(false);
        transaction.setItemVerified(false);
        transaction.setFundsReleased(false);
        
        String now = LocalDateTime.now().format(DATE_FORMATTER);
        transaction.setCreatedTime(now);
        transaction.setUpdatedTime(now);
        
        transactionDao.save(transaction);
        return null; // Success returns null
    }
    
    /**
     * Start negotiation (seller responds to buyer's proposal)
     * @param transactionId Transaction ID
     * @param sellerId Seller ID
     * @return Negotiation result message
     */
    public String startNegotiation(Long transactionId, Long sellerId) {
        Transaction transaction = transactionDao.findById(transactionId);
        if (transaction == null) {
            return "Transaction does not exist";
        }
        
        if (!transaction.getSellerId().equals(sellerId)) {
            return "You are not the seller of this transaction";
        }
        
        if (!transaction.getStatus().equals(TransactionStatus.PENDING.name())) {
            return "Transaction is not in pending status";
        }
        
        transaction.setStatus(TransactionStatus.NEGOTIATING.name());
        transaction.setUpdatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        transactionDao.update(transaction);
        return null;
    }
    
    /**
     * Propose new price (during negotiation)
     * @param transactionId Transaction ID
     * @param userId User ID (buyer or seller)
     * @param newPrice New proposed price
     * @return Proposal result message
     */
    public String proposePrice(Long transactionId, Long userId, double newPrice) {
        Transaction transaction = transactionDao.findById(transactionId);
        if (transaction == null) {
            return "Transaction does not exist";
        }
        
        if (!transaction.getBuyerId().equals(userId) && !transaction.getSellerId().equals(userId)) {
            return "You are not part of this transaction";
        }
        
        if (!transaction.getStatus().equals(TransactionStatus.NEGOTIATING.name()) && 
            !transaction.getStatus().equals(TransactionStatus.PENDING.name())) {
            return "Cannot propose price in current transaction status";
        }
        
        if (!ValidationUtils.isValidPrice(newPrice)) {
            return "Price must be greater than 0";
        }
        
        transaction.setAgreedPrice(newPrice);
        transaction.setStatus(TransactionStatus.NEGOTIATING.name());
        transaction.setUpdatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        transactionDao.update(transaction);
        return null;
    }
    
    /**
     * Confirm transaction (both parties agree)
     * @param transactionId Transaction ID
     * @param userId User ID (buyer or seller)
     * @param deliveryMethod Delivery method
     * @param shippingAddress Shipping address (if shipping)
     * @return Confirmation result message
     */
    public String confirmTransaction(Long transactionId, Long userId, 
                                    DeliveryMethod deliveryMethod, String shippingAddress) {
        Transaction transaction = transactionDao.findById(transactionId);
        if (transaction == null) {
            return "Transaction does not exist";
        }
        
        if (!transaction.getBuyerId().equals(userId) && !transaction.getSellerId().equals(userId)) {
            return "You are not part of this transaction";
        }
        
        if (!transaction.getStatus().equals(TransactionStatus.PENDING.name()) && 
            !transaction.getStatus().equals(TransactionStatus.NEGOTIATING.name())) {
            return "Transaction cannot be confirmed in current status";
        }
        
        if (deliveryMethod == DeliveryMethod.SHIP && 
            (shippingAddress == null || shippingAddress.trim().isEmpty())) {
            return "Shipping address is required for shipping delivery";
        }
        
        transaction.setStatus(TransactionStatus.CONFIRMED.name());
        transaction.setDeliveryMethod(deliveryMethod.name());
        transaction.setShippingAddress(deliveryMethod == DeliveryMethod.SHIP ? shippingAddress : null);
        transaction.setUpdatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        transactionDao.update(transaction);
        return null;
    }
    
    /**
     * Mark item as shipped (seller confirms shipment)
     * @param transactionId Transaction ID
     * @param sellerId Seller ID
     * @param trackingNumber Tracking number (if shipping)
     * @return Shipment result message
     */
    public String markAsShipped(Long transactionId, Long sellerId, String trackingNumber) {
        Transaction transaction = transactionDao.findById(transactionId);
        if (transaction == null) {
            return "Transaction does not exist";
        }
        
        if (!transaction.getSellerId().equals(sellerId)) {
            return "You are not the seller of this transaction";
        }
        
        if (!transaction.getStatus().equals(TransactionStatus.CONFIRMED.name())) {
            return "Transaction must be confirmed before shipping";
        }
        
        transaction.setStatus(TransactionStatus.SHIPPED.name());
        if (transaction.getDeliveryMethod().equals(DeliveryMethod.SHIP.name()) && 
            trackingNumber != null && !trackingNumber.trim().isEmpty()) {
            transaction.setTrackingNumber(trackingNumber);
        }
        transaction.setUpdatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        transactionDao.update(transaction);
        return null;
    }
    
    /**
     * Mark item as delivered (buyer confirms receipt)
     * @param transactionId Transaction ID
     * @param buyerId Buyer ID
     * @return Delivery result message
     */
    public String markAsDelivered(Long transactionId, Long buyerId) {
        Transaction transaction = transactionDao.findById(transactionId);
        if (transaction == null) {
            return "Transaction does not exist";
        }
        
        if (!transaction.getBuyerId().equals(buyerId)) {
            return "You are not the buyer of this transaction";
        }
        
        if (!transaction.getStatus().equals(TransactionStatus.SHIPPED.name()) && 
            !transaction.getStatus().equals(TransactionStatus.CONFIRMED.name())) {
            return "Transaction must be shipped or confirmed before marking as delivered";
        }
        
        transaction.setStatus(TransactionStatus.DELIVERED.name());
        transaction.setItemReceived(true);
        transaction.setUpdatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        transactionDao.update(transaction);
        return null;
    }
    
    /**
     * Verify item condition (buyer verifies item quality)
     * @param transactionId Transaction ID
     * @param buyerId Buyer ID
     * @return Verification result message
     */
    public String verifyItemCondition(Long transactionId, Long buyerId) {
        Transaction transaction = transactionDao.findById(transactionId);
        if (transaction == null) {
            return "Transaction does not exist";
        }
        
        if (!transaction.getBuyerId().equals(buyerId)) {
            return "You are not the buyer of this transaction";
        }
        
        if (!transaction.isItemReceived()) {
            return "Item must be received before verification";
        }
        
        transaction.setItemVerified(true);
        transaction.setUpdatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        transactionDao.update(transaction);
        return null;
    }
    
    /**
     * Complete transaction (release funds after verification)
     * @param transactionId Transaction ID
     * @param userId User ID (buyer or seller)
     * @return Completion result message
     */
    public String completeTransaction(Long transactionId, Long userId) {
        Transaction transaction = transactionDao.findById(transactionId);
        if (transaction == null) {
            return "Transaction does not exist";
        }
        
        if (!transaction.getBuyerId().equals(userId) && !transaction.getSellerId().equals(userId)) {
            return "You are not part of this transaction";
        }
        
        if (!transaction.isItemReceived() || !transaction.isItemVerified()) {
            return "Item must be received and verified before completing transaction";
        }
        
        if (transaction.getStatus().equals(TransactionStatus.COMPLETED.name())) {
            return "Transaction is already completed";
        }
        
        transaction.setStatus(TransactionStatus.COMPLETED.name());
        transaction.setFundsReleased(true);
        transaction.setUpdatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        transactionDao.update(transaction);
        
        // Mark item as inactive
        Item item = itemDao.findById(transaction.getItemId());
        if (item != null) {
            item.setActive(false);
            itemDao.update(item);
        }
        
        return null;
    }
    
    /**
     * Cancel transaction
     * @param transactionId Transaction ID
     * @param userId User ID (buyer or seller)
     * @return Cancellation result message
     */
    public String cancelTransaction(Long transactionId, Long userId) {
        Transaction transaction = transactionDao.findById(transactionId);
        if (transaction == null) {
            return "Transaction does not exist";
        }
        
        if (!transaction.getBuyerId().equals(userId) && !transaction.getSellerId().equals(userId)) {
            return "You are not part of this transaction";
        }
        
        if (transaction.getStatus().equals(TransactionStatus.COMPLETED.name())) {
            return "Cannot cancel completed transaction";
        }
        
        if (transaction.getStatus().equals(TransactionStatus.CANCELLED.name())) {
            return "Transaction is already cancelled";
        }
        
        transaction.setStatus(TransactionStatus.CANCELLED.name());
        transaction.setUpdatedTime(LocalDateTime.now().format(DATE_FORMATTER));
        transactionDao.update(transaction);
        return null;
    }
    
    /**
     * Get transaction by ID
     * @param transactionId Transaction ID
     * @return Transaction object
     */
    public Transaction getTransactionById(Long transactionId) {
        return transactionDao.findById(transactionId);
    }
    
    /**
     * Get transactions by buyer
     * @param buyerId Buyer ID
     * @return Transaction list
     */
    public List<Transaction> getTransactionsByBuyer(Long buyerId) {
        return transactionDao.findByBuyerId(buyerId);
    }
    
    /**
     * Get transactions by seller
     * @param sellerId Seller ID
     * @return Transaction list
     */
    public List<Transaction> getTransactionsBySeller(Long sellerId) {
        return transactionDao.findBySellerId(sellerId);
    }
    
    /**
     * Get transactions by item
     * @param itemId Item ID
     * @return Transaction list
     */
    public List<Transaction> getTransactionsByItem(Long itemId) {
        return transactionDao.findByItemId(itemId);
    }
    
    /**
     * Get active transaction for buyer and item
     * @param buyerId Buyer ID
     * @param itemId Item ID
     * @return Transaction object
     */
    public Transaction getActiveTransaction(Long buyerId, Long itemId) {
        Transaction transaction = transactionDao.findByBuyerAndItem(buyerId, itemId);
        if (transaction != null && !transaction.getStatus().equals(TransactionStatus.CANCELLED.name()) &&
            !transaction.getStatus().equals(TransactionStatus.COMPLETED.name())) {
            return transaction;
        }
        return null;
    }
}

