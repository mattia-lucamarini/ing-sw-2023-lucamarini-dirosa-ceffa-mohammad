## Run the Web Server using Docker
Follow this steps if you want the run the Web Server application as a containerized service.
### Create/Update the jar file:
1. Open the Maven project in IntelliJ.
2. In the right-hand Maven Projects sidebar, expand your project and locate the Lifecycle section. 
3. Double-click the package goal.
4. Once the build is complete, IntelliJ will create the JAR file in the target directory of your project: ```server.jar```. Move it into the directory: ```deliverables/jar```.

**Warning**: Make sure to have the jar file in the ```deliverables/jar``` directory before to proceed.

### Create the Docker Image:
```
docker build -t web-server .
```
### Create and run the container:
```
 docker run -d -p 59090:59090 -p 1099:1099 --name web-server web-server
```
Now the Web Server is running in a background container !

Notice: if you want to run it not in background, drop '-d'.

### Stop the container:
```
docker container stop web-server
```
### Run the container the next time:
The next time you want to run the container it's enough to run:
```
docker container start web-server
```