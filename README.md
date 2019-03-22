# Spring Microservices - Terminals Pooling

Client-Server Microservice application demonstrating terminal pooling

# Build
## What you’ll need
* JDK 1.8 or later
* Maven 3.5+

1. Run `mvn clean package`
2. Run Server `java -jar -Dspring.config.additional-location=server.properties terminal-server/target/terminal-server-0.0.1-SNAPSHOT.jar`
3. Run client `java -jar -Dspring.config.additional-location=client.properties 
terminal-client/target/terminal-client-0.0.1-SNAPSHOT.jar`


* **client|server.properties only needed to override defaults**

# Test 
`curl -X PUT http://localhost:5000/v1/terminal/client -H 'Content-Type: application/json'`
