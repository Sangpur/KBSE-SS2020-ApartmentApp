/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.ApartmentRepository;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.features.Payment;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.entity.member.MemberDetail;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.MemberColor;
import de.hsos.kbse.app.enums.MemberRole;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.AppException;
import de.hsos.kbse.app.util.General;
import de.hsos.kbse.app.util.Logable;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 *
 * @author Lucca
 */
@Named("authVM")
@ConversationScoped
public class AuthViewModel implements Serializable {
    
    private final MemberRepository memberRepository;
    private final ApartmentRepository apartmentRepository;
    private Member loggedInMember;
    
    /* Bean Validation API */
    private static Validator validator;
    
    /* Login Form fields */
    private String username;
    private String password;
    
    /* Register Form fields */
    private String repassword;
    private String apartmentName;
    private Date birthday;
    private String color;
    
    /* Conversation */
    private final Conversation conversation;
    
    @Inject
    @Logable(LogLevel.INFO)
    public AuthViewModel(MemberRepository memberRepository, Conversation conversation, ApartmentRepository apartmentRepository) {
        this.memberRepository = memberRepository;
        this.apartmentRepository = apartmentRepository;
        this.conversation = conversation;
    }
    
    @Logable(LogLevel.INFO)
    public void initConversation() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }
    
    @Logable(LogLevel.INFO)
    public void endConversation(){
        if(!conversation.isTransient()) {
            conversation.end();
        }
    }
    
    @PostConstruct
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    public String login() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Member user = null;
        try {
            user = memberRepository.findMemberByName(this.username);
        } catch(AppException ex) {
            String msg = ex.getMessage();
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage(msg));
        }
        if(user == null){
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Wrong username."));
        } else if(!user.getPassword().equals(this.password)){
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Wrong password."));
        } else {
            this.loggedInMember = user;
            session.setAttribute("user", user);
            return "pages/apartment?faces-redirect=true";
        }
        return "";
    }
    
    public String register() {
        if(password.equals(repassword)){
            Apartment a = null;
            if(this.apartmentName != null && this.apartmentName.length() >= 3 && this.apartmentName.length() <= 50){
                a = new Apartment(this.apartmentName);
            }
            if(a != null){
                Member m = new Member(username, MemberRole.ADMIN, password, birthday, MemberColor.RED);
                if(this.validateInput(ValidationGroup.GENERAL, m)) {   // Gueltige Eingabe
                    try {
                        apartmentRepository.createApartment(a);
                        m.setApartmentID(a.getId());
                        memberRepository.createMember(m);
                        FacesContext.getCurrentInstance().addMessage("Success", new FacesMessage("New apartment was registered."));
                        return "faces/login?faces-redirect=true";
                    } catch (AppException ex) {
                        FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Unable to register new apartment."));
                    }
                }
            } else {
                FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Apartment name must be between 3 and 50 characters long."));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Passwords are not identical."));
        }
        return "";
    }
    
    public void logout() throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
     
	session.invalidate();
        endConversation();
        
        facesContext.getExternalContext().redirect("/ApartmentApp/faces/login.xhtml");
    }

    public Member getLoggedInMember() {
        return loggedInMember;
    }

    public void setLoggedInMember(Member loggedInMember) {
        this.loggedInMember = loggedInMember;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getApartmentName() {
        return apartmentName;
    }

    public void setApartmentName(String apartmentName) {
        this.apartmentName = apartmentName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
    
    @Logable(LogLevel.INFO)
    private boolean validateInput(ValidationGroup group, Member member) {
        /* Die Methode validate() gibt ein Set von ConstraintViolations zurueck, in dem alle moeglicherweise begangenen Verstoesse aufgefuehrt
         * sind. Dieses Set ist leer, falls die Eingabe gueltig ist. Durch die Gruppierung der Constraints, hier General.class, besteht die
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird.
         * s. https://www.baeldung.com/javax-validation-groups */
        Set<ConstraintViolation<Member>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            constraintViolations = validator.validate(member, General.class );
        }

        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe */
            return validateInput(group, member.getDetails());
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<Member>> iter = constraintViolations.iterator();
            while (iter.hasNext()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                facesContext.addMessage("Constraint Violation",msg);
            }
            return false;
        }
    }
    
    @Logable(LogLevel.INFO)
    private boolean validateInput(ValidationGroup group, MemberDetail detail) {
        /* Die Methode validate() gibt ein Set von ConstraintViolations zurueck, in dem alle moeglicherweise begangenen Verstoesse aufgefuehrt
         * sind. Dieses Set ist leer, falls die Eingabe gueltig ist. Durch die Gruppierung der Constraints, hier General.class, besteht die
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird.
         * s. https://www.baeldung.com/javax-validation-groups */
        Set<ConstraintViolation<MemberDetail>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            constraintViolations = validator.validate(detail, General.class );
        }

        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe */
            return true;
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<MemberDetail>> iter = constraintViolations.iterator();
            while (iter.hasNext()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                facesContext.addMessage("Constraint Violation",msg);
            }
            return false;
        }
    }
}
