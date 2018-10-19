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
 * Copyright 2017-2018 ForgeRock AS.
 */
package org.kissi.urqui;

import com.iplanet.sso.SSOException;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.forgerock.json.JsonValue.*;
import static org.forgerock.json.test.assertj.AssertJJsonValueAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import org.mockito.Mock;
import static org.mockito.Matchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext.Builder;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.util.i18n.PreferredLocales;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.forgerock.openam.core.CoreWrapper;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdRepoException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.mockito.InjectMocks;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.REALM;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * A node which collects a password from the user via a password callback.
 *
 * <p>
 * Places the result in the shared state as 'password'.</p>
 */
public class URQUiDecisionNodeTest {

    @Mock
    CoreWrapper coreWrapper;

    @Mock
    AMIdentityRepository identityRepository;

    @Mock
    URQUiDecisionNode.Config config;

    @InjectMocks
    URQUiDecisionNode node;

    @Mock
    AMIdentity amIdentity;
	
    @Mock
    HttpsURLConnection con;

    @BeforeMethod
    public void setup() throws Exception {
        node = null;
        initMocks(this);
	 
        String mystr = "jonathan123456ok";
        InputStream stream = new ByteArrayInputStream(mystr.getBytes());
        
	given(node.getHttpsURLConnection("https://validate.urqui.net")).willReturn(con);
         
        given(con.getInputStream()).willReturn(stream);
        given(coreWrapper.convertRealmPathToRealmDn(any())).willReturn("org=name");
        given(coreWrapper.getAMIdentityRepository(any())).willReturn(identityRepository);
        given(coreWrapper.getIdentity(eq("bob"), any())).willReturn(amIdentity);
        given(amIdentity.isActive()).willReturn(true);
        given(amIdentity.getAttribute(any())).willReturn(Collections.singleton("RQUi"));
        given(config.rquiAttributeName()).willReturn("RQUi");

    }

    @Test
    public void testProcessWithGoodURQUi() throws NodeProcessException {

        // node = new RQUiAttributeNode(config,coreWrapper);
        JsonValue sharedState = json(object(field(USERNAME, "bob")));
        JsonValue transientState = json(object(field("URQUi", "GoodURQUi")));
        
        Action result = node.process(getContext(sharedState, transientState));
        assertThat(result.callbacks).isEmpty();
        assertThat(result.outcome).isEqualTo("true");
        assertThat(sharedState).isObject().containsExactly(entry(USERNAME, "bob"));
        assertThat(transientState).isObject().containsExactly(entry("URQUi", "GoodURQUi"));
    }

    @Test
    public void testProcessWithBadURQUi() throws NodeProcessException {

        // node = new RQUiAttributeNode(config,coreWrapper);
        JsonValue sharedState = json(object(field(USERNAME, "bob")));
        JsonValue transientState = json(object(field("URQUi", "BadURQUi")));

        Action result = node.process(getContext(sharedState, transientState));
        assertThat(result.callbacks).isEmpty();
        assertThat(result.outcome).isEqualTo("false");
        assertThat(sharedState).isObject().containsExactly(entry(USERNAME, "bob"));
        assertThat(transientState).isObject().containsExactly(entry("URQUi", "BadURQUi"));
    }

    private TreeContext getContext(JsonValue sharedState, JsonValue transientState) {
        return new TreeContext(sharedState, transientState, new Builder().build(), emptyList());
    }
}
