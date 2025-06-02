
.DEFAULT_GOAL := run-dist

clean:
	./gradlew clean

build:
	./gradlew bootJar

run:
	./gradlew bootRun

test:
	./gradlew test

lint:
	./gradlew checkstyleMain

lint-tests:
	./gradlew checkstyleTest

install-dist:
	./gradlew clean installBootDist

sonar:
	./gradlew sonar

report:
	./gradlew jacocoTestReport

run-dist:
	./build/install/app-boot/bin/app

preCommit: clean lint lint-tests
.PHONY: build
