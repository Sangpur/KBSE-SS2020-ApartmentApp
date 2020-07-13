/*
 * INTERFACE PaymentManager
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.util.AppException;
import java.util.List;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
public interface PaymentManager {
    
    public void createPayment(Payment payment) throws AppException;
    public void deletePayment(Payment payment) throws AppException;
    public Payment updatePayment(Payment payment) throws AppException;
    public Payment findPayment(Long id) throws AppException;
    public List<Payment> getAllPaymentsFrom(Long apartmentID) throws AppException;
    
}
