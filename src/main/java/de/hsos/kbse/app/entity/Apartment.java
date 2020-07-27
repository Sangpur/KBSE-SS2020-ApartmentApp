/*
 * ENTITY CLASS Apartment
 *
 */
package de.hsos.kbse.app.entity;

import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import java.io.Serializable;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel
 */
@Entity
@Vetoed
public class Apartment implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modApartment")
    @TableGenerator(name = "modApartment", initialValue = 1001)
    private Long id;
    
    @NotNull(groups = {Condition.class}, message="Der Name der WG darf nicht leer sein!")
    @Size(groups = {Condition.class}, min=3, max=50, message="Der Name der WG muss zwischen 3 und 50 Zeichen liegen!")
    @Pattern(groups = {Condition.class}, regexp = "^[0-9A-Za-zäÄöÖüÜß\\-\\.\\s]+$", message="Der Name der WG enthält ungültige Bezeichner!")
    private String name;
    
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    public Apartment() { }
    
    public Apartment(String name) {
        this.name = name;
    }
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Apartment{" + "id=" + id + ", name=" + name + '}';
    }
    
}
