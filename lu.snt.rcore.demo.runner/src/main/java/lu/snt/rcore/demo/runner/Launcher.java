package lu.snt.rcore.demo.runner;/*
* Author : Gregory Nain (developer.name@uni.lu)
* Date : 16/05/13
*/

import com.sun.org.apache.xalan.internal.xsltc.runtime.BasisLibrary;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class Launcher {


    public static void main(String[] args) {

        Launcher launcher = new Launcher();
        launcher.launch();
    }

    public void launch() {
        //Download
        try {
            System.out.println("Downloading Runtime...");
            URL website = new URL("http://maven.kevoree.org/release/org/kevoree/platform/org.kevoree.platform.standalone/1.9.0/org.kevoree.platform.standalone-1.9.0.jar");
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            File runtime = File.createTempFile("runtime" + System.currentTimeMillis(),".jar");
            FileOutputStream fos = new FileOutputStream(runtime);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            fos.close();
            System.out.println("Done.");

            System.out.println("Downloading Bootstrap model...");
            URL bootstrapModel = new URL("https://github.com/securityandtrust/ruleml13/raw/master/lu.snt.rcore.demo.runner/src/main/kevs/bootstrap.1.1.kevs");
            ReadableByteChannel rbcModel = Channels.newChannel(bootstrapModel.openStream());
            File model = File.createTempFile("bootstrap" + System.currentTimeMillis(),".kevs");
            FileOutputStream fos2 = new FileOutputStream(model);
            fos2.getChannel().transferFrom(rbcModel, 0, 1 << 24);
            fos2.close();
            System.out.println("Done.");

            //launch

            List<String> cmds = new ArrayList<String>();
            cmds.add(getJava());
            cmds.add("-Dnode.bootstrap=" + model.getAbsolutePath());

            cmds.add("-jar");
            cmds.add(runtime.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(cmds.toArray(new String[cmds.size()]));
            // java 7 specific
            //processBuilder.inheritIO();
            System.out.println("Starting the platform...");
            final Process process = processBuilder.start();
//        final Process process = Runtime.getRuntime().exec(cmds.toArray(new String[cmds.size()]));
            new Thread() {
                @Override
                public void run() {
                    processStream(process.getErrorStream(), System.err);
                }
            }.start();
            new Thread() {
                @Override
                public void run() {
                    processStream(process.getInputStream(), System.out);
                }
            }.start();
            new Thread() {
                @Override
                public void run() {
                    processStream(System.in, process.getOutputStream());
                }
            }.start();

            Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Hook") {
                public void run() {
                    try {
                        process.destroy();
                    } catch (Exception ignored) {
                    }
                }
            });

            System.exit(process.waitFor());
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private String getJava() {
        String java_home = System.getProperty("java.home");
        return java_home + File.separator + "bin" + File.separator + "java";
    }

    private void processStream(InputStream inputStream, OutputStream outputStream) {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            String line = reader.readLine();
            while (line != null) {
                writer.write(line);
                writer.newLine();
                writer.flush();
                line = reader.readLine();
            }
        } catch (IOException ignored) {
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException ignored) {
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}
