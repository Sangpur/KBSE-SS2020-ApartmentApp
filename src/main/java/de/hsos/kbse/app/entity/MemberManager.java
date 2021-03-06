/*
 * INTERFACE MemberManager
 *
 */
package de.hsos.kbse.app.entity;

import de.hsos.kbse.app.util.AppException;
import java.util.List;

/**
 *
 * @author Annika Limbrock, Lucca Oberhößel
 */
public interface MemberManager {
    
    public void createMember(Member member) throws AppException;
    public void deleteMember(Member member) throws AppException;
    public void deleteAllMembersFrom(Long apartmentID) throws AppException;
    public Member updateMember(Member member) throws AppException;
    public Member findMember(Long id) throws AppException;
    public Member findMemberByName(Long apartmentID, String name) throws AppException;
    public List<Member> getAllMembersFrom(Long apartmentID) throws AppException;
    public List<Member> getActiveMembersFrom(Long apartmentID) throws AppException;
    
}
