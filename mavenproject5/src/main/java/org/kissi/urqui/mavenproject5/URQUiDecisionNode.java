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
package org.kissi.urqui.mavenproject5;

import static org.forgerock.openam.auth.node.api.SharedStateConstants.PASSWORD;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;

import javax.inject.Inject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.AbstractDecisionNode;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.Node;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.CoreWrapper;
import org.forgerock.openam.idrepo.ldap.IdentityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iplanet.sso.SSOException;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.spi.InvalidPasswordException;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdRepoException;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

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
    public interface Config {
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
        logger.debug("myCustomAuthNode started");
        AMIdentityRepository idrepo = coreWrapper.getAMIdentityRepository(
                coreWrapper.convertRealmPathToRealmDn(context.sharedState.get(REALM).asString()));
        logger.debug("AMIdentityRepository claimed");

        NameCallback nameCallback = new NameCallback("notused");
        nameCallback.setName(context.sharedState.get(USERNAME).asString());

        PasswordCallback passwordCallback = new PasswordCallback("notused", false);

        passwordCallback.setPassword(getPassword(context));
        logger.debug("NameCallback and PasswordCallback set");
        Callback[] callbacks = new Callback[]{nameCallback, passwordCallback};
        boolean success = false;
        JsonValue newState = context.sharedState.copy();
        JsonValue newTransientState = context.transientState.copy();
        try {
            logger.debug("authenticating {} ", nameCallback.getName());
            success = validateUrqui(callbacks);
            //   success = idrepo.authenticate(callbacks);
            //    boolean isActive = coreWrapper.getIdentity(nameCallback.getName(),
            //            context.sharedState.get(REALM).asString()).isActive();
            //    success = success && isActive;
            //        success = true;
        } catch (InvalidPasswordException e) {
            logger.warn("invalid password error");
            // Ignore. Success is already false!

        }
        return goTo(success).replaceSharedState(newState)
                .replaceTransientState(newTransientState).build();
    }

    private char[] getPassword(TreeContext context) throws NodeProcessException {
        String password = context.transientState.get(PASSWORD).asString();
        if (password == null) {
            logger.error("Password is null, note this field is not stored across multiple requests");
            throw new NodeProcessException("Unable to authenticate");
        }
        return password.toCharArray();
    }

    private boolean validateUrqui(Callback[] callbacks) throws InvalidPasswordException {
        //  String rqui = x.getName();
        //   String urqui = "536028";

        NameCallback nameCallback = (NameCallback) callbacks[0];
        PasswordCallback passwordcallback = (PasswordCallback) callbacks[1];

        String rqui = nameCallback.getName();

       
        
        String urqui = new String(passwordcallback.getPassword());
 
       
         
        try {
            String result = sendPost(rqui + urqui);
 
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
    public static String sendPost(String ciphertext) throws Exception {

        final String USER_AGENT = "Mozilla/5.0";
        final String url = "http://urqui.net/validate/hostValidate2.cgi";

        int len = ciphertext.length();

        URL obj = new URL(url);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // HttpsURLConnection  con = (HttpsURLConnection) obj.openConnection();
        //  con.setSSLSocketFactory(new UrquiSSL("/urquinetCA.crt").mySocketFactory());
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
        OutputStream wr = null;
        try {
            wr = con.getOutputStream();
            wr.write(ciphertext.getBytes());

            wr.flush();
        } finally {
            if (wr != null) {
                try {
                    wr.close();
                } catch (IOException logOrIgnore) {
                }
            }
        }

        int responseCode = con.getResponseCode();
        ByteArrayOutputStream bais;

        DataInputStream in = new DataInputStream(con.getInputStream());
        byte[] f = new byte[256];
        bais = new ByteArrayOutputStream();
        int n;
        while ((n = in.read(f)) > 0) {
            bais.write(f, 0, n);
        }

        String result = new String(f);

        //   byte[] result = UrquiCommon.hex2bin(bais.toString());
        return result;
    }
}
