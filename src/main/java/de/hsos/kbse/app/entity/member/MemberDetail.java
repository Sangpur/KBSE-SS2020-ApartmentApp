/*
 * ENTITY CLASS MemberDetail
 *
 */
package de.hsos.kbse.app.entity.member;

import de.hsos.kbse.app.enums.MemberColor;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@Entity
@Vetoed
public class MemberDetail implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modMemberDetail")
    @TableGenerator(name = "modMemberDetail", initialValue = 5)
    private Long id;
    
    @NotNull(groups = {Condition.class}, message="Das Geburtsdatum darf nicht leer sein!")
    @Past(groups = {Condition.class}, message="Das Geburtsdatum muss in der Vergangenheit liegen!")
    @Temporal(TemporalType.DATE)
    private Date birthday;
    
    @Enumerated(EnumType.STRING)
    private MemberColor color;
    
    @Column(precision = 8, scale = 2)
    private BigDecimal cashBalance;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    public void addCashBalance(BigDecimal value) {
        this.cashBalance = this.cashBalance.add(value);
    }
    
    public void subtractCashBalance(BigDecimal value) {
        this.cashBalance = this.cashBalance.subtract(value);
    }
    
    /* -------------------------------------- PRIVATE METHODS -------------------------------------- */
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }
    
    public String getBirthdayFormat() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");  
        String strDate = formatter.format(this.birthday);  
        return strDate;
    }

    public MemberColor getColor() {
        return color;
    }

    public void setColor(MemberColor color) {
        this.color = color;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

}
