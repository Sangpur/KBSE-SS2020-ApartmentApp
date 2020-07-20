/*
 * ENTITY CLASS Note
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.NoteCategory;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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
    
    @NotNull(groups = {General.class, Condition.class}, message="Die Notiz darf nicht leer sein!")
    @Size(groups = {General.class, Condition.class}, min=3, max=120, message="Die Notiz muss zwischen 3 und 120 Zeichen liegen!")
    @Pattern(groups = {General.class, Condition.class}, regexp = "^[0-9A-Za-zäÄöÖüÜß\\-\\.\\?\\!\\#\\s]+$", message="Die Notiz enthält ungültige Bezeichner!")
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

    public Note(){
        
    }
    
    public Note(Member loggedInMember, Date date, Long apartmentID) {
        this.author = loggedInMember;
        this.timestamp = date;
        this.apartmentID = apartmentID;
    }

    public Note(Note note) {
        this.message = note.getMessage();
        this.author = note.getAuthor();
        this.category = note.getCategory();
        this.timestamp = note.getTimestamp();
        this.apartmentID = note.getApartmentID();
    }
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    @Override
    public int compareTo(Note n) {
        /* Sortiert die Notizen aufsteigend anhand des Datums und der Kategorie */        
        if (this.timestamp == null || n.getTimestamp()== null ){
            return 0;
        }
        else if(this.category.equals(NoteCategory.URGENT) && n.getCategory().equals(NoteCategory.URGENT)){
            return this.timestamp.compareTo(n.getTimestamp());
        }
        else if(this.category.equals(NoteCategory.URGENT) && !n.getCategory().equals(NoteCategory.URGENT)){
            return 1;
        }      
        else if(!this.category.equals(NoteCategory.URGENT) && n.getCategory().equals(NoteCategory.URGENT)){
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
