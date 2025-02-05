start:
	./mvnw spring-boot:run -Dspring-boot.run.arguments="--no-interaction"
stop:
	./mvnw spring-boot:stop -Dspring-boot.run.arguments="--no-interaction"
restart:
	./mvnw spring-boot:stop -Dspring-boot.run.arguments="--no-interaction"
	./mvnw spring-boot:run -Dspring-boot.run.arguments="--no-interaction"
