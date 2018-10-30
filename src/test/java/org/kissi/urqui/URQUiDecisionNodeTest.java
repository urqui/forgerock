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

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.forgerock.json.JsonValue.*;
import static org.forgerock.json.test.assertj.AssertJJsonValueAssert.assertThat;
import static org.forgerock.openam.auth.node.api.SharedStateConstants.USERNAME;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.MockitoAnnotations.initMocks;

import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import javax.net.ssl.HttpsURLConnection;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext.Builder;
import org.forgerock.openam.auth.node.api.NodeProcessException;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.CoreWrapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

    @Spy
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

        given(coreWrapper.convertRealmPathToRealmDn(any())).willReturn("org=name");
        given(coreWrapper.getAMIdentityRepository(any())).willReturn(identityRepository);
        given(coreWrapper.getIdentity(eq("bob"), any())).willReturn(amIdentity);
        given(amIdentity.isActive()).willReturn(true);
        given(amIdentity.getAttribute(any())).willReturn(Collections.singleton("RQUi"));
        given(config.rquiAttributeName()).willReturn("RQUi");

    }

    @Test
    public void testProcessWithGoodURQUi() throws NodeProcessException, IOException {

        String inputStreamText = "jonathan123456ok";
        InputStream inStream = new ByteArrayInputStream(inputStreamText.getBytes());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        Mockito.doReturn(con).when(node).getHttpsURLConnection(any());
        given(con.getInputStream()).willReturn(inStream);
        given(con.getOutputStream()).willReturn(outStream);

        JsonValue sharedState = json(object(field(USERNAME, "bob")));
        JsonValue transientState = json(object(field("URQUi", "GoodURQUi")));
        
        Action result = node.process(getContext(sharedState, transientState));
        assertThat(result.callbacks).isEmpty();
        assertThat(result.outcome).isEqualTo("true");
        assertThat(sharedState).isObject().containsExactly(entry(USERNAME, "bob"));
        assertThat(transientState).isObject().containsExactly(entry("URQUi", "GoodURQUi"));
    }

    @Test
    public void testProcessWithBadURQUi() throws NodeProcessException, IOException {


        String inputStreamText = "jonathan123456bad";
        InputStream inStream = new ByteArrayInputStream(inputStreamText.getBytes());
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        Mockito.doReturn(con).when(node).getHttpsURLConnection(any());
        given(con.getInputStream()).willReturn(inStream);
        given(con.getOutputStream()).willReturn(outStream);

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
