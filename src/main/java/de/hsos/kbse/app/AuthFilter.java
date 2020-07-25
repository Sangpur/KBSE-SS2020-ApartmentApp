/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hsos.kbse.app;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.Conversation;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Lucca
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter{

    private static final Set<String> ALLOWED_PATHS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("/ApartmentApp/faces/login.xhtml", "/ApartmentApp/faces/register.xhtml", "")));

    @Inject
    private Conversation conversation;
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false);

        String path = request.getRequestURI();
        if (!path.endsWith(".xhtml")) {
            chain.doFilter(request, response);
            return;
        }
        System.out.println(">>>>>>>>>>");        
        System.out.println(this.conversation); 
        System.out.println(path);
        if (ALLOWED_PATHS.contains(path) || (session != null && session.getAttribute("user") != null)) {
            chain.doFilter(req, res);
        } else {
            RequestDispatcher rd = request.getRequestDispatcher("/faces/login.xhtml");
            rd.forward(request, response);
        }
    }
}
