/*
 * INTERFACE NoteManager
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.util.AppException;
import java.util.List;

/**
 *
 * @author Christoph Weigandt
 */
public interface NoteManager {
    
    public void createNote(Note note) throws AppException;
    public void deleteNote(Note note) throws AppException;
    public void deleteAllNotesFrom(Long apartmentID) throws AppException;
    public Note updateNote(Note note) throws AppException;
    public Note findNote(Long id) throws AppException;
    public List<Note> getAllNotesFrom(Long apartmentID) throws AppException;
    public List<Note> getAllNotesFromLastWeek(Long apartmentID) throws AppException;
    
}
