/*
 * BOUNDARY CLASS ApartmentViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.ApartmentRepository;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.entity.member.MemberDetail;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.MemberColor;
import de.hsos.kbse.app.enums.MemberRole;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.AppException;
import de.hsos.kbse.app.util.General;
import de.hsos.kbse.app.util.Logable;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
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
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@Named("apartmentVM")
@ConversationScoped
public class ApartmentViewModel implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    /* Controller Classes */
    private final ApartmentRepository repository;
    private final MemberRepository memberRepository;
    
    /* Conversation */
    private final Conversation conversation;
    
    /* Bean Validation API */
    private static Validator validator;
    
    private Apartment apartment;
    private List<Member> members;
    private Member currentMember = null;
    private String newpassword = "";
    private String repassword = "";
    private Boolean editMode = false;
    private MemberColor[] colors = {MemberColor.RED, MemberColor.GREEN, MemberColor.BLUE, MemberColor.YELLOW, MemberColor.PINK, MemberColor.ORANGE};
    private MemberRole[] roles = {MemberRole.ADMIN, MemberRole.USER};
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public ApartmentViewModel(ApartmentRepository repository, MemberRepository memberRepository, Conversation conversation) {
        this.repository = repository;
        this.memberRepository = memberRepository;
        this.conversation = conversation;
        this.initApartmentByID(getLoggedInMember().getApartmentID()); // TODO: Bei Login-Prozess entsprechende ID setzen
    }
    
    
    @PostConstruct
    public void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    public String goToAddMember() {
        this.currentMember = new Member();
        this.repassword = "";
        this.editMode = false;
        this.currentMember.setApartmentID(apartment.getId());
        this.currentMember.setMemberRole(MemberRole.USER);
        this.currentMember.getDetails().setColor(MemberColor.GREEN);
        return "members-add";
    }
    
    public String editMember(Long id) {
        this.currentMember = members.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
        if(currentMember == null){
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Member with id " + id + "could not be found."));
            return "";
        }
        this.newpassword = "";
        this.repassword = "";
        this.editMode = true;
        return "members-add";
    }
    
    public String deleteMember(Long id) {
        this.currentMember = members.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
        if(currentMember == null){
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Member with id " + id + "could not be found."));
            return "";
        }
        try {
            currentMember.setDeleted(true);
            this.memberRepository.updateMember(currentMember);
            initApartmentByID(apartment.getId());
            FacesContext.getCurrentInstance().addMessage("Succes", new FacesMessage("Member was deleted."));
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.getExternalContext().redirect("/ApartmentApp/faces/pages/members.xhtml");
        } catch (Exception e) {
            Logger.getLogger(ApartmentViewModel.class.getName()).log(Level.SEVERE, null, e);
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Member with id " + id + "could not be deleted."));
        }
        initApartmentByID(apartment.getId());
        return "";
    }
    
    public String saveMember() {
        if(!editMode){ // Add new Member to Apartment
            if(currentMember.getPassword() != null && currentMember.getPassword().equals(repassword)){
                if(this.validateInput(ValidationGroup.GENERAL)) { // Gueltige Eingabe
                    try {
                        currentMember.getDetails().setCashBalance(new BigDecimal(0));
                        this.memberRepository.createMember(currentMember);
                        initApartmentByID(apartment.getId());
                        FacesContext.getCurrentInstance().addMessage("Succes", new FacesMessage("Member was created."));
                        return resetMember();
                    } catch (AppException ex) {
                        Logger.getLogger(ApartmentViewModel.class.getName()).log(Level.SEVERE, null, ex);
                        FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Member could not be added to apartment."));
                    }
                }
            } else {
                FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Passwords are not identical."));
                return "";
            }
        } else { // Save existing Member
            if(newpassword != null && newpassword.length() > 0 ){
                if(newpassword.equals(repassword)){
                    currentMember.setPassword(newpassword);
                } else {
                FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Passwords are not identical."));
                return "";
                }
            }
            if(this.validateInput(ValidationGroup.GENERAL)) { // Gueltige Eingabe
                try {
                    this.memberRepository.updateMember(currentMember);
                    initApartmentByID(apartment.getId());
                    FacesContext.getCurrentInstance().addMessage("Succes", new FacesMessage("Member was updated."));
                    return resetMember();
                } catch (AppException ex) {
                    Logger.getLogger(ApartmentViewModel.class.getName()).log(Level.SEVERE, null, ex);
                    FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Member informations could not be updated."));
                }
            } 
        }
        return "";
    }
    
    public String resetMember() {
        this.currentMember = null;
        this.newpassword = "";
        this.repassword = "";
        this.editMode = false;
        return "members";
    }
    
    public Boolean isAllowedToChangeMember(Long id){
        if(getLoggedInMember().getMemberRole().equals(MemberRole.ADMIN))
            return true;
        this.currentMember = members.stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
        if(currentMember == null){
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage("Member with id " + id + "could not be found."));
            return false;
        }
        return currentMember.getId().equals(getLoggedInMember().getId());
    }
    
    public Boolean isAdmin(){
        return getLoggedInMember().getMemberRole().equals(MemberRole.ADMIN);
    }
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
    private void initApartmentByID(Long id) {
        try {
            this.apartment = this.repository.findApartment(id);
            this.members = this.memberRepository.getAllMembersFrom(id);
        } catch(AppException ex) {
            String msg = ex.getMessage();
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage(msg));
        }
    }
    
    @Logable(LogLevel.INFO)
    private boolean validateInput(ValidationGroup group) {
        /* Die Methode validate() gibt ein Set von ConstraintViolations zurueck, in dem alle moeglicherweise begangenen Verstoesse aufgefuehrt
         * sind. Dieses Set ist leer, falls die Eingabe gueltig ist. Durch die Gruppierung der Constraints, hier General.class, besteht die
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird.
         * s. https://www.baeldung.com/javax-validation-groups */
        Set<ConstraintViolation<Member>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            constraintViolations = validator.validate(currentMember, General.class );
        }

        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe for member constraints so now checking for member.details constaints */
            Set<ConstraintViolation<MemberDetail>> detailConstraintViolations = null;
            if(group == ValidationGroup.GENERAL) {
                detailConstraintViolations = validator.validate(currentMember.getDetails(), General.class );
            }

            if(detailConstraintViolations != null && detailConstraintViolations.isEmpty()) {
                /* Gueltige Eingabe */
                return true;
            } else {
                /* Ungueltige Eingabe */
                FacesContext facesContext = FacesContext.getCurrentInstance();
                Iterator<ConstraintViolation<MemberDetail>> iter = detailConstraintViolations.iterator();
                while (iter.hasNext()) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                    facesContext.addMessage("Constraint Violation",msg);
                }
                return false;
            }
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
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public List<Member> getMembers() {
        return members;
    }

    public Member getLoggedInMember() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

        Member loggedInMember = (Member) session.getAttribute("user");
        return loggedInMember;
    }

    public Member getCurrentMember() {
        return currentMember;
    }

    public void setCurrentMember(Member currentMember) {
        this.currentMember = currentMember;
    }

    public String getRepassword() {
        return repassword;
    }

    public void setRepassword(String repassword) {
        this.repassword = repassword;
    }

    public String getNewpassword() {
        return newpassword;
    }

    public void setNewpassword(String newpassword) {
        this.newpassword = newpassword;
    }

    public MemberColor[] getColors() {
        return colors;
    }

    public MemberRole[] getRoles() {
        return roles;
    }

    public Boolean getEditMode() {
        return editMode;
    }

    public void setEditMode(Boolean editMode) {
        this.editMode = editMode;
    }
}
