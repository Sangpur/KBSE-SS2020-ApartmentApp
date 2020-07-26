/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hsos.kbse.app.boundary.rs;

import de.hsos.kbse.app.entity.rs.NoteDTO;
import de.hsos.kbse.app.control.ApartmentRepository;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.control.Pinboard;
import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.features.Note;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.enums.NoteCategory;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
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
@Path("apartments/{apartmentId}/notes")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class PinboardResource implements Serializable {
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    @Inject 
    ApartmentRepository apartmentRepo;
    
    @Inject 
    MemberRepository memberRepo;
    
    @Inject 
    Pinboard noteRepo;
    
    @Inject
    private Jsonb jsonb;
    
    @Context
    UriInfo uriInfo;
    
    private static final SimpleDateFormat DF = new SimpleDateFormat( "yyyy-MM-dd" );
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @GET
    public Response getAllNotes(@PathParam("apartmentId") String apartmentIdStr) {
        Long apartmentId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id is not a number").build();
        }
        List<Note> results = null;
        try {
            results = this.noteRepo.getAllNotesFrom(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(PinboardResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Notes.").build();
        }
        if(results != null)
            return Response.ok(jsonb.toJson(results)).build();
        return Response.ok("[]").build(); // Return empty Array if no Notes were found.
    }
    
    @GET
    @Path("{id}")
    public Response getNote(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr) {
        Long apartmentId;
        Long itemId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            itemId = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        Note result = null;
        try {
            result = this.noteRepo.findNote(itemId);
        } catch (AppException ex) {
            Logger.getLogger(PinboardResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Note.").build();
        }
        if(result != null){
            if(result.getApartmentID().equals(apartmentId))
                return Response.ok(jsonb.toJson(result)).build();
        }
        return Response.ok("{}").build(); // Returns empty object for wrong id 
    }
    
    @POST
    public Response createNote(@PathParam("apartmentId") String apartmentIdStr, NoteDTO parameters) {
        Long apartmentId;
        Long authorId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            authorId = Long.parseLong(parameters.authorid);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id or authorid is not a number").build();
        }
        Apartment a = null;
        try {
            a = apartmentRepo.findApartment(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find apartment.").build();
        }
        if(a == null){
            return Response.status(400, "Bad Request: No apartment with this id found.").build();
        }
        
        Member m = null;
        try {
            m = this.memberRepo.findMember(authorId);
        } catch (AppException ex) {
            Logger.getLogger(PinboardResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(m == null)
            return Response.status(400, "Bad Request: No member with this authorid found.").build();
        
        if(!m.getApartmentID().equals(apartmentId))
            return Response.status(400, "Bad Request: Author aparmentId does not match URL apartmentId.").build();
        
        
        
        if (parameters.message != null && parameters.message.length() >= 3
                && parameters.category != null && parameters.category.length() >= 4) {
            
            
            NoteCategory category;
            try {
                category = NoteCategory.valueOf(parameters.category);
            } catch (IllegalArgumentException ex) {
                return Response.status(400, "Bad Request: category needs to be either INFO, TODO or DRINGEND.").build();
            }
            
            
            Note n = new Note(m, new Date(), apartmentId);
            n.setMessage(parameters.message);
            n.setCategory(category);
            try {
                noteRepo.createNote(n);
            } catch (AppException ex) {
                Logger.getLogger(PinboardResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(500, "Server Error: Failed while trying to create the Note.").build();
            }
            return Response.ok(jsonb.toJson(n)).build();
        } else {
            return Response.status(400, "Bad Request: Note could not be created. This Form is needed: {\n" +
                    "    message: ...,\n" +
                    "    authorid: ...,\n" +
                    "    category: ...,\n" +
                    "}").build();
        }
    }
    
    @PUT
    @Path("{id}")
    public Response updateNote(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr, NoteDTO parameters) {
        Long apartmentId;
        Long id;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            id = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        Note n = null;
        try {
            n = this.noteRepo.findNote(id);
        } catch (AppException ex) {
            Logger.getLogger(PinboardResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Note.").build();
        }
        if(n == null){
            return Response.status(400, "Bad Request: Note with this id could not be found.").build();
        }
        if(!n.getApartmentID().equals(apartmentId)){
            return Response.status(400, "Bad Request: Apartment id does not match URL.").build();
        }
        
        
        if (parameters.message != null && parameters.message.length() >= 3
                && parameters.category != null && parameters.category.length() >= 4) {
            
            
            NoteCategory category;
            try {
                category = NoteCategory.valueOf(parameters.category);
            } catch (IllegalArgumentException ex) {
                return Response.status(400, "Bad Request: category needs to be either INFO, TODO or DRINGEND.").build();
            }
            
            n.setMessage(parameters.message);
            n.setCategory(category);
            
            
            try {
                noteRepo.updateNote(n);
            } catch (AppException ex) {
                Logger.getLogger(PinboardResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(500, "Server Error: Failed while trying to update the Note.").build();
            }
            return Response.ok(jsonb.toJson(n)).build();
        } else {
            return Response.status(400, "Bad Request: Note could not be updated. This Form is needed: {\n" +
                    "    message: ...,\n" +
                    "    authorid: ...,\n" +
                    "    category: ...,\n" +
                    "}").build();
        }
    }
    
    @DELETE
    @Path("{id}")
    public Response deleteNote(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr) {
        Long apartmentId;
        Long id;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            id = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        Note n = null;
        try {
            n = this.noteRepo.findNote(id);
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Note.").build();
        }
        if(n == null){
            return Response.status(400, "Bad Request: Note with this id could not be found.").build();
        }
        if(!n.getApartmentID().equals(apartmentId)){
            return Response.status(400, "Bad Request: Apartment id does not match URL.").build();
        }
        try {
            noteRepo.deleteNote(n);
            return Response.ok().build();
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to delete the Note.").build();
        } 
    }
    
}
