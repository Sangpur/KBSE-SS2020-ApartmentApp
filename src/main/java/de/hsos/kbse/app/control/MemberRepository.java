/*
 * CONTROLLER CLASS MemberRepository
 *
 */
package de.hsos.kbse.app.control;

import de.hsos.kbse.app.entity.Member;
import de.hsos.kbse.app.entity.MemberDetail;
import de.hsos.kbse.app.entity.MemberManager;
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
 * @author Annika Limbrock, Lucca Oberhößel
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
    
    @Override
    public Member findMemberByName(Long apartmentID, String name) throws AppException {
        Member member = null;
        try {
            String str = "SELECT m FROM Members m WHERE m.apartmentID = :id AND m.name = :name AND m.deleted = :deleted";
            TypedQuery<Member> querySelect = em.createQuery(str, Member.class);
            querySelect.setParameter("id", apartmentID);
            querySelect.setParameter("name", name);
            querySelect.setParameter("deleted", false);
            /* Es ist grundsaetzlich sinnvoll, auch bei nur einem erwarteten Ergebnis eine ResultList zurueckgeben zu lassen,
             * da die Methode getSingleResult() eine Exception ausloest, falls der gesuchte Eintrag nicht existiert, die
             * hier nicht gewuenscht ist, da kein Hinweis darauf gegeben werden soll, wenn ein User nicht existiert. */
            List<Member> result = querySelect.getResultList();
            if (result == null || result.isEmpty()) {
                return null;
            } else if (result.size() == 1) {
                return result.get(0);
            }
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
    public List<Member> getActiveMembersFrom(Long apartmentID) throws AppException {
        try {
            String str = "SELECT m FROM Members m WHERE m.apartmentID = :id AND m.deleted = :deleted";
            TypedQuery<Member> querySelect = em.createQuery(str, Member.class);
            querySelect.setParameter("id", apartmentID);
            querySelect.setParameter("deleted", false);
            List<Member> results = querySelect.getResultList();
            return results;
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Mitglieder der WG "+ apartmentID +" konnten nicht gefunden werden!");
        }
    }
    
    @Override
    public void deleteAllMembersFrom(Long apartmentID) throws AppException {
        List<Member> members = this.getAllMembersFrom(apartmentID);
        List<Long> listMemberDetails = new ArrayList();
        /* Loeschen aller zur WG gehoerigen Mitglieder */
        try {
            String str = "DELETE FROM Members m WHERE m.apartmentID = :id";
            TypedQuery<Member> querySelect = em.createQuery(str, Member.class);
            querySelect.setParameter("id", apartmentID);
            querySelect.executeUpdate();
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new AppException("Die Mitglieder der WG "+ apartmentID +" konnten nicht gelöscht werden!");
        }
        /* Loeschen der zugehoerigen MemberDetail-Eintraege in der Datenbank */
        for(int i = 0; i < members.size(); i++){
            listMemberDetails.add(members.get(i).getDetails().getId());
        }
        for(int i = 0; i < listMemberDetails.size(); i++){
            try {
                String str = "DELETE FROM MemberDetail m WHERE m.id = :id";
                TypedQuery<MemberDetail> querySelect = em.createQuery(str, MemberDetail.class);
                querySelect.setParameter("id", listMemberDetails.get(i));
                querySelect.executeUpdate();
            } catch(Exception ex) {
                ex.printStackTrace();
                throw new AppException("Die Details des Mitglieds mit der ID " + listMemberDetails.get(i) + " konnten nicht gelöscht werden!");
            }
        }
    }
}
