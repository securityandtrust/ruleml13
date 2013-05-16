#R-CoRe Demonstration For the RuleML'13 Conference

We propose here three ways to run the demonstration, from easy to advanced, requiring each time a bit more tools to run.

## Run the Demonstration (easy)
1. Download the demonstration launcher [here](http://maven.kevoree.org/ruleml/lu/snt/rcore/lu.snt.rcore.demo.runner/1.0/lu.snt.rcore.demo.runner-1.0.jar)
2. Open a `Terminal` or `Console`
3. `cd` into the folder where the launcher has been downloaded
4. Launch this executable Jar:```java -jar lu.snt.rcore.demo.runner-1.0.jar```
5. You can then enjoy the demonstration
6. To end the demo, simply close the `Monitor Screen` that displays the graph.


## Run the Demonstration (medium) 
For the medium, you need to have Apache Maven installed on your computer.

1. Clone the git repository
2. Open a console and `cd` into the `lu.snt.rcore.demo.runner` folder
3. Type `mvn key:run`
4. Enjoy the Demo
5. Just `ctrl + C` to end the demo.


## Run the Demonstration (advanced)

1. Download a **Kevoree Editor** in version **1.9.0** ([here](http://maven.kevoree.org/release/org/kevoree/tools/org.kevoree.tools.ui.editor.standalone/1.9.0/org.kevoree.tools.ui.editor.standalone-1.9.0.jar))
2. Download a **Kevoree Runtime** in version **1.9.0** ([here](http://maven.kevoree.org/release/org/kevoree/platform/org.kevoree.platform.standalone/1.9.0/org.kevoree.platform.standalone-1.9.0.jar))
3. Download the model of the demonstration ([here](https://github.com/securityandtrust/ruleml13/raw/master/lu.snt.rcore.demo.runner/src/main/kevs/bootstrap.kev))
4. Launch the Kevoree Runtime (`java -jar`)
5. Launch the Kevoree Editor (`java -jar` or double clic) and open the model of the demonstration you just downloaded
6. Clic on the node (black box) and just click `push`. The model of the demonstration will then be sent to the runtime.
