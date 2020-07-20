/*
 * ENUM NoteCategory
 * 
 */
package de.hsos.kbse.app.enums;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
public enum NoteCategory {
    
    INFO("Info"), 
    TODO("ToDo"), 
    URGENT("Dringend");
    
    private final String status;
    
    private NoteCategory(String status){
        this.status = status;
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
    
    public String getStatus(){
        return status;
    }
}
