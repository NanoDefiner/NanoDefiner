all: build_update
	mvn package
standalone: build_update
	mvn package -P standalone
tomcat: build_update
	mvn package -P tomcat
clean:
	mvn clean --fn
run: build_update
	mvn spring-boot:run
debug: build_update
	mvn spring-boot:run -Drun.jvmArguments="-ea"
dep:
	mvn dependency:tree
build_update:
	@echo "Updating version and build information"
	scripts/build_update.sh
