package com.gym.crm.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.sql.SQLException;

@Configuration
public class H2Config {

    /**
     * This bean starts the H2 TCP server, which allows the H2 console
     * to connect to the same in-memory database that your application is using.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2Server() throws SQLException {
        // Starts a TCP server on port 9092
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
    }
}