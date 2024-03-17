/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.openhab.binding.octoprint.internal.services;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.octoprint.internal.models.OctoprintServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HttpRequestService} provides methods to send HTTP-Requests to a {@link OctoprintServer}.
 *
 * @author Jan Niklas Freisinger - Initial contribution
 */
public class HttpRequestService {
    private final Logger logger = LoggerFactory.getLogger(HttpRequestService.class);

    private final OctoprintServer server;
    HttpClient httpClient = new HttpClient();

    public HttpRequestService(OctoprintServer octoprintServer) {
        server = octoprintServer;
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ContentResponse getRequest(String route) throws ExecutionException, InterruptedException, TimeoutException {
        String uri = String.format("http://%1$s/%2$s", server.uri, route);
        logger.debug("uri: {}", uri);
        Request request = httpClient.newRequest(uri).header("X-Api-Key", server.apiKey).method(HttpMethod.GET);
        ContentResponse res = request.send();
        logger.debug("response: status: {}, body: {}", res.getStatus(), res.getContentAsString());
        return res;
    }

    public Response postRequest(String route, String body) {
        String uri = String.format("http://%1$s/%2$s", server.uri, route);
        logger.debug("uri: {}", uri);
        Request request = httpClient.newRequest(uri).header("X-Api-Key", server.apiKey)
                .header(HttpHeader.ACCEPT, "application/json").header(HttpHeader.CONTENT_TYPE, "application/json")
                .method(HttpMethod.POST).content(new StringContentProvider(body), "application/json");
        try {
            Response res = request.send();
            logger.debug("response: status: {}", res.getStatus());
            return res;
        } catch (InterruptedException e) {
            // TODO
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            // TODO
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            // TODO
            throw new RuntimeException(e);
        }
    }

    public void dispose() {
        try {
            httpClient.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
