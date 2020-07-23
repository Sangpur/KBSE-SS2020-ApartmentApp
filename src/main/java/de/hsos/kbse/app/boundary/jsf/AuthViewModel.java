/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hsos.kbse.app.boundary.jsf;

import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.LogLevel;
import de.hsos.kbse.app.util.AppException;
import de.hsos.kbse.app.util.Logable;
import java.io.Serializable;
import java.util.Date;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Lucca
 */
@Named("authVM")
@ConversationScoped
public class AuthViewModel implements Serializable {
    
    private final MemberRepository memberRepository;
    private Member loggedInMember;
    
    /* Login Form fields */
    private String username;
    private String password;
    
    /* Register Form fields */
    private String repassword;
    private String apartmentName;
    private String birthday;
    private String color;
    
    /* Conversation */
    private final Conversation conversation;
    
    @Inject
    @Logable(LogLevel.INFO)
    public AuthViewModel(MemberRepository memberRepository, Conversation conversation) {
        this.memberRepository = memberRepository;
        this.conversation = conversation;
    }
    
    @Logable(LogLevel.INFO)
    public void initConversation() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
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
        
        return "faces/login?faces-redirect=true";
    }
    
    public void logout() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        
        session.setAttribute("userID", null);
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

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
