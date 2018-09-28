package com.revolut.task;

import com.revolut.task.di.InjectorProvider;
import com.revolut.task.service.api.DatabaseManager;
import com.revolut.task.util.DatabaseMigrator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class AppStarter {
    public static void main(String[] args) {
        DatabaseMigrator.run();
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> InjectorProvider.provide().getInstance(DatabaseManager.class).close())
        );
        startServer(8080, true);
    }


    public static Server startServer(int port, boolean joinThread) {
        Server server = new Server(port);
        ServletContextHandler ctx =
                new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

        ctx.setContextPath("/");
        server.setHandler(ctx);

        ServletHolder serHol = ctx.addServlet(ServletContainer.class, "/api/*");
        serHol.setInitOrder(1);
        serHol.setInitParameter("jersey.config.server.provider.packages",
                "com.revolut.task.http");

        server.setErrorHandler(new ErrorHandler());
        try {
            server.start();
            if (joinThread) {
                server.join();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (joinThread) {
                server.destroy();
            }
        }
        return server;
    }
}
