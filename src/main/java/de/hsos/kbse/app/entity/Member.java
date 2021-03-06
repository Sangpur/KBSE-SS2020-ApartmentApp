/*
 * ENTITY CLASS Member
 *
 */
package de.hsos.kbse.app.entity;

import de.hsos.kbse.app.enums.MemberColor;
import de.hsos.kbse.app.enums.MemberRole;
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
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel
 */
@Entity(name="Members")
@Vetoed
public class Member implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "modMember")
    @TableGenerator(name = "modMember", initialValue = 5)
    private Long id;
    
    @NotNull(groups = {General.class, Condition.class}, message="Der Name darf nicht leer sein!")
    @Size(groups = {General.class, Condition.class}, min=3, max=50, message="Der Name muss zwischen 3 und 50 Zeichen liegen!")
    @Pattern(groups = {General.class, Condition.class}, regexp = "^[0-9A-Za-zäÄöÖüÜß\\-\\.\\s]+$", message="Der Name enthält ungültige Bezeichner!")
    private String name;
    
    @Enumerated(EnumType.STRING)
    private MemberRole memberRole;
    
    @NotNull(groups = {General.class, Condition.class}, message="Das Passwort darf nicht leer sein!")
    @Size(groups = {General.class, Condition.class}, min=3, max=50, message="Das Passwort muss zwischen 3 und 50 Zeichen liegen!")
    private String password;
    
    @Transient
    @NotNull(groups = {Condition.class}, message="Die Passwortwiederholung darf nicht leer sein!")
    private String repassword;
    
    @OneToOne(cascade = CascadeType.ALL)
    @Valid
    private MemberDetail details;
    
    @Column(name="apartment_id")
    private Long apartmentID;
    
    private boolean deleted;

    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */
    
    public Member() {
        this.details = new MemberDetail();
        this.deleted = false;
    }
    
    public Member(Member obj) {
        this.name = obj.getName();
        this.memberRole = obj.getMemberRole();
        this.password = obj.getPassword();
        this.repassword = obj.getRepassword();
        this.details = new MemberDetail();
        this.details.setBirthday(obj.getDetails().getBirthday());
        this.details.setCashBalance(obj.getDetails().getCashBalance());
        this.details.setColor(obj.getDetails().getColor());
        this.apartmentID = obj.getApartmentID();
        this.deleted = obj.getDeleted();
    }
    
    public Member(MemberRole role, Long apartmentID) {
        this.memberRole = role;
        this.details = new MemberDetail();
        this.apartmentID = apartmentID;
        this.deleted = false;
    }
    
    public Member(String name, MemberRole memberRole, String password, Date birthday, MemberColor color) {
        this.name = name;
        this.memberRole = memberRole;
        this.password = password;
        this.deleted = false;
        this.details = new MemberDetail();
        this.details.setBirthday(birthday);
        this.details.setColor(color);
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

    public String getRepassword() {
        return repassword;
    }

    public void setRepassword(String repassword) {
        this.repassword = repassword;
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
    
    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Member{" + "id=" + id + ", name=" + name + ", memberRole=" + memberRole + ", password=" + password + ", details=" + details + ", apartmentID=" + apartmentID + '}';
    }
    
}
