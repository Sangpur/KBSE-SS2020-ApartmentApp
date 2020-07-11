/*
 * CONTROLLER CLASS Calender
 *
 */
package de.hsos.kbse.app.control;

import de.hsos.kbse.app.entity.features.Event;
import de.hsos.kbse.app.entity.features.EventManager;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@RequestScoped
@Transactional
public class Calender implements EventManager, Serializable {
    
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
            throw new AppException("Kalendereintrag konnte nicht angelegt werden!");
        }
    }

    @Override
    public void deleteEvent(Event event) throws AppException {
        try {
            Event toMerge = this.em.merge(event);
            this.em.remove(toMerge);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Kalendereintrag konnte nicht geloescht werden!");
        }
    }

    @Override
    public Event updateEvent(Event event) throws AppException {
        try {
            event = this.em.merge(event);           // Rueckgabe ist null, falls Kalendereintrag nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Kalendereintrag konnte nicht angepasst werden!");
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
            throw new AppException("Kalendereintrag konnte nicht gefunden werden!");
        }
        return event;
    }
    
    /* -------------------------------------- PRIVATE METHODS -------------------------------------- */
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */
    
}
