package model;

/**
 * User role enumeration defining different permission levels.
 * BUYER can purchase items, SELLER can list and sell items,
 * ADMIN has full system management access.
 */
public enum UserRole {
    /** Can browse and purchase items. */
    BUYER,
    /** Can list items for sale and complete transactions. */
    SELLER,
    /** Has full administrative access to manage users and content. */
    ADMIN
}