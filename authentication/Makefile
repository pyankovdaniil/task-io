JAR_FILE_NAME=target/authentication.jar

clean:
	@mvn clean

build: clean
	@mvn package

install: clean
	@mvn install

run: install
	@java -jar ${JAR_FILE_NAME}
