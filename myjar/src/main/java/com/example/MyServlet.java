package com.example;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class MyServlet extends HttpServlet
{
    protected void doGet(
        HttpServletRequest request,
        HttpServletResponse response
    )
    throws IOException {
        try (PrintWriter writer = response.getWriter()){
            writer.print("Overlay servlet");
            writer.flush();
        }
    } 
}
