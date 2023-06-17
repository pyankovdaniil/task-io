JAR_FILE_NAME=target/projects.jar

clean:
	@mvn clean

build: clean
	@mvn package

run: build
	@java -jar ${JAR_FILE_NAME}
