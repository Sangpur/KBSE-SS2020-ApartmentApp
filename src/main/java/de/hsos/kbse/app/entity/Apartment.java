/*
 * ENTITY CLASS Apartment
 *
 */
package de.hsos.kbse.app.entity;

import de.hsos.kbse.app.entity.member.Member;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.inject.Vetoed;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@Entity
@Vetoed
public class Apartment implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modApartment")
    @TableGenerator(name = "modApartment", initialValue = 1001)
    private Long id;
    
    private String name;
    
    //@OneToMany(cascade = CascadeType.PERSIST, orphanRemoval = true)
    //private List<Member> members;
    
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    public Apartment() {
        //this.members = new ArrayList();
    }
    
    public Apartment(String name) {
        this.name = name;
    }
    
    /* -------------------------------------- PRIVATE METHODS -------------------------------------- */
    
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

    /*public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }*/
    
}
