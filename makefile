

all:
	make compile
	make run

compile:
	javac src/Exceptions/*.java src/BPTree/*.java src/DB/*.java -d bin/

run:
	java -classpath ./bin/ DB.DBAppTest

clean:
	rm -r data/*
