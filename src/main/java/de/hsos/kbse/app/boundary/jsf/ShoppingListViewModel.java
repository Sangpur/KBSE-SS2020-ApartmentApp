/*
 * BOUNDARY CLASS ShoppingListViewModel
 *
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.ShoppingList;
import de.hsos.kbse.app.entity.features.ShoppingItem;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.enums.ValidationGroup;
import de.hsos.kbse.app.util.Condition;
import de.hsos.kbse.app.util.General;
import de.hsos.kbse.app.util.Logable;
import java.io.Serializable;
import java.util.Iterator;
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
@Named("shoppingVM")
@ConversationScoped
public class ShoppingListViewModel implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    /* Controller Class */
    private final ShoppingList shoppinglist;
    
    /* Injected Conversation */
    private final Conversation conversation;
    
    /* Bean Validation API */
    private static Validator validator;
    
    private Member loggedInMember;
    private ShoppingItem currentItem;
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Inject
    @Logable(LogLevel.INFO)
    public ShoppingListViewModel(ShoppingList shoppinglist, Conversation conversation) {
        this.shoppinglist = shoppinglist;
        this.conversation = conversation;
    }
    
    public String addItem() {
        System.out.println("addItem()");
        this.currentItem = new ShoppingItem();
        return "shoppinglist-add";
    }
    
    /* ------------------------------------- METHODEN PRIVATE ------------------------------------- */
    
    @PostConstruct
    private static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Logable(LogLevel.INFO)
    private boolean validateInput(ValidationGroup group) {
        /* Die Methode validate() gibt ein Set von ConstraintViolations zurueck, in dem alle moeglicherweise begangenen Verstoesse aufgefuehrt
         * sind. Dieses Set ist leer, falls die Eingabe gueltig ist. Durch die Gruppierung der Constraints, hier General.class, besteht die
         * Moeglichkeit, die Validation erst dann auszufuehren, wenn die entsprechende Gruppe direkt durch das Programm angesprochen wird.
         * s. https://www.baeldung.com/javax-validation-groups */
        Set<ConstraintViolation<ShoppingListViewModel>> constraintViolations = null;
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
            Iterator<ConstraintViolation<ShoppingListViewModel>> iter = constraintViolations.iterator();
            while (iter.hasNext()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Constraint.", iter.next().getMessage());
                facesContext.addMessage("Constraint Violation",msg);
            }
            return false;
        }
    }
     
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */
    
    public void setLoggedInMember(Member loggedInMember) {
        this.loggedInMember = loggedInMember;
    }
    
}
