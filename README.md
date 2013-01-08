file-share
==========

A web application to upload and download files.  This could be done already, but I couldn't find it :).  This is a Java EE application that uses JSF and JAX-RS/EJB.  To build and deploy:

    cd fileShare.war
    export JBOSS_HOME=<your JBoss dir>
    ant deploy

Perhaps someday I'll mavenize it.  To view the main page, just go to `http://<jboss server>:<port>/fileShare`.
