package com.winson.poc.micro.gateway.api.grpc;

import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Wei on 2016/12/2.
 */
public class GrpcProxy extends ProxyServlet.Transparent {

    protected static final Logger LOG = Log.getLogger(GrpcProxy.class);

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException{
        LOG.info("============");
        super.service(request, response);

    }
}
