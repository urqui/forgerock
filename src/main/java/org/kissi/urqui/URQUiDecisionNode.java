/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2018 ForgeRock AS.
 */
package org.kissi.urqui;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

import com.google.inject.assistedinject.Assisted;
import com.iplanet.sso.SSOException;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.sm.RequiredValueValidator;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Set;
import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.core.CoreWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A node that decides if the username and password exists in the data store.
 *
 * <p>
 * Expects 'username' and 'password' fields to be present in the shared
 * state.</p>
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = URQUiDecisionNode.Config.class)
public class URQUiDecisionNode extends AbstractDecisionNode {

    /**
     * Configuration for the data store node.
     */
    interface Config {

        @Attribute(order = 100, validators = {RequiredValueValidator.class})
        String rquiAttributeName();
    }

    private Config config;
    private final CoreWrapper coreWrapper;
    private final Logger logger = LoggerFactory.getLogger("amAuth");

    /**
     * Guice constructor.
     *
     * @param coreWrapper A core wrapper instance.
     */
    @Inject
    public URQUiDecisionNode(@Assisted Config config, CoreWrapper coreWrapper) {
        this.config = config;
        this.coreWrapper = coreWrapper;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {

        logger.debug("URQUiDecisionNode started");

        String username = context.sharedState.get(USERNAME).asString();
        String urqui = context.transientState.get("URQUi").asString();
        logger.debug("authenticating {} ", username);
        boolean isActive;
        Set<String> attrs;
        try {
            AMIdentity identity = coreWrapper.getIdentity(username, context.sharedState.get(REALM).asString());
            attrs = identity.getAttribute(config.rquiAttributeName());
            isActive = identity.isActive();
        } catch (IdRepoException | SSOException e) {
            throw new NodeProcessException(e);
        }

        try {
            return goTo(validateUrqui(attrs.iterator().next(), urqui) && isActive).build();
        } catch (CertificateException | NoSuchAlgorithmException | KeyManagementException | IOException e) {
           throw new NodeProcessException(e);
        }
    }


    private boolean validateUrqui(String rqui, String urqui) throws CertificateException, NoSuchAlgorithmException,
            KeyManagementException, IOException {
        String result;
        result = sendPost(rqui + urqui);

        return result.substring(14, 16).equals("ok");
    }

    // HTTP POST request
    private  String sendPost(String cipherText) throws IOException, CertificateException, NoSuchAlgorithmException, KeyManagementException {

        HttpsURLConnection con = getHttpsURLConnection("https://validate.urqui.net");
		
        con.setSSLSocketFactory(new UrquiSSL("/valurquinetCA.crt").mySocketFactory());

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/text");
        con.setRequestProperty("Content-Length", Integer.toString(cipherText.length()));

        con.setDoOutput(true);
        try (OutputStream wr = con.getOutputStream()) {
            wr.write(cipherText.getBytes());

            wr.flush();
        }

        byte[] f = new byte[256];
        int n;
        while ((n = new DataInputStream(con.getInputStream()).read(f)) > 0) {
            new ByteArrayOutputStream().write(f, 0, n);
        }

        return new String(f);
    }
	
	public   HttpsURLConnection getHttpsURLConnection(String url) throws MalformedURLException, IOException {
            
            HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
	
            return con;
	}
	
}
