**URQUi**

URQUi integration for ForgeRock\'s \[Identity
Platform\]\[forgerock\_platform\] 6.0 and above. This integration
handles:

1.  Primary or Secondary authentication

2.  RQUi registration.

**Installation**

Copy the .jar file from the ../target directory into the
../web-container/webapps/openam/WEB-INF/lib directory where AM is
deployed. Restart the web container to pick up the new node. The node
will then appear in the authentication trees components palette.

**URQUi Configuration**

1.  Install the URQUi ("Your Key") mobile phone 'app', which creates a
    unique URQUi ID. URQUi's mobile 'app' is user installable. URQUi is
    available from iTunes, BlackBerry World, and Google play. The URQUI
    app is available from URQUi.com for 'not smart' mobile phones.

2.  From "Options" menu, generate a random RQUi ("Our Key"). Non
    confidential.

**ForgeRock Configuration**

1.  Log into your ForgeRock AM console.

2.  Create a new Authentication Tree. 

![](media/image1.png){width="6.5in" height="3.65625in"}

3.  Setup the following configuration for the tree that was just
    created.

![](media/image2.png){width="6.5in" height="3.65625in"}

**Usage**

1.  Log into the Tree that was created in the steps above by going to
    /openam/XUI/\#login&service={{Tree\_Name}}.

2.  Log in with your ForgeRock username.

> ![](media/image3.png){width="6.5in" height="3.65625in"}

3.  If first time, then register your RQUi (A) or if you\'ve already
    registered, enter URQUi to log in(B). 

<!-- -->

A.  ![](media/image4.png){width="6.5in" height="3.65625in"}

B.  ![](media/image5.png){width="6.5in" height="3.65625in"}
