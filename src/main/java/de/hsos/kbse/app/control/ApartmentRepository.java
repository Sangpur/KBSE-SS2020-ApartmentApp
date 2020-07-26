/*
 * CONTROLLER CLASS ApartmentRepository
 *
 */
package de.hsos.kbse.app.control;

import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.ApartmentManager;
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
 * @author Annika Limbrock, Lucca Oberhößel
 */
@RequestScoped
@Transactional
public class ApartmentRepository implements ApartmentManager, Serializable {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @PersistenceContext(name = "ApartmentPU")
    private EntityManager em;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    @Override
    public void createApartment(Apartment apartment) throws AppException {
        try {
            this.em.persist(apartment);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("WG konnte nicht angelegt werden!");
        }
    }

    @Override
    public void deleteApartment(Apartment apartment) throws AppException {
        try {
            Apartment toMerge = this.em.merge(apartment);
            this.em.remove(toMerge);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("WG konnte nicht geloescht werden!");
        }
    }

    @Override
    public Apartment updateApartment(Apartment apartment) throws AppException {
        try {
            apartment = this.em.merge(apartment);           // Rueckgabe ist null, falls WG nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("WG konnte nicht angepasst werden!");
        }
        return apartment;
    }

    @Override
    public Apartment findApartment(Long id) throws AppException {
        Apartment apartment = null;
        try {
            apartment = this.em.find(Apartment.class, id);  // Rueckgabe ist null, falls WG nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("WG konnte nicht gefunden werden!");
        }
        return apartment;
    }
    
    
    @Override
    public List<Apartment> getAllApartments() throws AppException {
        try {
            String str = "SELECT m FROM Apartment m";
            TypedQuery<Apartment> querySelect = em.createQuery(str, Apartment.class);
            List<Apartment> results = querySelect.getResultList();
            return results;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("WGs konnten nicht ausgegeben werden!");
        }
    }
    
}
