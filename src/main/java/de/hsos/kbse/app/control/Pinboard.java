/*
 * CONTROLLER CLASS Pinboard
 *
 */
package de.hsos.kbse.app.control;

import de.hsos.kbse.app.entity.features.Note;
import de.hsos.kbse.app.entity.features.NoteManager;
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
public class Pinboard implements NoteManager, Serializable {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @PersistenceContext(name = "ApartmentPU")
    private EntityManager em;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */

    @Override
    public void createNote(Note note) throws AppException {
        try {
            this.em.persist(note);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Notiz konnte nicht angelegt werden!");
        }
    }

    @Override
    public void deleteNote(Note note) throws AppException {
        try {
            Note toMerge = this.em.merge(note);
            this.em.remove(toMerge);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Notiz konnte nicht geloescht werden!");
        }
    }

    @Override
    public Note updateNote(Note note) throws AppException {
        try {
            note = this.em.merge(note);           // Rueckgabe ist null, falls Notiz nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Notiz konnte nicht angepasst werden!");
        }
        return note;
    }

    @Override
    public Note findNote(Long id) throws AppException {
        Note note = null;
        try {
            note = this.em.find(Note.class, id);  // Rueckgabe ist null, falls Notiz nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Notiz konnte nicht gefunden werden!");
        }
        return note;
    }
    
    @Override
    public List<Note> getAllNotesFrom(Long apartmentID) throws AppException {
        try {
            String str = "SELECT n FROM Note n WHERE n.apartmentID = :id";
            TypedQuery<Note> querySelect = em.createQuery(str, Note.class);
            querySelect.setParameter("id", apartmentID);
            List<Note> results = querySelect.getResultList();
            return results;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Notizen der WG "+ apartmentID +" konnten nicht gefunden werden!");
        }
    }
    
    @Override
    public void deleteAllNotesFrom(Long apartmentID) throws AppException {
        try {
            String str = "DELETE FROM Note n WHERE n.apartmentID = :id";
            TypedQuery<Note> querySelect = em.createQuery(str, Note.class);
            querySelect.setParameter("id", apartmentID);
            querySelect.executeUpdate();
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Die Notizen der WG "+ apartmentID +" konnten nicht gelöscht werden!");
        }
    }
}
