/*
 * INTERFACE NoteManager
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.util.AppException;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
public interface NoteManager {
    
    public void createNote(Note note) throws AppException;
    public void deleteNote(Note note) throws AppException;
    public Note updateNote(Note note) throws AppException;
    public Note findNote(Long id) throws AppException;
    
}
