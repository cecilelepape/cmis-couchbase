This project is a simple demo to show how to build Content Management system flexible and scalable using Couchbase as the repository for metadata. For more information check on http://cecilelepape.blogspot.fr/2015/09/flexible-and-scalable-content.html

The main folder of this project is chemistry-opencmis-server-couchbase. It contains all the CMIS implementation of a server using Couchbase to store metadata, and either local storage or AWS S3 storage.

Get the code from chemistry-opencmis-server-couchbase and edit the properties you can find in src/main/webapp/WEB-INF/classes/repository.properties

- Edit the login with the credentials that must match your Web server's one. I use Apache Tomcat so I edited the conf/tomcat-users.xml to added test/test user.
- Edit the location of your couchbase server (localhost by default)
- Create a bucket to store your metadata in Couchbase. By default the bucket must be named cmismeta.
- If you're testing a local storage on your filesystem, update the storage path
- If you're testing S3 storage, comment the local storage properties and uncomment the aws storage properties,then update them with your own S3 bucket informations.

Compile the code and deploy the war in your Web server as couchbase.war

To create folders or documents into your Apache Chemistry server, you can use the workbench provided by Apache Chemistry. You can found the instructions in here : http://chemistry.apache.org/java/developing/tools/dev-tools-workbench.html

To connect to the repository, set the URL to http://localhost:8080/couchbase/atom11, set the credentials (test/test by default) and the repository name (test).
