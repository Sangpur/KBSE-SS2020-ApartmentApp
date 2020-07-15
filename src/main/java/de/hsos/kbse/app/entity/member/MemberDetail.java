/*
 * ENTITY CLASS MemberDetail
 *
 */
package de.hsos.kbse.app.entity.member;

import de.hsos.kbse.app.enums.MemberColor;
import java.io.Serializable;
import java.util.Date;
import javax.enterprise.inject.Vetoed;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

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
    @TableGenerator(name = "modMemberDetail", initialValue = 4)
    private Long id;
    
    @Temporal(TemporalType.DATE)
    private Date birthday;
    
    @Enumerated(EnumType.STRING)
    private MemberColor color;
    
    private float cashBalance;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    /* -------------------------------------- PRIVATE METHODS -------------------------------------- */
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public MemberColor getColor() {
        return color;
    }

    public void setColor(MemberColor color) {
        this.color = color;
    }

    public float getCashBalance() {
        return cashBalance;
    }

}
