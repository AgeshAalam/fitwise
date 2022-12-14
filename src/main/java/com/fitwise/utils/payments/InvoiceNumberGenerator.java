package com.fitwise.utils.payments;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.HibernateException;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class InvoiceNumberGenerator implements IdentifierGenerator {

    private final static String label = "FIT";
    private final static SecureRandom sr = new SecureRandom();

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        long val = sr.nextLong();
        return label + Long.toString(Math.abs(val), Character.MAX_RADIX);
    }

}