/*
 * ENUM MemberColor
 * 
 */
package de.hsos.kbse.app.enums;

/**
 *
 * @author Annika Limbrock
 */
public enum MemberColor {
    
    RED("Rot"), GREEN("Grün"), BLUE("Blau"), YELLOW("Gelb"), PINK("Pink"), ORANGE("Orange");
    
    private final String name;
    
    private MemberColor(String name){
        this.name = name;
    }
    
    public String getName(){
        return this.name;
    }
    
}
