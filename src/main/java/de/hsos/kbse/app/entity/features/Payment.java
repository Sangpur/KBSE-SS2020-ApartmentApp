/*
 * ENTITY CLASS Payment
 *
 */
package de.hsos.kbse.app.entity.features;

import de.hsos.kbse.app.entity.member.Member;
import java.io.Serializable;
import java.text.SimpleDateFormat;
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

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@Entity
@Vetoed
public class Payment implements Serializable, Comparable<Payment> {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modPayment")
    @TableGenerator(name = "modPayment", initialValue = 4)
    private Long id;
    
    @Column(name="amount")
    private float sum;
    
    private String description;
    
    @OneToOne(cascade = CascadeType.MERGE)
    private Member giver;
    
    @OneToMany(cascade = CascadeType.MERGE)
    private List<Member> involvedMembers;
    
    @Temporal(TemporalType.DATE)
    @Column(name="day")
    private Date date;
    
    private boolean repayment;  // boolean fuer Zahlung (= false) oder Rueckzahlung (= true)
    
    @Column(name="apartment_id")
    private Long apartmentID;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
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
    
    public float getSum() {
        return sum;
    }

    public void setSum(float sum) {
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

    public boolean isRepayment() {
        return repayment;
    }

    public void setRepayment(boolean repayment) {
        this.repayment = repayment;
    }

    public Long getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(Long apartmentID) {
        this.apartmentID = apartmentID;
    }
    
}
