Overlaying a web-fragment
===

Example on how to add web fragments to a JEE application on WildFly.

We will also use Apache Tomcat to check the web fragments-related
aspects on this project, without relying on the WildFly-specific
feature of overlays.

This example contains the following modules:
* `mywar-skinny` - webapp that does not contain anything; we'll use it
to add two overlays to it, one for a servlet and one for security
constraints on the server
* `myear` - this is just an EAR wrappe for `mywar-skinny`
* `myservlet` - this is the JAR that contains the web fragment that defines
the servlet
* `mysecurity` - this is the JAR that contains the web fragment that defines
the security constraints for the servlet
* `mywar` - this is a regular WAR that contains the two JARs above

The plan is to
* Use WildFly to
  * deploy `mywar-skinny` and add `myservlet` and  `mysecurity` as overlays to it
  * deploy `myear` to do the same
* Use Tomcat to deploy `mywar` and verify that the web fragments defined by
`myservlet` and  `mysecurity` work there, too

# Install WildFly

Install [WildFly](https://www.wildfly.org) and start it up:

```shell
$ ~/wildfly-28.0.1.Final/bin/standalone.sh
```

# Adding a servlet to a WAR

Deploy the WAR:

```shell
$ mvn clean install && \
  cp mywar-skinny/target/mywar-skinny-1.0-SNAPSHOT.war \
  ~/wildfly-28.0.1.Final/standalone/deployments/
```

Confirm that the servlet does not yet exist:

```shell
$ curl http://localhost:8080/mywar-skinny-1.0-SNAPSHOT/MyServlet
<html><head><title>Error</title></head><body>Not Found</body></html>
```

Add the overlay with the servlet:

```shell
$ ~/wildfly-28.0.1.Final/bin/jboss-cli.sh --connect
[standalone@localhost:9990 /] deployment-overlay add --name=web-fragment-jar \
  --content=WEB-INF/lib/myservlet-1.0-SNAPSHOT.jar=~/projects/web-fragments-overlays/myservlet/target/myservlet-1.0-SNAPSHOT.jar \
  --deployments=*.war --redeploy-affected
```

Check:

```shell
curl http://localhost:8080/mywar-skinny-1.0-SNAPSHOT/MyServlet
My servlet - unauthenticated
```

# Adding security constraints to a WAR

Deploy the fragment adding security constraint:

```shell
$ ~/wildfly-28.0.1.Final/bin/jboss-cli.sh --connect
[standalone@localhost:9990 /] deployment-overlay add --name=my-security-jar \
  --content=WEB-INF/lib/mysecurity-1.0-SNAPSHOT.jar=~/projects/web-fragments-overlays/mysecurity/target/mysecurity-1.0-SNAPSHOT.jar \
  --deployments=*.war --redeploy-affected
```

Confirm that access is denied without credentials:

```shell
$ curl http://localhost:8080/mywar-skinny-1.0-SNAPSHOT/MyServlet
<html><head><title>Error</title></head><body>Unauthorized</body></html>
```

Add a user with credentials `foo` / `foo` belonging to group `bar` (any role will do,
as we've specified `<role-name>*</role-name>` in the `web-fragment.xml` file):

```shell
$ ~/wildfly-28.0.1.Final/bin/add-user.sh 

What type of user do you wish to add? 
 a) Management User (mgmt-users.properties) 
 b) Application User (application-users.properties)
(a): b

Enter the details of the new user to add.
Using realm 'ApplicationRealm' as discovered from the existing property files.
Username : foo
Password recommendations are listed below. To modify these restrictions edit the add-user.properties configuration file.
 - The password should be different from the username
 - The password should not be one of the following restricted values {root, admin, administrator}
 - The password should contain at least 8 characters, 1 alphabetic character(s), 1 digit(s), 1 non-alphanumeric symbol(s)
Password : 
WFLYDM0098: The password should be different from the username
Are you sure you want to use the password entered yes/no? yes
Re-enter Password : 
What groups do you want this user to belong to? (Please enter a comma separated list, or leave blank for none)[  ]: bar
About to add user 'foo' for realm 'ApplicationRealm'
Is this correct yes/no? yes
Added user 'foo' to file '/path/to/wildfly-28.0.1.Final/standalone/configuration/application-users.properties'
Added user 'foo' to file '/path/to/wildfly-28.0.1.Final/domain/configuration/application-users.properties'
Added user 'foo' with groups bar to file '/path/to/wildfly-28.0.1.Final/standalone/configuration/application-roles.properties'
Added user 'foo' with groups bar to file '/path/to/wildfly-28.0.1.Final/domain/configuration/application-roles.properties'
```

Accessing <http://localhost:8080/mywar-skinny-1.0-SNAPSHOT/MyServlet>, you will be required
to authenticate - using `foo` / `foo` will work.

```shell
$ curl -u foo:foo http://localhost:8080/mywar-skinny-1.0-SNAPSHOT/MyServlet
My servlet - logged in as foo
```

# Using an EAR file

First, undeploy everything:

```shell
$ ~/wildfly-28.0.1.Final/bin/jboss-cli.sh --connect
[standalone@localhost:9990 /] deployment-overlay remove --name=my-security-jar --redeploy-affected
[standalone@localhost:9990 /] deployment-overlay remove --name=web-fragment-jar --redeploy-affected
```

```shell
$ rm ~/wildfly-28.0.1.Final/standalone/deployments/mywar-skinny-1.0-SNAPSHOT.war
```

Now we'll do the same as before, but using the EAR:

```shell
$ mvn clean install && \
  cp myear/target/myear-1.0-SNAPSHOT.ear \
  ~/wildfly-28.0.1.Final/standalone/deployments/
```

Check that our servlet does not yet exist:

```shell
curl http://localhost:8080/mywar/MyServlet
<html><head><title>Error</title></head><body>404 - Not Found</body></html>
```

Add the overlay with the servlet:

```shell
$ ~/wildfly-28.0.1.Final/bin/jboss-cli.sh --connect
[standalone@localhost:9990 /] deployment-overlay add --name=web-fragment-jar \
  --content=/org.example-mywar-skinny-1.0-SNAPSHOT.war/WEB-INF/lib/myservlet-1.0-SNAPSHOT.jar=~/projects/web-fragments-overlays/myservlet/target/myservlet-1.0-SNAPSHOT.jar \
  --deployments=*.ear \
  --redeploy-affected
```

Check that the servlet responds correctly:

```shell
curl http://localhost:8080/mywar/MyServlet
My servlet - unauthenticated
```

Deploy the fragment adding security constraint:

```shell
$ ~/wildfly-28.0.1.Final/bin/jboss-cli.sh --connect
[standalone@localhost:9990 /] deployment-overlay add --name=my-security-jar \
  --content=/org.example-mywar-skinny-1.0-SNAPSHOT.war/WEB-INF/lib/mysecurity-1.0-SNAPSHOT.jar=~/projects/web-fragments-overlays/mysecurity/target/mysecurity-1.0-SNAPSHOT.jar \
  --deployments=*.ear --redeploy-affected
```

Confirm that access is now denied without credentials:

```shell
$ curl http://localhost:8080/mywar/MyServlet
<html><head><title>Error</title></head><body>Unauthorized</body></html>
```

Check that access is now granted with credentials:

```shell
$ curl -u foo:foo http://localhost:8080/mywar/MyServlet
My servlet - logged in as foo
```

# Checking no roles

In fact, also users with no roles will satisfy the `*` constraint. To check, create a
user with credentials `baz` / `baz` belonging to no groups:

```shell
$ ~/wildfly-28.0.1.Final/bin/add-user.sh 

What type of user do you wish to add? 
 a) Management User (mgmt-users.properties) 
 b) Application User (application-users.properties)
(a): b

Enter the details of the new user to add.
Using realm 'ApplicationRealm' as discovered from the existing property files.
Username : baz
Password recommendations are listed below. To modify these restrictions edit the add-user.properties configuration file.
 - The password should be different from the username
 - The password should not be one of the following restricted values {root, admin, administrator}
 - The password should contain at least 8 characters, 1 alphabetic character(s), 1 digit(s), 1 non-alphanumeric symbol(s)
Password : 
WFLYDM0098: The password should be different from the username
Are you sure you want to use the password entered yes/no? yes
Re-enter Password : 
What groups do you want this user to belong to? (Please enter a comma separated list, or leave blank for none)[  ]: 
About to add user 'baz' for realm 'ApplicationRealm'
Is this correct yes/no? yes
Added user 'baz' to file '/Users/milad/wildfly-28.0.1.Final/standalone/configuration/application-users.properties'
Added user 'baz' to file '/Users/milad/wildfly-28.0.1.Final/domain/configuration/application-users.properties'
Added user 'baz' with groups  to file '/Users/milad/wildfly-28.0.1.Final/standalone/configuration/application-roles.properties'
Added user 'baz' with groups  to file '/Users/milad/wildfly-28.0.1.Final/domain/configuration/application-roles.properties'
```

To confirm that the new user has no group, compare it with the other one, `foo`:

```shell
$ tail -n 2 ~/wildfly-28.0.1.Final/standalone/configuration/application-roles.properties
foo=bar
baz=
```

Check that access is granted with the new credentials:

```shell
$ curl -u baz:baz http://localhost:8080/mywar/MyServlet
My servlet - logged in as baz
```

For completeness, this is how to undeploy everything again:

```shell
$ ~/wildfly-28.0.1.Final/bin/jboss-cli.sh --connect
[standalone@localhost:9990 /] deployment-overlay remove --name=my-security-jar --redeploy-affected
[standalone@localhost:9990 /] deployment-overlay remove --name=web-fragment-jar --redeploy-affected
```

```shell
$ rm ~/wildfly-28.0.1.Final/standalone/deployments/myear-1.0-SNAPSHOT.ear
```

Finally, you can stop WildFly by hitting Ctrl-C on its terminal window.

# Using Tomcat (10.1.9)

So far, we've been verifying the overlays functionality on WildFly, together
with the web fragment feature of JEE. Now we'll remove overlays from the picture,
and test the web fragments on Tomcat, just to check that this feature doesn't just
work WildFly, but on any servlet container.

To do that, we will use `mywar`, which includes the two web fragments deployed as JARs
in its `WEB-INF/lib` folder to define a servlet and security constraints
to the servlet.

To do this check, download [Tomcat 10](https://tomcat.apache.org/download-10.cgi),
and start the server:

```shell
$ export JAVA_HOME=`asdf where java` && \
  ~/apache-tomcat-10.1.9/bin/catalina.sh start
```

Deploy the webapp with the servlet and security constraints web fragments,
contained in two JARs packaged with the WAR:

```shell
$ mvn clean install && \
  cp mywar/target/mywar-1.0-SNAPSHOT.war ~/apache-tomcat-10.1.9/webapps/
```

Verify that access is denied without authentication:

```shell
$ curl http://localhost:8080/mywar-1.0-SNAPSHOT/MyServlet 
<!doctype html><html lang="en"><head><title>HTTP Status 401 – Unauthorized</title><style type="text/css">body {font-family:Tahoma,Arial,sans-serif;} h1, h2, h3, b {color:white;background-color:#525D76;} h1 {font-size:22px;} h2 {font-size:16px;} h3 {font-size:14px;} p {font-size:12px;} a {color:black;} .line {height:1px;background-color:#525D76;border:none;}</style></head><body><h1>HTTP Status 401 – Unauthorized</h1><hr class="line" /><p><b>Type</b> Status Report</p><p><b>Description</b> The request has not been applied because it lacks valid authentication credentials for the target resource.</p><hr class="line" /><h3>Apache Tomcat/10.1.9</h3></body></html>
```

Add a user to `~/apache-tomcat-10.1.9/conf/tomcat-users.xml`:

```xml
<tomcat-users>
    ...
    <user username="foo" password="foo" />
</tomcat-users>
```

Verify that access is granted with authentication:

```shell
$ curl -u foo:foo http://localhost:8080/mywar-1.0-SNAPSHOT/MyServlet
My servlet - logged in as foo
```

# References

* <https://docs.wildfly.org/26.1/Admin_Guide.html#Deployment_Overlays>
* <http://wildscribe.github.io/WildFly/18.0/deployment-overlay/index.html>
* <https://www.mastertheboss.com/jbossas/jboss-script/wildfly-how-to-add-an-user/>
* <https://www.mastertheboss.com/jbossas/jboss-security/configuring-http-basic-authentication-with-wildfly/>
* <https://jakarta.ee/xml/ns/jakartaee/#10>
* <https://www.amitph.com/servlet-3-0-web-fragments-and-other-features/>
* <https://www.mastertheboss.com/web/jboss-web-server/how-to-add-a-web-fragment-to-all-applications-deployed-on-wildfly/>
* <https://access.redhat.com/solutions/383393>
* <https://download.oracle.com/otn-pub/jcp/servlet-3_1-fr-eval-spec/servlet-3_1-final.pdf>
* <https://developer.jboss.org/thread/266201>