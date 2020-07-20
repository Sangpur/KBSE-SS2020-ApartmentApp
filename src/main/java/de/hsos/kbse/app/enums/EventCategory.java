/*
 * ENUM NoteCategory
 * 
 */
package de.hsos.kbse.app.enums;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
public enum EventCategory {
    
    BIRTHDAY("Geburtstag"), 
    EVENT("Veranstaltung"), 
    VACATION("Urlaub"), 
    APPOINTMENT("Termin"), 
    OTHER("Sonstiges");
    
    private final String status;
    
    private EventCategory(String status){
        this.status = status;
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
    
    public String getStatus(){
        return this.status;
    }
}
