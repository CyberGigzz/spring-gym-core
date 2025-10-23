package com.gym.crm.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import jakarta.servlet.FilterConfig; // <-- ADD THIS IMPORT

import java.io.IOException;
import java.util.UUID;

public class LoggingFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String transactionId = UUID.randomUUID().toString();

        try {
            MDC.put(TRANSACTION_ID_KEY, transactionId);
            LOGGER.info(">>> Request Start: {} {} | Remote Addr: {}",
                    req.getMethod(), req.getRequestURI(), req.getRemoteAddr());
            chain.doFilter(request, response);
        } finally {
            LOGGER.info("<<< Response End: {} {} | Status: {}",
                    req.getMethod(), req.getRequestURI(), res.getStatus());
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }

    /**
     * Called by the web container to indicate to a filter that it is
     * being placed into service.
     * @param filterConfig a {@code FilterConfig} object containing the
     * filter's configuration and initialization parameters
     * @throws ServletException if an exception has occurred that interferes with
     * the filter's normal operation
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code, if needed (e.g., read filter init-params)
        LOGGER.info("LoggingFilter initialized.");
    }

    /**
     * Called by the web container to indicate to a filter that it is being
     * taken out of service.
     */
    @Override
    public void destroy() {
        // Cleanup code, if needed
        LOGGER.info("LoggingFilter destroyed.");
    }

    // REMOVE THE DUPLICATE init() AND destroy() METHODS THAT WERE HERE
}