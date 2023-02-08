
### TESTS

- Generate Jacoco report and push to Coverall

```bash
mvn clean test jacoco:report coveralls:report
```

- Generate Sonar report and push to Coverall

```bash
export SONAR_TOKEN=...
mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=clun_astra-cli
```

### PACKAGING

- Build the project

```
mvn clean install
```

- Run with Agent:

_Make sure you are using a graalVM JDK_

```bash
sdk install java 22.3.1.r17-grl
java --version
```

```
/Library/Java/JavaVirtualMachines/graalvm-ce-java17-21.3.0/Contents/Home/bin/java -agentlib:native-image-agent=config-output-dir=/tmp/native-image -jar target/astra-cli-0.2-shaded.jar token list
```

- Build as a native image

```
mvn clean install -Pnative
```

### PUSH

- Start the `ssh-agent`
```
eval "$(ssh-agent -s)"
```
- Add the ssh key to the agent
```
cd ~/.ssh
ssh-add githubff4j
```
- Update sources
```
find . -type f -name *.DS_Store -ls -delete
git pull
git add -A
git commit -m "delivery"
git push
```
- Release
```
mvn clean -DskipTests -Darguments=-DskipTests release:prepare release:perform
```

### DEPLOY

- Build the project

- Copy `astra-init.sh` on each distribution folder

- Copy the shaded and put it in Java folder, rename to `astra-cli.jar`

- Build in native

```
mvn clean install -Pnative
```

- Chmod everything

- Packaging

```
zip astra-cli-0.2.zip *
zip astra-cli-0.2-linux.zip *
zip astra-cli-0.2-windows.zip *
zip astra-cli-0.2-mac.zip *
tar -czf astra-cli-0.2-brew.tar.gz *
brew create https://github.com/datastax/astra-cli/releases/download/0.2/astra-cli-0.2-homebrew.tar.gz
```

- Edit Releases

```
https://github.com/datastax/astra-cli/releases
```