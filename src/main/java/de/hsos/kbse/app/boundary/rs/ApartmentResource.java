/*
 * JAX RS BOUNDARY CLASS ApartmentResource
 * 
 *
 */
package de.hsos.kbse.app.boundary.rs;

import de.hsos.kbse.app.entity.rs.ApartmentDTO;
import de.hsos.kbse.app.control.ApartmentRepository;
import de.hsos.kbse.app.control.Calendar;
import de.hsos.kbse.app.control.CashFlow;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.control.Pinboard;
import de.hsos.kbse.app.control.ShoppingList;
import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.features.Note;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.MemberColor;
import de.hsos.kbse.app.enums.MemberRole;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Lucca Oberhößel
 */
@Stateless
@Path("apartments")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class ApartmentResource implements Serializable {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    @Inject 
    ApartmentRepository repository;
    
    @Inject 
    MemberRepository memberRepo;
    
    @Inject 
    Pinboard noteRepo;
    
    @Inject 
    Calendar eventRepo;
    
    @Inject 
    CashFlow paymentRepo;
    
    @Inject 
    ShoppingList shoppingRepo;
    
    @Inject
    private Jsonb jsonb;
    
    @Context
    UriInfo uriInfo;
    
    private static final SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd" );
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @GET
    public Response getAllApartments() {
        List<Apartment> result = null;
        try {
            result = this.repository.getAllApartments();
        } catch (AppException ex) {
            Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(result != null)
            return Response.ok(jsonb.toJson(result)).build();
        return Response.serverError().build();
    }
    
    @GET
    @Path("{id}")
    public Response getApartment(@PathParam("id") String id) {
        Long apartmentId;
        try {
            apartmentId = Long.parseLong(id);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id is not a number").build();
        }
        Apartment result = null;
        try {
            result = this.repository.findApartment(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(result != null){
            return Response.ok(jsonb.toJson(result)).build();
        }
        return Response.ok(jsonb.toJson(new Object())).build(); // Returns empty object for wrong id 
    }
    
    @POST
    public Response createApartment(ApartmentDTO parameters) {
        System.out.println(jsonb.toJson(parameters));
        
        if(parameters.apartmentname != null){
            Apartment a = new Apartment(parameters.apartmentname);
            try {
                repository.createApartment(a);
            } catch (AppException ex) {
                Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.serverError().build();
            }
            if(parameters.username != null && parameters.username.length() >= 3 
                    && parameters.password != null && parameters.password.length() >= 3 
                    && parameters.birthdate != null && parameters.birthdate.length() == 10){
                Date d;
                try {
                    d = new Date(df.parse(parameters.birthdate).getTime());
                } catch (ParseException ex) {
                    Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
                    return Response.status(400, "Bad Request: birthdate format has to be yyyy-mm-dd").build();
                }
                Member m = new Member(parameters.username, MemberRole.ADMIN, parameters.password, d, MemberColor.RED);
                m.setApartmentID(a.getId());
                try {
                    memberRepo.createMember(m);
                } catch (AppException ex) {
                    Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
                    return Response.status(400, "Bad Request: Member could not be created. This Form is needed: {\n" +
                        "    apartmentname: ...,\n" +
                        "    username: ...,\n" +
                        "    password: ...,\n" +
                        "    birthdate: yyyy-mm-dd\n" +
                        "}").build();
                }
                return Response.ok("{\"apartment\":" +jsonb.toJson(a) + ", \"member\": " + jsonb.toJson(m) + "}").build();
            } else {
                return Response.status(400, "Bad Request: Member could not be created. This Form is needed: {\n" +
                        "    apartmentname: ...,\n" +
                        "    username: ...,\n" +
                        "    password: ...,\n" +
                        "    birthdate: yyyy-mm-dd\n" +
                        "}").build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
    
    @PUT
    @Path("{id}")
    public Response updateApartment(@PathParam("id") String id, Apartment parameters) {
        Long apartmentId;
        try {
            apartmentId = Long.parseLong(id);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id is not a number").build();
        }
        Apartment a = null;
        try {
            a = this.repository.findApartment(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: AppException when looking for apartment with this id").build();
        }
        if(a == null)
            return Response.status(400, "Bad Request: No apartment found for this id").build();
        if(parameters.getName() != null && parameters.getName().length() >= 3) {
            a.setName(parameters.getName());
            try {
                repository.updateApartment(a);
            } catch (AppException ex) {
                Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(500, "Server Error: Can not update apartment").build();
            }
            return Response.ok(jsonb.toJson(a)).build();
        } else {
            return Response.status(400, "Bad Request: Apartment name is missing. This form is needed: { name : ... }").build();
        }
    }
    
    @DELETE
    @Path("{id}")
    public Response updateApartment(@PathParam("id") String id) {
        Long apartmentId;
        try {
            apartmentId = Long.parseLong(id);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id is not a number").build();
        }
        Apartment a = null;
        try {
            a = this.repository.findApartment(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(a == null){
            return Response.status(400, "Bad Request: Apartment with this id could not be found.").build();
        }
        
        
        
        try {
            this.eventRepo.deleteAllEventsFrom(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to delete all events for aparment.").build();
        }
        try {
            this.noteRepo.deleteAllNotesFrom(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to delete all notes for aparment.").build();
        }
        try {
            this.paymentRepo.deleteAllPaymentsFrom(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to delete all payments for aparment.").build();
        }
        try {
            this.shoppingRepo.deleteAllShoppingItemsFrom(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to delete all shoppingitems for aparment.").build();
        }
        try {
            this.memberRepo.deleteAllMembersFrom(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to delete all members for aparment.").build();
        }
        try {
            this.repository.deleteApartment(a);
        } catch (AppException ex) {
            Logger.getLogger(ApartmentResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to delete the aparment.").build();
        }
        
        
        
        return Response.ok(jsonb.toJson(new Object())).build(); // Returns empty object for wrong id 
    }
}
