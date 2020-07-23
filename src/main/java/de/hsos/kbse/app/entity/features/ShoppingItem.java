/*
 * ENTITY CLASS ShoppingItem
 *
 */
package de.hsos.kbse.app.entity.features;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Annika Limbrock
 */
@Entity
@Vetoed
public class ShoppingItem implements Serializable, Comparable<ShoppingItem>  {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modShoppingItem")
    @TableGenerator(name = "modShoppingItem", initialValue = 8)
    private Long id;
    
    private String name;
    
    private int amount;
    
    @Temporal(TemporalType.DATE)
    @Column(name="day")
    private Date date;
    
    private boolean checked;   // boolean fuer ausstehend (= false) oder erledigt (= true)
    
    @Temporal(TemporalType.DATE)
    @Column(name="checkday")
    private Date checkdate;     // Datum, an dem Artikel als "erledigt" markiert wurde
    
    @Column(name="apartment_id")
    private Long apartmentID;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    @Override
    public int compareTo(ShoppingItem s) {
        if(this.name == null || s.getName() == null) {
            return 0;
        } else if(this.checked && !s.isChecked()) { // Item 1 ist checked, Item 2 ist unchecked
            return 1;
        } else if(!this.checked && s.isChecked()) { // Item 1 ist unchecked, Item 2 ist checked
            return -1;
        }
        return this.name.compareTo(s.getName());    // Beide Items haben den gleichen Status
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
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

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        if(this.checked) {
            this.checkdate = java.sql.Date.valueOf(LocalDate.now());
        } else {
            this.checkdate = null;
        }
    }

    public Date getCheckdate() {
        return checkdate;
    }

    public Long getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(Long apartmentID) {
        this.apartmentID = apartmentID;
    }
    
}
