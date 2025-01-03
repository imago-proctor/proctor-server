/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hmdm.plugins.audit.rest.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.awt.image.ImagingOpException;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * <p>An enumeration over the resources which are targets for audit tracking.</p>
 *
 * @author isv
 */
public enum ResourceAuditInfo {
    LOGIN("POST", "/rest/public/auth/login", true, "plugin.audit.action.user.login", true, true),
    JWT_LOGIN("POST", "/rest/public/jwt/login", true, "plugin.audit.action.jwt.login", true, false),
    UPDATE_DEVICE("PUT", "/rest/private/devices", true, "plugin.audit.action.update.device", true, true),
    REMOVE_DEVICE("DELETE", "/rest/private/devices", false, "plugin.audit.action.remove.device", true, true),
    UPDATE_CONFIG("PUT", "/rest/private/configurations", true, "plugin.audit.action.update.configuration", true, true),
    COPY_CONFIG("PUT", "/rest/private/configurations/copy", true, "plugin.audit.action.copy.configuration", true, true),
    REMOVE_CONFIG("DELETE", "/rest/private/configurations", false, "plugin.audit.action.remove.configuration", true, true),
    UPDATE_APP("PUT", "/rest/private/applications/android", true, "plugin.audit.action.update.application", true, true),
    UPDATE_WEBAPP("PUT", "/rest/private/applications/web", true, "plugin.audit.action.update.webapp", true, true),
    REMOVE_APP("DELETE", "/rest/private/applications", false, "plugin.audit.action.remove.application", true, true),
    UPDATE_APP_VERSION("PUT", "/rest/private/applications/versions", true, "plugin.audit.action.update.version", true, true),
    REMOVE_APP_VERSION("DELETE", "/rest/private/applications/versions", false, "plugin.audit.action.remove.version", true, true),
    UPDATE_FILE("POST", "/rest/private/web-ui-files/move", true, "plugin.audit.action.update.file", true, true),
    REMOVE_FILE("POST", "/rest/private/web-ui-files/remove", true, "plugin.audit.action.remove.file", true, true),
    UPDATE_APP_CONFIG("POST", "/rest/private/applications/configurations", true, "plugin.audit.action.update.app.config", true, true),
    UPDATE_VERSION_CONFIG("POST", "/rest/private/applications/version/configurations", true, "plugin.audit.action.version.config", true, true),
    UPDATE_DESIGN("POST", "/rest/private/settings/design", true, "plugin.audit.action.update.design", true, true),
    UPDATE_USERROLES("POST", "/rest/private/settings/userRoles", false, "plugin.audit.action.update.user.roles", true, true),
    UPDATE_LANGUAGE("POST", "/rest/private/settings/lang", true, "plugin.audit.action.update.language", true, true),
    UPDATE_PLUGINS("POST", "/rest/plugin/main/private/disabled", true, "plugin.audit.action.update.plugins", true, true),
    UPDATE_USER("PUT", "/rest/private/users", true, "plugin.audit.action.update.user", true, true),
    REMOVE_USER("DELETE", "/rest/private/users", false, "plugin.audit.action.remove.user", true, true),
    UPDATE_GROUP("PUT", "/rest/private/groups", true, "plugin.audit.action.update.group", true, true),
    REMOVE_GROUP("DELETE", "/rest/private/groups", false, "plugin.audit.action.remove.group", true, true),
    PASSWORD_CHANGED("PUT", "/rest/private/users/current", true, "plugin.audit.action.password.changed", true, true),
    DEVICE_PASSWORD_RESET("PUT", "/rest/plugins/devicereset/private/password", true, "plugin.audit.action.password.reset", true, true),
    DEVICE_FACTORY_RESET("PUT", "/rest/plugins/devicereset/private/reset", false, "plugin.audit.action.device.reset", false, true),
    DEVICE_LOCK("PUT", "/rest/plugins/devicereset/private/lock", true, "plugin.audit.action.device.lock", true, true);

    /**
     * <p>Method for the REST resource to track audit log for.</p>
     */
    private final String method;

    /**
     * <p>An URI for the REST resource to track audit log for.</p>
     */
    private final String uri;

    /**
     * <p>A flag indicating if exact match for the resource URI is required in order to have the incoming request get
     * audited.</p>
     */
    private final boolean uriExactMatch;

    /**
     * <p>A key in message resource bundle referring to description of action mapped to audited request.</p>
     */
    private final String auditLogAction;

    /**
     * <p>A flag indicating if request data must be saved as a payload.</p>
     */
    private final boolean payload;

    /**
     * <p>A flag indicating if response is in Headwind MDM standard format and should be checked for errors.</p>
     */
    private final boolean checkResponse;

    /**
     * <p>Constructs new <code>ResourceAuditInfo</code> instance. This implementation does nothing.</p>
     */
    ResourceAuditInfo(String method, String uri, boolean uriExactMatch, String auditLogAction, boolean payload, boolean checkResponse) {
        this.method = method;
        this.uri = uri;
        this.uriExactMatch = uriExactMatch;
        this.auditLogAction = auditLogAction;
        this.payload = payload;
        this.checkResponse = checkResponse;
    }

    /**
     * <p>Gets the auditor for the specified request/response chain.</p>
     *
     * @param request an incoming request to be processed.
     * @param response a response to be sent to client.
     * @param chain a filter chain.
     * @return an auditor for the specified request/response chain.
     */
    public ResourceAuditor getResourceAuditor(ServletRequest request, ServletResponse response, FilterChain chain,
                                              String proxyIps, String ipHeader) throws IOException {
        return new ResourceAuditor(auditLogAction, request, response, chain, payload, checkResponse, proxyIps, ipHeader);
    }

    /**
     * <p>Checks if specified request URI matches this audit rule.</p>
     *
     * @param requestUri an URI for the current request being processed by application.
     * @return <code>true</code> if there is a match; <code>false</code> otherwise.
     */
    private boolean matches(String requestMethod, String requestUri) {
        if (!this.method.equalsIgnoreCase(requestMethod)) {
            return false;
        }
        if (this.uriExactMatch) {
            return this.uri.equals(requestUri);
        } else {
            return requestUri.startsWith(this.uri);
        }
    }

    /**
     * <p>Finds the details for audit process (if any) to be performed against specified request URI.</p>
     *
     * @param requestUri an URI for the current request being processed by application.
     * @return the details for audit process to apply to processed request.
     */
    public static Optional<ResourceAuditInfo> findAuditInfo(String requestMethod, String requestUri) {
        return Stream.of(ResourceAuditInfo.values())
                .filter(info -> info.matches(requestMethod, requestUri))
                .findAny();
    }
}
