/*
 * WEBFILTER CLASS
 *
 */
package de.hsos.kbse.app;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
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
 * @author Lucca Oberhößel
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {
    
    /* ----------------------------------------- ATTRIBUTE ---------------------------------------- */
    
    private static final Set<String> ALLOWED_PATHS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("/KBSE-SS2020-ApartmentApp/faces/login.xhtml", "/KBSE-SS2020-ApartmentApp/faces/register.xhtml", "")));
    
    /* -------------------------------------- METHODEN PUBLIC ------------------------------------- */
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
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
        if (ALLOWED_PATHS.contains(path) || (session != null && session.getAttribute("user") != null)) {
            chain.doFilter(req, res);
        } else {
            RequestDispatcher rd = request.getRequestDispatcher("/faces/login.xhtml");
            rd.forward(request, response);
        }
    }
    
    @Override
    public void destroy() {
    }
}
