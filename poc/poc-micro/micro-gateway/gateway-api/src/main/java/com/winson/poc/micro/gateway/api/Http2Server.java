package com.winson.poc.micro.gateway.api;

//
//  ========================================================================
//  Copyright (c) 1995-2016 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//


import org.eclipse.jetty.alpn.ALPN;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Date;


/* ------------------------------------------------------------ */

/**
 */
public class Http2Server {
    public static void main(String... args) throws Exception {
        Server server = new Server();

//        MBeanContainer mbContainer = new MBeanContainer(
//                ManagementFactory.getPlatformMBeanServer());
//        server.addBean(mbContainer);



        WebAppContext wac = new WebAppContext();
        wac.setResourceBase("src/main/webapp");
        wac.setDescriptor("WEB-INF/web.xml");
        wac.setContextPath("/");
//        wac.setParentLoaderPriority(true);
        server.setHandler(wac);


//        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
//        context.setResourceBase("src/main/webapp");
//
//        context.setInitParameter("proxyTo","http://localhost:50051");
//        context.setInitParameter("prefix","/");
////        context.addFilter(PushCacheFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
//        // context.addFilter(PushSessionCacheFilter.class,"/*",EnumSet.of(DispatcherType.REQUEST));
////        context.addFilter(PushedTilesFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
//        context.addServlet(new ServletHolder(new GrpcProxy()), "/");


//        context.addServlet(DefaultServlet.class, "/").setInitParameter("maxCacheSize", "81920");
//        server.setHandler(context);

//        ConnectHandler proxy = new ConnectHandler();
//        server.setHandler(proxy);
//
//        ServletContextHandler context = new ServletContextHandler(proxy, "/",
//                ServletContextHandler.SESSIONS);
//        ServletHolder proxyServlet = new ServletHolder(ProxyServlet.class);
//        //proxyServlet.setInitParameter("blackList", "www.eclipse.org");
//        proxyServlet.setInitParameter("proxyTo", "http://localhost:50051");
//        proxyServlet.setInitParameter("prefix", "/");
//
//        context.addServlet(proxyServlet, "/");



        // HTTP Configuration
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(8443);
        http_config.setSendXPoweredBy(true);
        http_config.setSendServerVersion(true);


        // HTTP Connector
        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config), new HTTP2CServerConnectionFactory(http_config));
        http.setPort(8080);
        server.addConnector(http);


/*
        // SSL Context Factory for HTTPS and HTTP/2
        String jetty_distro = System.getProperty("jetty.distro", "../../jetty-distribution/target/distribution");
        SslContextFactory sslContextFactory = new SslContextFactory();
//        sslContextFactory.setKeyStorePath(jetty_distro + "/demo-base/etc/keystore");

        sslContextFactory.setKeyStoreResource(newClassPathResource("keystore"));


        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);

        // HTTPS Configuration
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        // HTTP/2 Connection Factory
        HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(https_config);

        NegotiatingServerConnectionFactory.checkProtocolNegotiationAvailable();
        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        alpn.setDefaultProtocol(http.getDefaultProtocol());

        // SSL Connection Factory
        SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

        // HTTP/2 Connector
        ServerConnector http2Connector =
                new ServerConnector(server, ssl, alpn, h2, new HttpConnectionFactory(https_config));
        http2Connector.setPort(8443);
        server.addConnector(http2Connector);*/

        ALPN.debug = false;

        server.start();
        //server.dumpStdErr();
        server.join();
    }



    ;

    static Servlet servlet = new HttpServlet() {
        private static final long serialVersionUID = 1L;

        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String code = request.getParameter("code");
            if (code != null)
                response.setStatus(Integer.parseInt(code));

            HttpSession session = request.getSession(true);
            if (session.isNew())
                response.addCookie(new Cookie("bigcookie",
                        "This is a test cookies that was created on " + new Date() + " and is used by the jetty http/2 test servlet."));
            response.setHeader("Custom", "Value");
            response.setContentType("text/plain");
            String content = "Hello from Jetty using " + request.getProtocol() + "\n";
            content += "uri=" + request.getRequestURI() + "\n";
            content += "session=" + session.getId() + (session.isNew() ? "(New)\n" : "\n");
            content += "date=" + new Date() + "\n";

            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0)
                for (Cookie c : cookies)
                    content += "cookie " + c.getName() + "=" + c.getValue() + "\n";

            response.setContentLength(content.length());
            response.getOutputStream().print(content);
        }
    };
}