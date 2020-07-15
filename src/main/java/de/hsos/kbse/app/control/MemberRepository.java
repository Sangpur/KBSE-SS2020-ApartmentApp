/*
 * CONTROLLER CLASS MemberRepository
 *
 */
package de.hsos.kbse.app.control;

import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.entity.member.MemberManager;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author Annika Limbrock
 */
public class MemberRepository implements MemberManager, Serializable {
    
    /* ----------------------------------------- ATTRIBUTES ---------------------------------------- */
    
    @PersistenceContext(name = "ApartmentPU")
    private EntityManager em;
    
    /* --------------------------------------- PUBLIC METHODS -------------------------------------- */

    @Override
    public void createMember(Member member) throws AppException {
        try {
            this.em.persist(member);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Mitglied konnte nicht angelegt werden!");
        }
    }

    @Override
    public void deleteMember(Member member) throws AppException {
        try {
            Member toMerge = this.em.merge(member);
            this.em.remove(toMerge);
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Mitglied konnte nicht geloescht werden!");
        }
    }

    @Override
    public Member updateMember(Member member) throws AppException {
        try {
            member = this.em.merge(member);           // Rueckgabe ist null, falls Zahlung nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Mitglied konnte nicht angepasst werden!");
        }
        return member;
    }

    @Override
    public Member findMember(Long id) throws AppException {
        Member member = null;
        try {
            member = this.em.find(Member.class, id);  // Rueckgabe ist null, falls Zahlung nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Mitglied konnte nicht gefunden werden!");
        }
        return member;
    }

    @Override
    public List<Member> getAllMembersFrom(Long apartmentID) throws AppException {
        try {
            String str = "SELECT m FROM Members m WHERE m.apartmentID = :id";
            TypedQuery<Member> querySelect = em.createQuery(str, Member.class);
            querySelect.setParameter("id", apartmentID);
            List<Member> results = querySelect.getResultList();
            return results;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Mitglieder der WG "+ apartmentID +" konnten nicht gefunden werden!");
        }
    }
    
    /* -------------------------------------- PRIVATE METHODS -------------------------------------- */
    
    /* -------------------------------------- GETTER AND SETTER ------------------------------------ */
    
}