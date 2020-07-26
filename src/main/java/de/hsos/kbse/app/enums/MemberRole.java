/*
 * ENUM MemberRole
 * 
 */
package de.hsos.kbse.app.enums;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
public enum MemberRole {
    
    ADMIN("Admin"), USER("User");
    
    private final String title;
    
    private MemberRole(String title){
        this.title = title;
    }
    
    public String getTitle(){
        return this.title;
    }
    
}
