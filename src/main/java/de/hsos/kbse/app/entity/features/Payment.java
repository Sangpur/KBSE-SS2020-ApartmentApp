/*
 * ENTITY CLASS Payment
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.entity.Member;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import javax.enterprise.inject.Vetoed;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 *
 * @author Annika Limbrock
 */
@Entity
@Vetoed
public class Payment implements Serializable, Comparable<Payment> {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modPayment")
    @TableGenerator(name = "modPayment", initialValue = 4)
    private Long id;
    
    @Column(name="amount", precision = 8, scale = 2)
    @NotNull(groups = {General.class, Condition.class}, message="Der Betrag darf nicht leer sein!")
    @DecimalMin(groups = {General.class, Condition.class}, value="0.01", message="Der Betrag muss größer als 0.00 € sein!")
    private BigDecimal sum;
    
    @NotNull(groups = {General.class, Condition.class}, message="Die Beschreibung darf nicht leer sein!")
    @Size(groups = {General.class, Condition.class}, min=3, max=50, message="Die Beschreibung muss zwischen 3 und 50 Zeichen liegen!")
    @Pattern(groups = {General.class, Condition.class}, regexp = "^[0-9A-Za-zäÄöÖüÜß\\-\\.\\s]+$", message="Die Beschreibung enthält ungültige Bezeichner!")
    private String description;
    
    @OneToOne(cascade = CascadeType.MERGE)
    private Member giver;
    
    @OneToMany(cascade = CascadeType.MERGE)
    @Size(groups = {General.class, Condition.class}, min=1, message="Es muss mindestens ein Mitglied ausgewählt werden, für das bezahlt wurde!")
    private List<Member> involvedMembers;
    
    @Temporal(TemporalType.DATE)
    @Column(name="day")
    private Date date;
    
    @Column(name="apartment_id")
    private Long apartmentID;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    public Payment() {}
    
    public Payment(Member giver, Date date, Long apartmentID) {
        this.giver = giver;
        this.involvedMembers = new ArrayList();
        this.date = date;
        this.apartmentID = apartmentID;
    }
    
    public Payment(Payment obj) {
        this.sum = obj.getSum();
        this.description = obj.getDescription();
        this.giver = obj.getGiver();
        this.involvedMembers = obj.getInvolvedMembers();
        this.date = obj.getDate();
        this.apartmentID = obj.getApartmentID();
    }
    
    @Override
    public int compareTo(Payment p) {
      if (this.date == null || p.getDate() == null)
        return 0;
      return this.date.compareTo(p.getDate());
    }
    
    /* -------------------------------------- PRIVATE METHODS -------------------------------------- */
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Member getGiver() {
        return giver;
    }

    public void setGiver(Member giver) {
        this.giver = giver;
    }

    public List<Member> getInvolvedMembers() {
        return involvedMembers;
    }
    
    public String getInvolvedMembersFormat() {
        String str = "";
        ListIterator<Member> it = this.involvedMembers.listIterator(); 
        while (it.hasNext()) { 
            str += it.next().getName();
            if(it.hasNext())
                str += ", ";
        }
        return str;
    }

    public void setInvolvedMembers(List<Member> involvedMembers) {
        this.involvedMembers = involvedMembers;
    }

    public Date getDate() {
        return date;
    }
    
    public String getDateFormat() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");  
        String strDate = formatter.format(this.date);  
        return strDate;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(Long apartmentID) {
        this.apartmentID = apartmentID;
    }
    
}
