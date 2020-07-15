/*
 * BOUNDARY CLASS CashFlowViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.CashFlow;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.entity.features.Payment;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.AppException;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import de.hsos.kbse.app.util.Logable;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel, Christoph Weigandt
 */
@Named("cashflowVM")
@ConversationScoped
public class CashFlowViewModel implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    /* Controller Classes */
    private final CashFlow cashflow;
    private final MemberRepository memberRepository;
    
    /* Injected Conversation */
    private final Conversation conversation;
    
    /* Bean Validation API */
    private static Validator validator;
    
    private Long apartmentID = 1000L; // TODO: Hinzufuegen sobald Scope gestartet wird bei Login-Prozess
    private List<Payment> payments;
    private List<Member> members;
    private Member loggedInMember;
    private boolean balance = false;        // true = positive, false = negative
    private Payment newPayment;
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public CashFlowViewModel(CashFlow cashflow, MemberRepository memberRepository, Conversation conversation) {
        this.cashflow = cashflow;
        this.memberRepository = memberRepository;
        this.conversation = conversation;
        this.payments = new LinkedList(); // Hier kann das neuste Element an Position 0 eingefuegt werden
        this.initPaymentsList();
        this.initMemberList();
        this.newPayment = new Payment();
    }
    
    @PostConstruct
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    public boolean checkBalance(float sum) {
        return sum >= 0;
    }
    
    public String savePayment() {
        System.out.println("savePayment()");
        System.out.println("Description: " + newPayment.getDescription());
        for(int i=0; i < newPayment.getInvolvedMembers().size(); i++) {
            System.out.println("InvolvedMember " + i + ": "+ newPayment.getInvolvedMembers().get(i).getName());
        }
        System.out.println("Sum: " + newPayment.getSum() + " €");
        return "cashflow";
    }
    
    public String discardPayment() {
        System.out.println("discardPayment()");
        this.newPayment.setDescription("");
        this.newPayment.getInvolvedMembers().clear();
        this.newPayment.setSum(0);
        return "cashflow";
    }
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
     private void initPaymentsList() {
        try {
            this.payments = this.cashflow.getAllPaymentsFrom(apartmentID);
            /* Absteigende Sortierung der Zahlungen anhand des Datums */
            Collections.sort(this.payments);
            Collections.reverse(this.payments);
        } catch(AppException ex) {
            String msg = ex.getMessage();
            FacesContext.getCurrentInstance().addMessage("Error", new FacesMessage(msg));
        }
    }
     
    private void initMemberList() {
        try {
            this.members = this.memberRepository.getAllMembersFrom(apartmentID);
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
        Set<ConstraintViolation<CashFlowViewModel>> constraintViolations = null;
        if(group == ValidationGroup.GENERAL) {
            // Moeglichkeit 1 -> diese Klasse selbst validieren
            constraintViolations = validator.validate( this, General.class );
            // Moeglichkeit 1 -> Sub-Klasse validieren
            //constraintViolations = validator.validate( this.subklasse, General.class );
        } else if(group == ValidationGroup.CONDITION) {
            constraintViolations = validator.validate( this, Condition.class );
        }

        if(constraintViolations != null && constraintViolations.isEmpty()) {
            /* Gueltige Eingabe */
            return true;
        } else {
            /* Ungueltige Eingabe */
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Iterator<ConstraintViolation<CashFlowViewModel>> iter = constraintViolations.iterator();
            while (iter.hasNext()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                facesContext.addMessage("Constraint Violation",msg);
            }
            return false;
        }
    }
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */

    public void setApartmentID(Long apartmentID) {
        this.apartmentID = apartmentID;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setLoggedInMember(Member loggedInMember) {
        this.loggedInMember = loggedInMember;
    }

    public void setBalance(boolean balance) {
        this.balance = balance;
    }

    public boolean isBalance() {
        return balance;
    }

    public Payment getNewPayment() {
        return newPayment;
    }

    public void setNewPayment(Payment newPayment) {
        this.newPayment = newPayment;
    }
    
    
    
    
    
    public void test() {
        System.out.println("Member Anzahl: " + this.members.size());
        System.out.println("Login Name:" + this.loggedInMember.getName());
    }
}
