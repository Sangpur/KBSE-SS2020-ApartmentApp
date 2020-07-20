/*
 * ENTITY CLASS Note
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.NoteCategory;
import java.io.Serializable;
import java.util.Date;
import javax.enterprise.inject.Vetoed;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@Entity
@Vetoed
public class Note implements Serializable, Comparable<Note> {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modNote")
    @TableGenerator(name = "modNote", initialValue = 7)
    private Long id;
    
    private String message;
    
    @OneToOne(cascade = CascadeType.MERGE)
    private Member author;
    
    @Enumerated(EnumType.STRING)
    private NoteCategory category;    // INFO (Info), TODO (ToDo), URGENT (Dringend)
    
    @Column(name="datetime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    
    @Column(name="apartment_id")
    private Long apartmentID;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    @Override
    public int compareTo(Note n) {
        System.out.println("compareTo()");
        
        if (this.timestamp == null || n.getTimestamp()== null ){
            return 0;
        }else if(this.category.equals(NoteCategory.URGENT) && n.getCategory().equals(NoteCategory.URGENT)){
          return this.timestamp.compareTo(n.getTimestamp());
        }else if(this.category.equals(NoteCategory.URGENT) && !n.getCategory().equals(NoteCategory.URGENT)){
            return 1;
        }else if(!this.category.equals(NoteCategory.URGENT) && n.getCategory().equals(NoteCategory.URGENT)){
          return -1;
        }
        return this.timestamp.compareTo(n.getTimestamp());
    }
    
    /* -------------------------------------- PRIVATE METHODS -------------------------------------- */
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Member getAuthor() {
        return author;
    }

    public void setAuthor(Member author) {
        this.author = author;
    }

    public NoteCategory getCategory() {
        return category;
    }

    public void setCategory(NoteCategory category) {
        this.category = category;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Long getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(Long apartmentID) {
        this.apartmentID = apartmentID;
    }

}
