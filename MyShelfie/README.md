## Run the Web Server using Docker
Follow this steps if you want the run the Web Server application as a containerized service.
### Create/Update the jar file:
1. Open the Maven project in IntelliJ.
2. In the right-hand Maven Projects sidebar, expand your project and locate the Lifecycle section. Double-click the package goal.
3.  Once the build is complete, IntelliJ will create the JAR file in the target directory of your project: ```PSP54-1.0-SNAPSHOT.jar```.

**Warning**: Make sure to have the jar file in the target directory before to proceed.

### Create the Docker Image:
```
docker build -t web-server .
```
### Create and run the container:
```
 docker run -d -p 59090:59090 -p 1099:1099 --name web-server web-server
```
Now the Web Server is running in background container !

### Stop the container:
```
docker container stop web-server
```
### Run the container the next time:
The next time you want to run container it's enough to run:
```
docker container start web-server
```