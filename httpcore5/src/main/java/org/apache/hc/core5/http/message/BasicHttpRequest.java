/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.hc.core5.http.message;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.net.URIAuthority;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.apache.hc.core5.util.Args;

/**
 * Basic implementation of {@link HttpRequest}.
 *
 * @since 4.0
 */
public class BasicHttpRequest extends HeaderGroup implements HttpRequest {

    private static final long serialVersionUID = 1L;

    private final String method;
    private ProtocolVersion version;
    private URI requestUri;
    private String scheme;
    private URIAuthority authority;
    private String path;
    private List<NameValuePair> queryParameters;

    /**
     * Creates request message with the given method and request path.
     *
     * @param method request method.
     * @param uri request uri as string.
     */
    public BasicHttpRequest(final String method, final String uri) {
        super();
        this.method = method;
        if (uri != null) {
            try {
                setUri(new URI(uri));
            } catch (final URISyntaxException ex) {
                this.path = uri;
            }
        }
    }

    /**
     * Creates request message with the given method, host and request path.
     *
     * @param method request method.
     * @param host request host.
     * @param path request path.
     * @since 5.0
     */
    public BasicHttpRequest(final String method, final HttpHost host, final String path) {
        this(method, host, path, (List<NameValuePair>) null);
    }

    /**
     * Creates request message with the given method, host and request path.
     *
     * @param method request method.
     * @param host request host.
     * @param path request path.
     * @param rawQueryParameters request query parameters.
     * @since 5.0
     */
    public BasicHttpRequest(final String method, final HttpHost host, final String path,
            final String rawQueryParameters) {
        this(method, host, path,
                URLEncodedUtils.parse(rawQueryParameters, Charset.defaultCharset()));
    }

    /**
     * Creates request message with the given method, host and request path.
     *
     * @param method request method.
     * @param host request host.
     * @param path request path.
     * @param queryParameters request query parameters.
     * @since 5.0
     */
    public BasicHttpRequest(final String method, final HttpHost host, final String path,
            final List<NameValuePair> queryParameters) {
        super();
        this.method = Args.notNull(method, "Method name");
        this.scheme = host != null ? host.getSchemeName() : null;
        this.authority = host != null ? new URIAuthority(host) : null;
        this.path = path;
        this.queryParameters = queryParameters;
    }

    /**
     * Creates request message with the given method, request URI.
     *
     * @param method request method.
     * @param requestUri request URI.
     * @since 5.0
     */
    public BasicHttpRequest(final String method, final URI requestUri) {
        super();
        this.method = Args.notNull(method, "Method name");
        setUri(Args.notNull(requestUri, "Request URI"));
    }

    @Override
    public void addHeader(final String name, final Object value) {
        Args.notNull(name, "Header name");
        addHeader(new BasicHeader(name, value));
    }

    @Override
    public void setHeader(final String name, final Object value) {
        Args.notNull(name, "Header name");
        setHeader(new BasicHeader(name, value));
    }

    @Override
    public void setVersion(final ProtocolVersion version) {
        this.version = version;
    }

    @Override
    public ProtocolVersion getVersion() {
        return this.version;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void setPath(final String path) {
        this.path = path;
        this.requestUri = null;
    }

    @Override
    public String getQueryParametersAsString() {
        if (this.queryParameters != null)
            return URLEncodedUtils.format(this.queryParameters, Charset.defaultCharset());
        return null;
    }

    @Override
    public void setQueryParameters(String rawQueryParameters) {
        this.queryParameters = URLEncodedUtils.parse(rawQueryParameters, Charset.defaultCharset());
        this.requestUri = null;
    }

    @Override
    public List<NameValuePair> getQueryParameters() {
        return this.queryParameters;
    }

    @Override
    public void setQueryParameters(List<NameValuePair> queryParameters) {
        this.queryParameters = queryParameters;
        this.requestUri = null;
    }

    @Override
    public String getScheme() {
        return this.scheme;
    }

    @Override
    public void setScheme(final String scheme) {
        this.scheme = scheme;
        this.requestUri = null;
    }

    @Override
    public URIAuthority getAuthority() {
        return this.authority;
    }

    @Override
    public void setAuthority(final URIAuthority authority) {
        this.authority = authority;
        this.requestUri = null;
    }

    @Override
    public String getRequestUri() {
        String s = this.path;
        if (this.queryParameters != null && !this.queryParameters.isEmpty()) {
            s += "?" + URLEncodedUtils.format(this.queryParameters, Charset.defaultCharset());
        }
        return s;
    }

    void setUri(final URI requestUri) {
        this.scheme = requestUri.getScheme();
        this.authority = requestUri.getHost() != null ? new URIAuthority(
                requestUri.getRawUserInfo(),
                requestUri.getHost(),
                requestUri.getPort()) : null;

        String rawQuery = requestUri.getRawQuery();

        if (rawQuery != null) {
            this.queryParameters = URLEncodedUtils.parse(rawQuery, Charset.defaultCharset());
        }

        String path = requestUri.getPath();
        if (path.isEmpty()) {
            this.path = "/";
        } else {
            this.path = path;
        }

        try {
            this.requestUri = getUri();
        } catch (URISyntaxException e) {
            // we can do nothing here
        }
    }

    @Override
    public URI getUri() throws URISyntaxException {
        if (this.requestUri == null) {
            URIBuilder builder = new URIBuilder();
            if (this.authority != null) {
                builder.setScheme(this.scheme != null ? this.scheme : "http");
                builder.setHost(this.authority.getHostName());
                if (this.authority.getPort() >= 0) {
                    builder.setPort(this.authority.getPort());
                }
            }

            if (this.path == null) {
                builder.setPath("/");
            } else if (!this.path.startsWith("/")) {
                builder.setPath("/" + this.path);
            } else {
                builder.setPath(this.path);
            }

            if (this.queryParameters != null) {
                builder.setParameters(this.queryParameters);
            }

            this.requestUri = builder.build();
        }
        return this.requestUri;
    }

    @Override
    public String toString() {
        String s = this.method + " " + this.scheme + "://" + this.authority + this.path;
        if (this.queryParameters != null && !this.queryParameters.isEmpty()) {
            s += "?" + URLEncodedUtils.format(this.queryParameters, Charset.defaultCharset());
        }
        return s;
    }

}
