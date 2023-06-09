Overlaying a web-fragment
===

Example on how to add web fragments to a JEE application on WildFly 28.

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

Add a user foo/foo belonging to group `bar`:

```shell
$ wildfly-28.0.1.Final/bin/add-user.sh 

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

Check:

```shell
curl http://localhost:8080/mywar/MyServlet
<html><head><title>Error</title></head><body>404 - Not Found</body></html>
```

Add the overlay with the servlet:

```shell
$ ~/wildfly-28.0.1.Final/bin/jboss-cli.sh --connect
[standalone@localhost:9990 /] deployment-overlay add --name=web-fragment-jar \
  --content=/org.example-mywar-skinny-1.0-SNAPSHOT.war/WEB-INF/lib/myjar-1.0-SNAPSHOT.jar=~/projects/web-fragments-overlays/myjar/target/myjar-1.0-SNAPSHOT.jar \
  --deployments=myear-1.0-SNAPSHOT.ear \
  --redeploy-affected
```

Check:

```shell
curl http://localhost:8080/mywar/MyServlet
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