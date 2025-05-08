
.DEFAULT_GOAL := build-run

clean:
	./gradlew clean

build:
	./gradlew build

install:
	./gradlew clean install

run:
	./gradlew bootRun

test:
	./gradlew test

lint:
	./gradlew checkstyleMain

lint-tests:
	./gradlew checkstyleTest

install-dist:
	./gradlew installDist

build-run: build run

sonar:
	./gradlew sonar

report:
	./gradlew jacocoTestReport

do-all: clean build run

preCommit: clean lint lint-tests
.PHONY: build
