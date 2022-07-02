SET test_jar=..\..\base\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET pckg=info\kgeorgiy\java\advanced\implementor

mkdir "bin"
javac ..\java-solutions\info\kgeorgiy\ja\sinitsyn\implementor\Implementor.java -d bin -cp %test_jar%
cd bin
jar cfm Implementor.jar ..\..\META-INF\MANIFEST.MF info\kgeorgiy\ja\sinitsyn\implementor\*.class %pckg%\*.class
