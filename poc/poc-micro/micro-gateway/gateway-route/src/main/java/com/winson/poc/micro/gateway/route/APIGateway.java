package com.winson.poc.micro.gateway.route;

import org.eclipse.jetty.client.ContinueProtocolHandler;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.ProtocolHandlers;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.concurrent.Executor;

/**
 * Created by Wei on 2016/11/30.
 */
public class APIGateway extends ProxyServlet.Transparent {

    private static final Logger LOG = LoggerFactory.getLogger(APIGateway.class);

    @Override
    public void init() throws ServletException {
        super.init();
        LOG.info("Entering [APIGateway].[init]");
        Log.getRootLogger().setDebugEnabled(true);
    }

    @Override
    protected void copyRequestHeaders(HttpServletRequest clientRequest, Request proxyRequest) {
        super.copyRequestHeaders(clientRequest, proxyRequest);
        for (Enumeration<String> headerNames = clientRequest.getHeaderNames(); headerNames.hasMoreElements(); ) {
            String headerName = headerNames.nextElement();
            if ("te".equalsIgnoreCase(headerName)) {
                for (Enumeration<String> headerValues = clientRequest.getHeaders(headerName); headerValues.hasMoreElements(); ) {
                    String headerValue = headerValues.nextElement();
                    if (headerValue != null) {
                        proxyRequest.header(headerName, headerValue);
                    }
                }

            }
        }
    }

    @Override
    protected HttpClient createHttpClient() throws ServletException {


        LOG.info("Entering [APIGateway].[createHttpClient]");

        ServletConfig config = getServletConfig();

        HttpClient client = new HttpClient(new HttpClientTransportOverHTTP2(new HTTP2Client()), null);

        client.setFollowRedirects(false);

        client.setCookieStore(new HttpCookieStore.Empty());

        Executor executor;
        String value = config.getInitParameter("maxThreads");
        if (value == null || "-".equals(value)) {
            executor = (Executor) getServletContext().getAttribute("org.eclipse.jetty.server.Executor");
            if (executor == null)
                throw new IllegalStateException("No server executor for proxy");
        } else {
            QueuedThreadPool qtp = new QueuedThreadPool(Integer.parseInt(value));
            String servletName = config.getServletName();
            int dot = servletName.lastIndexOf('.');
            if (dot >= 0)
                servletName = servletName.substring(dot + 1);
            qtp.setName(servletName);
            executor = qtp;
        }

        client.setExecutor(executor);

        value = config.getInitParameter("maxConnections");
        if (value == null)
            value = "256";
        client.setMaxConnectionsPerDestination(Integer.parseInt(value));

        value = config.getInitParameter("idleTimeout");
        if (value == null)
            value = "30000";
        client.setIdleTimeout(Long.parseLong(value));

        value = config.getInitParameter("timeout");
        if (value == null)
            value = "60000";
        super.setTimeout(Long.parseLong(value));

        value = config.getInitParameter("requestBufferSize");
        if (value != null)
            client.setRequestBufferSize(Integer.parseInt(value));

        value = config.getInitParameter("responseBufferSize");
        if (value != null)
            client.setResponseBufferSize(Integer.parseInt(value));

        try {
            client.start();

            // Content must not be decoded, otherwise the client gets confused.
            client.getContentDecoderFactories().clear();

            // Pass traffic to the client, only intercept what's necessary.
            ProtocolHandlers protocolHandlers = client.getProtocolHandlers();
            protocolHandlers.clear();
            protocolHandlers.put(new ProxyContinueProtocolHandler());


            return client;
        } catch (Exception x) {
            throw new ServletException(x);
        }
    }

    class ProxyContinueProtocolHandler extends ContinueProtocolHandler {
        @Override
        protected void onContinue(Request request) {
            LOG.info("Entering [ProxyContinueProtocolHandler].[onContinue]");

            HttpFields fields = request.getHeaders();

            if (fields != null)
                for (String name : fields.getFieldNamesCollection()) {
                    LOG.info("Name:" + name);
                    LOG.info("Value: " + fields.get(name));
                }

            HttpServletRequest clientRequest = (HttpServletRequest) request.getAttributes().get(CLIENT_REQUEST_ATTRIBUTE);
            APIGateway.this.onContinue(clientRequest, request);

            LOG.info("Leaving [ProxyContinueProtocolHandler].[onContinue]");
        }
    }
}
