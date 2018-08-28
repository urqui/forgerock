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
import com.sun.identity.idm.IdRepoException;
import com.sun.identity.sm.RequiredValueValidator;
import com.sun.identity.idm.AMIdentity;
import static com.sun.identity.sm.SMSEntry.bundle;
import javax.inject.Inject;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.core.CoreWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import static org.forgerock.openam.auth.node.api.Action.send;
import javax.security.auth.callback.NameCallback;
/**
 * A node that decides if the RQUi is on the user profile.
 *
 * <p>
 * Expects 'username' and 'password' fields to be present in the shared
 * state.</p>
 */
@Node.Metadata(outcomeProvider = AbstractDecisionNode.OutcomeProvider.class,
        configClass = RQUiAttributeNode.Config.class)
public class RQUiAttributeNode extends SingleOutcomeNode {

    /**
     * Configuration for the RQUi decision node
     */
    interface Config {

        @Attribute(order = 100, validators = {RequiredValueValidator.class})
        String rquiAttributeName();
    }

    private final CoreWrapper coreWrapper;
    private final Config config;
    private final Logger logger = LoggerFactory.getLogger("amAuth");

    /**
     * Guice constructor.
     *
     * @param coreWrapper A core wrapper instance.
     */
    @Inject
    public RQUiAttributeNode(@Assisted Config config, CoreWrapper coreWrapper) {
        this.config = config;
        this.coreWrapper = coreWrapper;

    }

    @Override
    public Action process(TreeContext context) {
        logger.debug("RQUiAttributeNode started");

        config.rquiAttributeName();
        AMIdentity userIdentity = coreWrapper.getIdentity(context.sharedState.get(USERNAME).asString(), context.sharedState.get(REALM).asString());

        Map<String, Set> map = new HashMap<String, Set>();

        Set<String> values = new HashSet<String>();
        values.add("RQUi");
        map.put(config.rquiAttributeName(), values);

        try {
            userIdentity.setAttributes(map);
            userIdentity.store();
        } catch (IdRepoException e) {
            logger.error(" Error storing profile atttibute '{}' ", e);
        } catch (SSOException e) {
            logger.error("Node exception", e);
        }
        return  goToNext().build();
    }
}
