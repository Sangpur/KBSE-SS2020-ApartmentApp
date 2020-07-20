/*
 * INTERFACE EventManager
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.util.AppException;
import java.util.List;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
public interface EventManager {
    
    public void createEvent(Event event) throws AppException;
    public void deleteEvent(Event event) throws AppException;
    public Event updateEvent(Event event) throws AppException;
    public Event findEvent(Long id) throws AppException;
    public List<Event> getAllEventsFrom(Long apartmentID) throws AppException;
    
}
