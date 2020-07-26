/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hsos.kbse.app.boundary.rs;

import de.hsos.kbse.app.boundary.rs.dto.MemberDTO;
import de.hsos.kbse.app.control.ApartmentRepository;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.Member;
import de.hsos.kbse.app.entity.MemberDetail;
import de.hsos.kbse.app.enums.MemberColor;
import de.hsos.kbse.app.enums.MemberRole;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Lucca
 */
@Stateless
@Path("apartments/{apartmentId}/members")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class MemberResource implements Serializable {
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    @Inject 
    ApartmentRepository apartmentRepo;
    
    @Inject 
    MemberRepository memberRepo;
    
    @Inject
    private Jsonb jsonb;
    
    @Context
    UriInfo uriInfo;
    
    private static final SimpleDateFormat DF = new SimpleDateFormat( "yyyy-MM-dd" );
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @GET
    public Response getAllApartments(@PathParam("apartmentId") String apartmentIdStr) {
        Long apartmentId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id is not a number").build();
        }
        List<Member> results = null;
        try {
            results = this.memberRepo.getActiveMembersFrom(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(MemberResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(results != null)
            return Response.ok(jsonb.toJson(results)).build();
        return Response.serverError().build();
    }
    
    @GET
    @Path("{id}")
    public Response getApartment(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr) {
        Long apartmentId;
        Long memberId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            memberId = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        Member result = null;
        try {
            result = this.memberRepo.findMember(memberId);
        } catch (AppException ex) {
            Logger.getLogger(MemberResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(result != null){
            if(result.getApartmentID().equals(apartmentId))
                return Response.ok(jsonb.toJson(result)).build();
        }
        return Response.ok(jsonb.toJson(new Object())).build(); // Returns empty object for wrong id 
    }
    
    @POST
    public Response createApartment(@PathParam("apartmentId") String apartmentIdStr, MemberDTO parameters) {
        Long apartmentId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id is not a number").build();
        }
        Apartment a = null;
        try {
            a = apartmentRepo.findApartment(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(MemberResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find apartment.").build();
        }
        if(a == null){
            return Response.status(400, "Bad Request: No apartment with this id found.").build(); //---------------------------------------------------------------
        }
        
        if(parameters.name != null && parameters.name.length() >= 3 
                && parameters.password != null && parameters.password.length() >= 3 
                && parameters.birthdate != null && parameters.birthdate.length() == 10){
            Date d;
            try {
                d = new Date(DF.parse(parameters.birthdate).getTime());
            } catch (ParseException ex) {
                Logger.getLogger(MemberResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(400, "Bad Request: birthdate format has to be yyyy-mm-dd").build();
            }
            MemberRole role;
            try {
                role = MemberRole.valueOf(parameters.role);
            } catch (IllegalArgumentException ex) {
                return Response.status(400, "Bad Request: role needs to be either ADMIN or USER.").build();
            }
            MemberColor color;
            try {
                color = MemberColor.valueOf(parameters.color);
            } catch (IllegalArgumentException ex) {
                return Response.status(400, "Bad Request: color needs to be one of these: RED, GREEN, BLUE, YELLOW, PINK, ORANGE.").build();
            }
            Member m = new Member(parameters.name, role, parameters.password, d, color);
            m.setApartmentID(a.getId());
            m.getDetails().setCashBalance(new BigDecimal(0));
            try {
                memberRepo.createMember(m);
            } catch (AppException ex) {
                Logger.getLogger(MemberResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(500, "Server Error: Failed while trying to create Member").build();
            }
            return Response.ok(jsonb.toJson(m)).build();
        } else {
            return Response.status(400, "Bad Request: Member could not be created. This Form is needed: {\n" +
                    "    name: ...,\n" +
                    "    password: ...,\n" +
                    "    birthdate: yyyy-mm-dd\n" +
                    "    role: ...,\n" +
                    "    color: ...,\n" +
                    "}").build();
        }
    }
    
    @PUT
    @Path("{id}")
    public Response updateApartment(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr, MemberDTO parameters) {
        Long apartmentId;
        Long memberId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            memberId = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        Member m = null;
        try {
            m = this.memberRepo.findMember(memberId);
        } catch (AppException ex) {
            Logger.getLogger(MemberResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(m == null)
            return Response.status(400, "Bad Request: No member found for this id").build();
        
        if(parameters.name != null && parameters.name.length() >= 3
                && parameters.birthdate != null && parameters.birthdate.length() == 10){
            Date d;
            try {
                d = new Date(DF.parse(parameters.birthdate).getTime());
            } catch (ParseException ex) {
                Logger.getLogger(MemberResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(400, "Bad Request: birthdate format has to be yyyy-mm-dd").build();
            }
            MemberRole role;
            try {
                role = MemberRole.valueOf(parameters.role);
            } catch (IllegalArgumentException ex) {
                return Response.status(400, "Bad Request: role needs to be either ADMIN or USER.").build();
            }
            MemberColor color;
            try {
                color = MemberColor.valueOf(parameters.color);
            } catch (IllegalArgumentException ex) {
                return Response.status(400, "Bad Request: color needs to be one of these: RED, GREEN, BLUE, YELLOW, PINK, ORANGE.").build();
            }
            if (parameters.password != null){
                if (parameters.password.length() >= 3){
                    m.setPassword(parameters.password);
                } else {
                    return Response.status(400, "Bad Request: Password needs to be more than 2 characters.").build();
                }
            }
            m.setName(parameters.name);
            m.setMemberRole(role);
            MemberDetail md = m.getDetails();
            md.setBirthday(d);
            md.setColor(color);
            m.setDetails(md);
            try {
                memberRepo.updateMember(m);
            } catch (AppException ex) {
                Logger.getLogger(MemberResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(500, "Server Error: Failed while trying to update a member").build();
            }
            return Response.ok(jsonb.toJson(m)).build();
        } else {
            return Response.status(400, "Bad Request: Member could not be created. This Form is needed: {\n" +
                    "    name: ...,\n" +
                    "    password: ...,\n" +
                    "    birthdate: yyyy-mm-dd\n" +
                    "    role: ...,\n" +
                    "    color: ...,\n" +
                    "}").build();
        }
    }
    
    @DELETE
    @Path("{id}")
    public Response deleteApartment(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr) {
        Long apartmentId;
        Long memberId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            memberId = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        Member m = null;
        try {
            m = this.memberRepo.findMember(memberId);
        } catch (AppException ex) {
            Logger.getLogger(MemberResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(m != null){
            if(m.getApartmentID().equals(apartmentId)){
                return Response.status(500, "Missing Implementation of Member.delteted Attribute").build();
                //m.setDeleted(true);
                //try {
                //    this.memberRepo.updateMember(m);
                //    return Response.ok().build();
                //} catch (AppException ex) {
                //    Logger.getLogger(MemberResource.class.getName()).log(Level.SEVERE, null, ex);
                //    return Response.status(500, "Server Error: Failure while trying to mark member as deleted").build();
                //}
            }
        }
        return Response.status(400, "Bad Request:  No member found for this id").build();
    }
}
