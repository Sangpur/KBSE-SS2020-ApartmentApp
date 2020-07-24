/*
 * ENTITY CLASS Member
 *
 */
package de.hsos.kbse.app.entity.member;

import de.hsos.kbse.app.enums.MemberColor;
import de.hsos.kbse.app.enums.MemberRole;
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

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@Entity(name="Members")
@Vetoed
public class Member implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modMember")
    @TableGenerator(name = "modMember", initialValue = 4)
    private Long id;
    
    private String name;
    
    @Enumerated(EnumType.STRING)
    private MemberRole memberRole;
    
    private String password;
    
    @OneToOne(cascade = CascadeType.ALL)
    private MemberDetail details;
    
    @Column(name="apartment_id")
    private Long apartmentID;

    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    public Member() {
        this.details = new MemberDetail();
    }
    
    public Member(String name, MemberRole memberRole, String password, Date birthday, MemberColor color) {
        this.name = name;
        this.memberRole = memberRole;
        this.password = password;
        
        this.details = new MemberDetail();
        this.details.setBirthday(birthday);
        this.details.setColor(color);
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

    public MemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(MemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MemberDetail getDetails() {
        return details;
    }

    public void setDetails(MemberDetail details) {
        this.details = details;
    }
    
    public Long getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(Long apartmentID) {
        this.apartmentID = apartmentID;
    }

    @Override
    public String toString() {
        return "Member{" + "id=" + id + ", name=" + name + ", memberRole=" + memberRole + ", password=" + password + ", details=" + details + ", apartmentID=" + apartmentID + '}';
    }

    
}
