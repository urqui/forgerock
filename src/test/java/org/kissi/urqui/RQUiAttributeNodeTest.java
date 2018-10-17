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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import com.iplanet.sso.SSOException;
import com.sun.identity.idm.AMIdentity;
import com.sun.identity.idm.AMIdentityRepository;
import com.sun.identity.idm.IdRepoException;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext.Builder;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.openam.core.CoreWrapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A node which collects a password from the user via a password callback.
 *
 * <p>
 * Places the result in the shared state as 'password'.</p>
 */
public class RQUiAttributeNodeTest {

    @Mock
    CoreWrapper coreWrapper;

    @Mock
    AMIdentityRepository identityRepository;

    @Mock
    RQUiAttributeNode.Config config;

    @InjectMocks
    RQUiAttributeNode node;

    @Mock
    AMIdentity amIdentity;

    @BeforeMethod
    public void setup() throws Exception {
        node = null;
        initMocks(this);
        given(coreWrapper.convertRealmPathToRealmDn(any())).willReturn("org=name");
        given(coreWrapper.getAMIdentityRepository(any())).willReturn(identityRepository);
        given(coreWrapper.getIdentity(eq("bob"), any())).willReturn(amIdentity);
        given(amIdentity.isActive()).willReturn(true);
        given(config.rquiAttributeName()).willReturn("theConfigValueYouWant");

    }

    @Test
    public void testProcess() throws IdRepoException, SSOException {

        JsonValue sharedState = json(object(field(USERNAME, "bob")));
        JsonValue transientState = json(object(field("RQUi", "jonathan")));

        Action result = node.process(getContext(sharedState, transientState));

        assertThat(result.callbacks).isEmpty();
        assertThat(sharedState).isObject().containsExactly(entry(USERNAME, "bob"));
        assertThat(transientState).isObject().containsExactly(entry("RQUi", "jonathan"));
        verify(amIdentity, times(1)).store();
    }

    private TreeContext getContext(JsonValue sharedState, JsonValue transientState) {
        return new TreeContext(sharedState, transientState, new Builder().build(), emptyList());
    }
}
