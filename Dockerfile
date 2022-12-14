FROM inovatrend/tomcat8-java8
RUN mkdir /usr/local/itms
COPY itms/ /usr/local/itms/
COPY security/ /usr/lib/jvm/java-8-oracle/jre/lib/security/
VOLUME /tmp
RUN mkdir /tmp/credentials
RUN mkdir /opt/newrelic
COPY newrelic/ /opt/newrelic
ADD target/service-backend.war /usr/local/tomcat/webapps/service-backend.war
RUN sh -c 'touch /usr/local/tomcat/webapps/service-backend.war'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -javaagent:/opt/newrelic/newrelic.jar -jar /usr/local/tomcat/webapps/service-backend.war" ]

