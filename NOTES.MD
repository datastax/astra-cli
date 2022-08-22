
# Generate the Asset (for now)

```
mvn package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true
cp ./target/astra-shell-0.1-alpha3-shaded.jar ~/.astra/cli/astra-shell.jar
```


 TO DEPLOY:
============
mvn clean install -Dmaven.test.skip=true

cp ./target/astra-shell-0.1-alpha3-shaded.jar ~/.astra/cli/astra-shell.jar
cp ./dist/astra-init.sh ~/.astra/cli/
cp ./dist/astra-install.sh ~/.astra/cli/






sdk, brew, chocolaty

```
- run `astra-install.sh`
- run `astra-autocomplete.bash`
- run `astra version` to validate installation


# NATIVE IMAGE
https://simply-how.com/fix-graalvm-native-image-compilation-issues
https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/Reflection.md
https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/Limitations.md


Run with Agent
java -agentlib:native-image-agent=config-output-dir=. -jar astra-shell-0.3.1-SNAPSHOT-shaded.jar

export PATH=$PATH:"$HOME/.astra/cli"
echo -e "\nexport PATH=$PATH:$HOME/.astra/cli" >> ./dist/astra-init.sh



 TO BUILD:
============
mvn clean install -Dmaven.test.skip=true


 TO BUILD IN NATIVE:
 ======================
(1) you need to install GraalVM (GU)

(2) mvn clean install -Dmaven.test.skip=true -Pnative

# Clean the ZIP
zip -d 0.3.2-alpha2.zip __MACOSX/\* @
zip -d 0.3.2-alpha2.zip \*/.DS_Store







