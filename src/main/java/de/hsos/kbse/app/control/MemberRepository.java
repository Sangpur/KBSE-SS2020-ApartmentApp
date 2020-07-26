/*
 * CONTROLLER CLASS MemberRepository
 *
 */
package de.hsos.kbse.app.control;

import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.entity.member.MemberDetail;
import de.hsos.kbse.app.entity.member.MemberManager;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

/**
 *
 * @author Annika Limbrock
 */
@RequestScoped
@Transactional
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
            member = this.em.merge(member);           // Rueckgabe ist null, falls Mitglied nicht existiert
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
            member = this.em.find(Member.class, id);  // Rueckgabe ist null, falls Mitglied nicht existiert
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Mitglied konnte nicht gefunden werden!");
        }
        return member;
    }
    
    // Does not throw Exception because it is expected that users won't be found during the login process
    @Override
    public Member findMemberByName(String name) throws AppException {
        Member member = null;
        try {
            String str = "SELECT m FROM Members m WHERE m.name = :name";
            TypedQuery<Member> querySelect = em.createQuery(str, Member.class);
            querySelect.setParameter("name", name);
            member = querySelect.getSingleResult();
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
    
    @Override
    public void deleteAllMembersFrom(Long apartmentID) throws AppException {
        List<Member> members;
        List<Long> detailIds = new ArrayList();
        try {
            String str = "SELECT m FROM Members m WHERE m.apartmentID = :id";
            TypedQuery<Member> querySelect = em.createQuery(str, Member.class);
            querySelect.setParameter("id", apartmentID);
            members = querySelect.getResultList();
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Mitglieder der WG "+ apartmentID +" konnten nicht gefunden werden!");
        }
        for(int i = 0; i < members.size(); i++){
            detailIds.add(members.get(i).getDetails().getId());
        }
        try {
            String str = "DELETE FROM Members m WHERE m.apartmentID = :id";
            TypedQuery<Member> querySelect = em.createQuery(str, Member.class);
            querySelect.setParameter("id", apartmentID);
            querySelect.executeUpdate();
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Alle Member der WG "+ apartmentID +" konnten nicht gelöscht werden!");
        }
        for(int i = 0; i < detailIds.size(); i++){
            try {
            String str = "DELETE FROM MemberDetail m WHERE m.id = :id";
            TypedQuery<MemberDetail> querySelect = em.createQuery(str, MemberDetail.class);
            querySelect.setParameter("id", detailIds.get(i));
            querySelect.executeUpdate();
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("MemberDetails mit der id " + detailIds.get(i) + " der WG "+ apartmentID +" konnten nicht gelöscht werden!");
        }
        }
    }
}
