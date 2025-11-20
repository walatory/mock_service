package com.mock.core;

import com.mock.model.MockServiceConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DispatcherServlet extends HttpServlet {

    private final MockRequestDispatcher dispatcher;
    private final MockServiceConfig config;

    public DispatcherServlet(MockRequestDispatcher dispatcher, MockServiceConfig config) {
        this.dispatcher = dispatcher;
        this.config = config;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        dispatcher.dispatch(req, resp, config);
    }
}
