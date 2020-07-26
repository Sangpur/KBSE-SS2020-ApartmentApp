/*
 * CONTROLLER CLASS Calendar
 *
 */
package de.hsos.kbse.app.control;

import de.hsos.kbse.app.entity.features.Event;
import de.hsos.kbse.app.entity.features.EventManager;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

/**
 *
 * @author Christoph Weigandt
 */
@RequestScoped
@Transactional
public class Calendar implements EventManager, Serializable {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @PersistenceContext(name = "ApartmentPU")
    private EntityManager em;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */

    @Override
    public void createEvent(Event event) throws AppException {
        try {
            this.em.persist(event);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Events konnte nicht angelegt werden!");
        }
    }

    @Override
    public void deleteEvent(Event event) throws AppException {
        try {
            Event toMerge = this.em.merge(event);
            this.em.remove(toMerge);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Events konnte nicht geloescht werden!");
        }
    }

    @Override
    public Event updateEvent(Event event) throws AppException {
        try {
            event = this.em.merge(event);           // Rueckgabe ist null, falls Kalendereintrag nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Events konnte nicht angepasst werden!");
        }
        return event;
    }

    @Override
    public Event findEvent(Long id) throws AppException {
        Event event = null;
        try {
            event = this.em.find(Event.class, id);  // Rueckgabe ist null, falls Kalendereintrag nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Events konnte nicht gefunden werden!");
        }
        return event;
    }
    
    @Override
    public List<Event> getAllEventsFrom(Long apartmentID) throws AppException {
        try {
            String str = "SELECT e FROM Event e WHERE e.apartmentID = :id";
            TypedQuery<Event> querySelect = em.createQuery(str, Event.class);
            querySelect.setParameter("id", apartmentID);
            List<Event> results = querySelect.getResultList();
            return results;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Events der WG "+ apartmentID +" konnten nicht gefunden werden!");
        }
    }
       
    @Override
    public List<Event> getAllEventsFromMonth(Long apartmentID, int year, int month, int totalDays) throws AppException {
        Date beginMonth = java.sql.Date.valueOf(LocalDate.of(year, month, 1));
        Date endMonth = java.sql.Date.valueOf(LocalDate.of(year, month, totalDays));
        try {
            String str = "SELECT e FROM Event e "
                         + "WHERE e.apartmentID = :id AND e.begin >= :beginMonth AND e.begin <= :endMonth "
                         + "OR e.apartmentID = :id AND e.end >= :beginMonth AND e.end <= :endMonth";
            TypedQuery<Event> querySelect = em.createQuery(str, Event.class);
            querySelect.setParameter("id", apartmentID);
            querySelect.setParameter("beginMonth", beginMonth);
            querySelect.setParameter("endMonth", endMonth);
            List<Event> results = querySelect.getResultList();
            return results;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Events der WG "+ apartmentID +" konnten nicht gefunden werden!");
        }
    }
    
    @Override
    public void deleteAllEventsFrom(Long apartmentID) throws AppException {
        try {
            String str = "DELETE FROM Event e WHERE e.apartmentID = :id";
            TypedQuery<Event> querySelect = em.createQuery(str, Event.class);
            querySelect.setParameter("id", apartmentID);
            querySelect.executeUpdate();
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Die Events der WG "+ apartmentID +" konnten nicht gelöscht werden!");
        }
    }

}
