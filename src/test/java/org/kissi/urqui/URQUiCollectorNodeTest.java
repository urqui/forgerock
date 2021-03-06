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
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.forgerock.json.JsonValue.*;
import static org.forgerock.json.test.assertj.AssertJJsonValueAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.PasswordCallback;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.ExternalRequestContext.Builder;
import org.forgerock.openam.auth.node.api.TreeContext;
import org.forgerock.util.i18n.PreferredLocales;
import org.testng.annotations.Test;



/**
 * A node which collects a password from the user via a password callback.
 *
 * <p>Places the result in the shared state as 'password'.</p>
 */

public class URQUiCollectorNodeTest   {

    @Test
    public void testProcessWithNoCallbacksReturnsASingleCallback() {
        // Given
        URQUiCollectorNode node = new URQUiCollectorNode();
        JsonValue sharedState = json(object(field("initial", "initial")));
        PreferredLocales preferredLocales = mock(PreferredLocales.class);
        ResourceBundle resourceBundle = new MockResourceBundle("URQUi");
        given(preferredLocales.getBundleInPreferredLocale(any(), any())).willReturn(resourceBundle);

        // When
        Action result = node.process(getContext(sharedState, preferredLocales, emptyList()));

        // Then
        assertThat(result.outcome).isEqualTo(null);
        assertThat(result.callbacks).hasSize(1);
        assertThat(result.callbacks.get(0)).isInstanceOf(PasswordCallback.class);
        assertThat(((PasswordCallback) result.callbacks.get(0)).getPrompt()).isEqualTo("URQUi");
        assertThat((Object) result.sharedState).isNull();
        assertThat(sharedState).isObject().containsExactly(entry("initial", "initial"));
    }

    @Test
    public void testProcessWithCallbacksAddsToState() {
        URQUiCollectorNode node = new URQUiCollectorNode();
        JsonValue sharedState = json(object(field("initial", "initial")));
        PasswordCallback callback = new PasswordCallback("prompt", false);
        callback.setPassword("secret".toCharArray());
        Action result = node.process(getContext(sharedState, new PreferredLocales(), singletonList(callback)));
        assertThat(result.outcome).isEqualTo("outcome");
        assertThat(result.callbacks).isEmpty();
        assertThat(result.sharedState).isObject().contains("initial", "initial");
        assertThat(result.transientState).isObject().contains("URQUi", "secret");
        assertThat(sharedState).isObject().containsExactly(entry("initial", "initial"));
    }

    private TreeContext getContext(JsonValue sharedState, PreferredLocales preferredLocales,
                                   List<? extends Callback> callbacks) {
        return new TreeContext(sharedState, new Builder().locales(preferredLocales).build(), callbacks);
    }

    static class MockResourceBundle extends ResourceBundle {
        private final String value;

        MockResourceBundle(String value) {
            this.value = value;
        }

        @Override
        protected Object handleGetObject(String key) {
            return value;
        }

        @Override
        public Enumeration<String> getKeys() {
            return null;
        }
    }
}

