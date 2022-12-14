package com.fitwise.utils.payments;

import java.util.concurrent.atomic.AtomicLong;

public class OrderNumberGenerator {

    private static final AtomicLong sequence = new AtomicLong(System.currentTimeMillis());

    public static String generateOrderNumber() {
        return "ORD" + sequence.incrementAndGet();
    }

    public static String generateInvoiceNumber() {
        return "#FITIV" + sequence.incrementAndGet();
    }

    public static String generateBillNumber() {
        return "#FITBL" + sequence.incrementAndGet();
    }

    public static String generateReceiptNumber() {
        return "#FITRN" + sequence.incrementAndGet();
    }

    public static String generateProductSKU(String productType) {
        return productType + sequence.incrementAndGet();
    }
}