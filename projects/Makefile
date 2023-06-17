JAR_FILE_NAME=target/projects.jar

clean:
	@mvn clean

build: clean
	@mvn package

install: clean
	@mvn install

run: install
	@java -jar ${JAR_FILE_NAME}
