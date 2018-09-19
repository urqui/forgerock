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

![](https://github.com/urqui/forgerock/blob/master/images/ForgeRock1.png)  

3.  Setup the following configuration for the tree that was just
    created. 

	The RQUi is an anonymous (Does not contain identifying information) userid. This allows a user to distribute it,
	without concern of privacy infringement, and at the same time ensuring only they can be authenticated.
	
	The URQUi, is the authenticating value (Changing password) that is used to authenticate the user.
	
	The "RQUi Decison Node", "RQUi Save Attribute Node", and "URQUi Decision Node", all require an Atrribute configured in the Identities DB, to save the users RQUi value. 
	
	When a User signs on for the first time and their RQUi attribute is blank("RQUi Decision Node"), the User should be allowed to enter their RQUi("RQUi Collector Node") and have it saved for future use("RQUi Save Attribute Node").
	
	Once the Users RQUi as been determined, either by first time entry, or retrieving an existing RQUi from DB, the User can then enter their URQUi(Authenticating Value).
	
	The two values together will determine if their access is allowed(URQUi Decision Node).
	
![](https://github.com/urqui/forgerock/blob/master/images/ForgeRock2.png) 

**Usage**

1.  Log into the Tree that was created in the steps above by going to
    /openam/XUI/\#login&service={{Tree\_Name}}.

2.  Log in with your ForgeRock username.

> ![](https://github.com/urqui/forgerock/blob/master/images/ForgeRock3.png) 

3.  If first time, then register your RQUi (A) or if you\'ve already
    registered, enter URQUi to log in(B). 

<!-- -->

A.  ![](https://github.com/urqui/forgerock/blob/master/images/ForgeRock4.png) 

B.  ![](https://github.com/urqui/forgerock/blob/master/images/ForgeRock5.png) 
