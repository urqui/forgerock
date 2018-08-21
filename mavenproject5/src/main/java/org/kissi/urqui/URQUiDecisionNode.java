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

import com.iplanet.sso.SSOException;
import com.sun.identity.authentication.spi.InvalidPasswordException;
import com.sun.identity.idm.IdRepoException;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import javax.inject.Inject;
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
    }

    private final CoreWrapper coreWrapper;
    private final Logger logger = LoggerFactory.getLogger("amAuth");

    /**
     * Guice constructor.
     *
     * @param coreWrapper A core wrapper instance.
     */
    @Inject
    public URQUiDecisionNode(CoreWrapper coreWrapper) {
        this.coreWrapper = coreWrapper;
    }

    @Override
    public Action process(TreeContext context) throws NodeProcessException {
        logger.debug("URQUiDecisionNode started");

        String username = context.sharedState.get(USERNAME).asString();
        String urqui = context.transientState.get("URQUI").asString();
        logger.debug("authenticating {} ", username);
        boolean isActive;
       
        try {
            isActive = coreWrapper.getIdentity(username, context.sharedState.get(REALM).asString()).isActive();
        } catch (IdRepoException | SSOException e) {
            throw new NodeProcessException(e);
        }
 
        return goTo(validateUrqui(username, urqui) && isActive).build();
    }


    private boolean validateUrqui(String username, String urqui) {
        //  String  = x.getName();
         
 
        try {
            String result = sendPost(username + urqui);
  
            if (result.substring(14, 16).equals("ok")) {

                return true;
                
            } else {
                throw new InvalidPasswordException(result);

                //   return result.substring(14, 16).equals("ok"); // System.out.println("ok");
                //  System.out.println("bad");
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        return false;
    }

    // HTTP POST request
    private static String sendPost(String ciphertext) throws Exception {

        final String USER_AGENT = "Mozilla/5.0";
        final String url = "https://validate.urqui.net";

        int len = ciphertext.length();
 
        URL obj = new URL(url);

       // HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        HttpsURLConnection  con = (HttpsURLConnection) obj.openConnection();
          con.setSSLSocketFactory(new UrquiSSL("/valurquinetCA.crt").mySocketFactory());
          
         
        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setRequestProperty("Content-Type", "application/text");
        con.setRequestProperty("Content-Length", Integer.toString(len));

        // combine byte arrays into 1.
        //  byte[] postData = new byte[len];
        // copy a to result
        // System.arraycopy(iv, 0, postData, 0, iv.length);
        // copy b to result
        //  System.arraycopy(id, 0, postData, iv.length, id.length);
        // copy b to result
        //  System.arraycopy(ciphertext, 0, postData, iv.length + id.length, ciphertext.length);
        // Send post request
        con.setDoOutput(true);
        try (OutputStream wr = con.getOutputStream()) {
            wr.write(ciphertext.getBytes());

            wr.flush();
        }

        ByteArrayOutputStream bais;

        DataInputStream in = new DataInputStream(con.getInputStream());
        byte[] f = new byte[256];
        bais = new ByteArrayOutputStream();
        int n;
        while ((n = in.read(f)) > 0) {
            bais.write(f, 0, n);
        }

        //   byte[] result = UrquiCommon.hex2bin(bais.toString());
        return new String(f);
    }
}
