/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hsos.kbse.app.boundary.rs;

import de.hsos.kbse.app.boundary.rs.DTOs.PaymentInputDTO;
import de.hsos.kbse.app.control.ApartmentRepository;
import de.hsos.kbse.app.control.CashFlow;
import de.hsos.kbse.app.control.MemberRepository;
import de.hsos.kbse.app.entity.Apartment;
import de.hsos.kbse.app.entity.features.Payment;
import de.hsos.kbse.app.entity.member.Member;
import de.hsos.kbse.app.util.AppException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
@Path("apartments/{apartmentId}/payments")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class CashFlowResource implements Serializable {
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    @Inject 
    ApartmentRepository apartmentRepo;
    
    @Inject 
    MemberRepository memberRepo;
    
    @Inject 
    CashFlow paymentRepo;
    
    @Inject
    private Jsonb jsonb;
    
    @Context
    UriInfo uriInfo;
    
    private static final SimpleDateFormat DF = new SimpleDateFormat( "yyyy-MM-dd" );
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @GET
    public Response getAllPayments(@PathParam("apartmentId") String apartmentIdStr) {
        Long apartmentId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id is not a number").build();
        }
        List<Payment> results = null;
        try {
            results = this.paymentRepo.getAllPaymentsFrom(apartmentId);
        } catch (AppException ex) {
            Logger.getLogger(CashFlowResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Payments.").build();
        }
        if(results != null)
            return Response.ok(jsonb.toJson(results)).build();
        return Response.ok("[]").build(); // Return empty Array if no Payments were found.
    }
    
    @GET
    @Path("{id}")
    public Response getPayment(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr) {
        Long apartmentId;
        Long itemId;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            itemId = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        Payment result = null;
        try {
            result = this.paymentRepo.findPayment(itemId);
        } catch (AppException ex) {
            Logger.getLogger(CashFlowResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Payment.").build();
        }
        if(result != null){
            if(result.getApartmentID().equals(apartmentId))
                return Response.ok(jsonb.toJson(result)).build();
        }
        return Response.ok("{}").build(); // Returns empty object for wrong id 
    }
    
    @POST
    public Response createPayment(@PathParam("apartmentId") String apartmentIdStr, PaymentInputDTO parameters) {
        Long apartmentId;
        Long giverId;
        List<Long> listOfInvolvedId = new ArrayList<>();
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            giverId = Long.parseLong(parameters.giverid);

            
            if(parameters.listofinvolvedid == null || parameters.listofinvolvedid.isEmpty())
                return Response.status(400, "Bad Request: listofinvolvedid needs at least one member id.").build();
            
            for(int i = 0; i< parameters.listofinvolvedid.size(); i++){
                listOfInvolvedId.add(Long.parseLong(parameters.listofinvolvedid.get(i)));
            }
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id, giverid or one of id of listofinvolvedid is not a number.").build();
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
        
        Member giver = null;
        try {
            giver = this.memberRepo.findMember(giverId);
        } catch (AppException ex) {
            Logger.getLogger(CashFlowResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(giver == null)
            return Response.status(400, "Bad Request: No member with this giverid found.").build();
        
        if(!giver.getApartmentID().equals(apartmentId))
            return Response.status(400, "Bad Request: Giver aparmentId does not match URL apartmentId.").build();
        
        List<Member> involved = new ArrayList<>();
        for(int i = 0; i< listOfInvolvedId.size(); i++) {
            Member m = null;
            try {
                m = this.memberRepo.findMember(listOfInvolvedId.get(i));
            } catch (AppException ex) {
                Logger.getLogger(CashFlowResource.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(m == null)
                return Response.status(400, "Bad Request: No member with this involvedid " + listOfInvolvedId.get(i) + " found.").build();

            if(!m.getApartmentID().equals(apartmentId))
                return Response.status(400, "Bad Request: Involved member with id " + listOfInvolvedId.get(i) + " aparmentId does not match URL apartmentId.").build();
            
            involved.add(m);
        }
        
        if (parameters.sum != null && parameters.sum.length() >= 1
                && parameters.description != null && parameters.description.length() >= 3 ) {
            
            BigDecimal sum;
            Locale de_DE = new Locale("de","DE");

            DecimalFormat nf = (DecimalFormat)NumberFormat.getInstance(de_DE);
            nf.setParseBigDecimal(true);

            sum = (BigDecimal)nf.parse(parameters.sum, new ParsePosition(0));

            if (sum == null) {
                return Response.status(400, "Bad Request: sum could not be parsed as a BigDecimal. This Format is needed: 0,00001 .").build();
            }
            
            Payment p = new Payment(giver, new Date(), apartmentId);
            p.setInvolvedMembers(involved);
            p.setSum(sum);
            p.setDescription(parameters.description);
            
            try {
                paymentRepo.createPayment(p);
            } catch (AppException ex) {
                Logger.getLogger(CashFlowResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(500, "Server Error: Failed while trying to create the Payment.").build();
            }
            return Response.ok(jsonb.toJson(p)).build();
        } else {
            return Response.status(400, "Bad Request: Payment could not be created. This Form is needed: {\n" +
                    "    giverid: ...,\n" +
                    "    listofinvolvedid: [ ... , ... , ... ],\n" +
                    "    sum: ...,\n" +
                    "    description: ...,\n" +
                    "}").build();
        }
    }
    
    @PUT
    @Path("{id}")
    public Response updatePayment(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr, PaymentInputDTO parameters) {
        Long apartmentId;
        Long id;
        List<Long> listOfInvolvedId = new ArrayList<>();
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            id = Long.parseLong(idStr);

            
            if(parameters.listofinvolvedid == null || parameters.listofinvolvedid.isEmpty())
                return Response.status(400, "Bad Request: listofinvolvedid needs at least one member id.").build();
            
            for(int i = 0; i< parameters.listofinvolvedid.size(); i++){
                listOfInvolvedId.add(Long.parseLong(parameters.listofinvolvedid.get(i)));
            }
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url id, giverid or one of id of listofinvolvedid is not a number.").build();
        }
        
        Payment p = null;
        try {
            p = this.paymentRepo.findPayment(id);
        } catch (AppException ex) {
            Logger.getLogger(CashFlowResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Payment.").build();
        }
        if(p == null){
            return Response.status(400, "Bad Request: Payment with this id could not be found.").build();
        }
        if(!p.getApartmentID().equals(apartmentId)){
            return Response.status(400, "Bad Request: Apartment id does not match URL.").build();
        }
        
        List<Member> involved = new ArrayList<>();
        for(int i = 0; i< listOfInvolvedId.size(); i++) {
            Member m = null;
            try {
                m = this.memberRepo.findMember(listOfInvolvedId.get(i));
            } catch (AppException ex) {
                Logger.getLogger(CashFlowResource.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(m == null)
                return Response.status(400, "Bad Request: No member with this involvedid " + listOfInvolvedId.get(i) + " found.").build();

            if(!m.getApartmentID().equals(apartmentId))
                return Response.status(400, "Bad Request: Involved member with id " + listOfInvolvedId.get(i) + " aparmentId does not match URL apartmentId.").build();
            
            involved.add(m);
        }
        
        
        if (parameters.sum != null && parameters.sum.length() >= 1
                && parameters.description != null && parameters.description.length() >= 3 ) {
            
            BigDecimal sum;
            Locale de_DE = new Locale("de","DE");

            DecimalFormat nf = (DecimalFormat)NumberFormat.getInstance(de_DE);
            nf.setParseBigDecimal(true);

            sum = (BigDecimal)nf.parse(parameters.sum, new ParsePosition(0));

            if (sum == null) {
                return Response.status(400, "Bad Request: sum could not be parsed as a BigDecimal. This Format is needed: 0,00001 .").build();
            }
            
            
            p.setInvolvedMembers(involved);
            p.setSum(sum);
            p.setDescription(parameters.description);
            
            
            try {
                paymentRepo.updatePayment(p);
            } catch (AppException ex) {
                Logger.getLogger(CashFlowResource.class.getName()).log(Level.SEVERE, null, ex);
                return Response.status(500, "Server Error: Failed while trying to update the Payment.").build();
            }
            return Response.ok(jsonb.toJson(p)).build();
        } else {
            return Response.status(400, "Bad Request: Payment could not be created. This Form is needed: {\n" +
                    "    giverid: ...,\n" +
                    "    listofinvolvedid: [ ... , ... , ... ],\n" +
                    "    sum: ...,\n" +
                    "    description: ...,\n" +
                    "}").build();
        }
    }
    
    @DELETE
    @Path("{id}")
    public Response deletePayment(@PathParam("apartmentId") String apartmentIdStr, @PathParam("id") String idStr) {
        Long apartmentId;
        Long id;
        try {
            apartmentId = Long.parseLong(apartmentIdStr);
            id = Long.parseLong(idStr);
        } catch(NumberFormatException e) {
            return Response.status(400, "Bad Request: url ids are not numbers").build();
        }
        Payment p = null;
        try {
            p = this.paymentRepo.findPayment(id);
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to find the Payment.").build();
        }
        if(p == null){
            return Response.status(400, "Bad Request: Payment with this id could not be found.").build();
        }
        if(!p.getApartmentID().equals(apartmentId)){
            return Response.status(400, "Bad Request: Apartment id does not match URL.").build();
        }
        try {
            paymentRepo.deletePayment(p);
            return Response.ok().build();
        } catch (AppException ex) {
            Logger.getLogger(ShoppingListResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(500, "Server Error: Failed while trying to delete the Payment.").build();
        } 
    }
    
}
