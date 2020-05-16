package monitor.implementation.shell;

import monitor.model.Configuration;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LogFileReplayer {

    public static final String YYYYMMDD = "yyyy-MM-dd-";
    public static final String LOG_FORMAT = "HH:mm:ss,S";
    private final String nowYYYYMMDD = new SimpleDateFormat(YYYYMMDD).format(new Date());

    /**
     * replay an existing file
     *
     * @param timeDilation 0.5 would play back at double speed, 2 is half speed
     */
    public void replay(String outputFileName, String inputFileName, double timeDilation) throws IOException, InterruptedException {
        List<String> lines = load(inputFileName);
        File outputFile = new File(outputFileName);

        if (!outputFile.exists()) {
            String directories = outputFileName.substring(0, outputFileName.lastIndexOf(File.separatorChar));
            File dirs = new File(directories);
            if (!dirs.exists()) {
                if (!dirs.mkdirs()) {
                    throw new IOException("Could not create directories: " + directories);
                }
            }
            outputFile.createNewFile();
        }

        PrintWriter p = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));

        long logFileStartTime = findFirstTime(lines);
        long replayStart = new Date().getTime();
        StringBuilder linesWithSameTime = new StringBuilder();
        for (int x = 0; x < lines.size(); x++) {
            long logTime = fileLogMillis(lines.get(x));
            if (logTime > 0) {
                long millisWhenDue = (long) ((logTime - logFileStartTime) * timeDilation);
                long millisIntoPlayback = new Date().getTime() - replayStart;
                if (millisWhenDue > millisIntoPlayback) {
                    Thread.sleep(millisWhenDue - millisIntoPlayback);
                }
                linesWithSameTime.append(lines.get(x));
                p.println(linesWithSameTime.toString());
                p.flush();
                linesWithSameTime = new StringBuilder();
            } else {
                linesWithSameTime.append(lines.get(x)).append('\n');
            }
        }
        if (linesWithSameTime.length() > 0) {
            p.println(linesWithSameTime.toString());
        }
        p.close();
    }

    long findFirstTime(List<String> lines) {
        for (int x = 0; x < lines.size(); x++) {
            long time = fileLogMillis(lines.get(x));
            if (time > 0) {
                return time;
            }
        }
        return 0;
    }

    List<String> load(String inputFile) throws IOException {
        List<String> lines = new ArrayList<String>();
        File file = new File(inputFile);
        BufferedReader reader = new BufferedReader(new FileReader(Configuration.getInstance().getDataDirectory() + "/" + file));
        String nextLine = reader.readLine();
        while (nextLine != null) {
            lines.add(nextLine);
            nextLine = reader.readLine();
        }
        reader.close();
        return lines;
    }

    // converts "17:08:54,808, ERROR blah..." into milliseconds
    long fileLogMillis(String line) {
        long fileLogMillis = 0;
        try {
            String time = nowYYYYMMDD + line.substring(0, "17:08:54,808".length());
            SimpleDateFormat format = new SimpleDateFormat(YYYYMMDD + LOG_FORMAT);
            fileLogMillis = format.parse(time).getTime();
        } catch (Exception ignore) {
        }
        return fileLogMillis;
    }


}
