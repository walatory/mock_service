package com.mock.core;

import com.mock.model.MockServiceConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class DynamicServerManager {

    private final Map<String, WebServer> runningServers = new ConcurrentHashMap<>();
    private final MockRequestDispatcher dispatcher;
    private final EurekaRegistrar eurekaRegistrar;

    public DynamicServerManager(MockRequestDispatcher dispatcher, EurekaRegistrar eurekaRegistrar) {
        this.dispatcher = dispatcher;
        this.eurekaRegistrar = eurekaRegistrar;
    }

    public synchronized void startService(MockServiceConfig config) {
        if (runningServers.containsKey(config.getId())) {
            log.warn("Service {} is already running", config.getServiceName());
            return;
        }

        try {
            ServletWebServerFactory factory = new TomcatServletWebServerFactory(config.getPort());
            WebServer server = factory.getWebServer(new ServletContextInitializer() {
                @Override
                public void onStartup(ServletContext servletContext) throws ServletException {
                    ServletRegistration.Dynamic registration = servletContext.addServlet("dispatcher",
                            new DispatcherServlet(dispatcher, config));
                    registration.addMapping("/*");
                    registration.setLoadOnStartup(1);
                }
            });

            server.start();
            runningServers.put(config.getId(), server);
            config.setRunning(true);
            log.info("Started mock service [{}] on port {}", config.getServiceName(), config.getPort());

            eurekaRegistrar.register(config);

        } catch (Exception e) {
            log.error("Failed to start service {}", config.getServiceName(), e);
            throw new RuntimeException("Failed to start service", e);
        }
    }

    public synchronized void stopService(MockServiceConfig config) {
        WebServer server = runningServers.remove(config.getId());
        if (server != null) {
            server.stop();
            config.setRunning(false);
            log.info("Stopped mock service [{}]", config.getServiceName());

            eurekaRegistrar.deregister(config);
        }
    }

    public boolean isRunning(String serviceId) {
        return runningServers.containsKey(serviceId);
    }
}
