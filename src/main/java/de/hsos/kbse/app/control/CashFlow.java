/*
 * CONTROLLER CLASS CashFlow
 *
 */
package de.hsos.kbse.app.control;

import de.hsos.kbse.app.entity.features.Payment;
import de.hsos.kbse.app.entity.features.PaymentManager;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@RequestScoped
@Transactional
public class CashFlow implements PaymentManager, Serializable {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @PersistenceContext(name = "ApartmentPU")
    private EntityManager em;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */

    @Override
    public void createPayment(Payment payment) throws AppException {
        try {
            this.em.persist(payment);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Zahlung konnte nicht angelegt werden!");
        }
    }

    @Override
    public void deletePayment(Payment payment) throws AppException {
        try {
            Payment toMerge = this.em.merge(payment);
            this.em.remove(toMerge);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Zahlung konnte nicht geloescht werden!");
        }
    }

    @Override
    public Payment updatePayment(Payment payment) throws AppException {
        try {
            payment = this.em.merge(payment);           // Rueckgabe ist null, falls Zahlung nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Zahlung konnte nicht angepasst werden!");
        }
        return payment;
    }

    @Override
    public Payment findPayment(Long id) throws AppException {
        Payment payment = null;
        try {
            payment = this.em.find(Payment.class, id);  // Rueckgabe ist null, falls Zahlung nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Zahlung konnte nicht gefunden werden!");
        }
        return payment;
    }
    
    @Override
    public List<Payment> getAllPaymentsFrom(Long apartmentID) throws AppException {
        try {
            String str = "SELECT p FROM Payment p WHERE p.apartmentID = :id";
            TypedQuery<Payment> querySelect = em.createQuery(str, Payment.class);
            querySelect.setParameter("id", apartmentID);
            List<Payment> results = querySelect.getResultList();
            return results;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Zahlungen der WG "+ apartmentID +" konnten nicht gefunden werden!");
        }
    }
    
    @Override
    public void deleteAllPaymentsFrom(Long apartmentID) throws AppException {
        try {
            String str = "DELETE FROM Payment p WHERE p.apartmentID = :id";
            TypedQuery<Payment> querySelect = em.createQuery(str, Payment.class);
            querySelect.setParameter("id", apartmentID);
            querySelect.executeUpdate();
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Die Zahlungen der WG "+ apartmentID +" konnten nicht gelöscht werden!");
        }
    }
}
