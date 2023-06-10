Overlaying a web-fragment
===

Example on how to add web fragments to a JEE application on WildFly 28.

WildFly must be up and running:

```shell
$ ~/wildfly-28.0.1.Final/bin/standalone.sh
```

# Adding a servlet

Deploy the "empty" WAR:

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
  --content=WEB-INF/lib/myjar-1.0-SNAPSHOT.jar=~/projects/web-fragments-overlays/myjar/target/myjar-1.0-SNAPSHOT.jar \
  --deployments=*.war --redeploy-affected
```

Check:

```shell
curl http://localhost:8080/mywar-skinny-1.0-SNAPSHOT/MyServlet
Overlay servlet
```

# Adding security constraints

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
Overlay servlet
```

# Using an EAR file

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
  --content=/org.example-mywar-skinny-1.0-SNAPSHOT.war/WEB-INF/lib/myjar-1.0-SNAPSHOT.jar=~/projects/web-fragments-overlays/myjar/target/myjar-1.0-SNAPSHOT.jar \
  --deployments=*.ear \
  --redeploy-affected
```

Check that the servlet responds correctly:

```shell
curl http://localhost:8080/mywar/MyServlet
Overlay servlet
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
Overlay servlet
```

# Checking no roles

In fact, also no roles at all will do
To check that at least a role is necessary, create a user with credentials `baz` / `baz`
belonging to no groups:

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

To confirm:

```shell
$ tail -n 2 ~/wildfly-28.0.1.Final/standalone/configuration/application-roles.properties
foo=bar
baz=
```

Check that access is not granted with these credentials:

```shell
$ curl -u baz:baz http://localhost:8080/mywar/MyServlet
Overlay servlet
```

# Using Tomcat (10.1.9)

Start the server:

```shell
$ export JAVA_HOME=`asdf where java`
$ ~/apache-tomcat-10.1.9/bin/catalina.sh start
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

Add a user to `~/apache-tomcat-10.1.9//conf/tomcat-users.xml`:

```xml
<tomcat-users>
    ...
    <user username="foo" password="foo" />
</tomcat-users>
```

Verify that access is granted with authentication:

```shell
$ curl -u foo:foo http://localhost:8080/mywar-1.0-SNAPSHOT/MyServlet
Overlay servlet
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