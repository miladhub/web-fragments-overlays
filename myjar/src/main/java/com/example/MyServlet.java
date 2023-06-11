package com.example;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;

public class MyServlet extends HttpServlet
{
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    )
    throws IOException {
        try (PrintWriter writer = response.getWriter()){
            Principal principal = request.getUserPrincipal();
            String auth = (principal != null)
                    ? "logged in as " + principal.getName()
                    : "unauthenticated";
            writer.print("My servlet - " + auth);
            writer.flush();
        }
    } 
}
