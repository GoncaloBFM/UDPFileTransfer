package t1;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Andre Pontes on 13/10/2015.
 */
public abstract class StatsU {

    private static final int START_TIMEOUT = 3000;
    private static final int START_WINDOW_SIZE = 20;

    private static final double ALPHA = 0.125;
    private static final double BETA = 0.25;
    private static final double THETA = 0.25;

    private static final double ALPHA_LEFT = 1 - ALPHA;
    private static final double BETA_LEFT = 1 - BETA;
    private static final double THETA_LEFT = 1 - THETA;

    private static double estimatedRTT = START_TIMEOUT;
    private static double devRTT;

    private static int transmissionTime = 3;

    private static int windowSize = START_WINDOW_SIZE;
    private static int avgWindowSize = START_WINDOW_SIZE;
    private static int avgWindowSizeCount = 1;

    private static int ackReceived = 0;
    private static int packetsSent = 0;

    private static long millisFileStart;
    private static long millisFileEnd;
    private static long fileLength;

    public static void addRTTSample(long sampleRTT){
        devRTT = devRTT * BETA_LEFT + Math.abs(sampleRTT - estimatedRTT) * BETA;
        estimatedRTT = estimatedRTT * ALPHA_LEFT + sampleRTT * ALPHA;
    }

    public static int getOptimalTimeout(){
        //System.out.println((int) (estimatedRTT + 4 * devRTT));
        return (int) (estimatedRTT + 4 * devRTT);
    }

    public static int getOptimalWindowSize(){
        double estimatedValue = (estimatedRTT / transmissionTime);
        if (estimatedValue > windowSize) {
            windowSize ++;
        } else if (estimatedValue < windowSize) {
            windowSize = (int) estimatedValue;
        }
        return 50;//windowSize;
    }

    public static void addTransmissionTimeSample(int transmissionTimeSample) {
        transmissionTime = (int) Math.ceil(transmissionTime * THETA_LEFT + transmissionTimeSample * THETA);
    }

    public synchronized static void notifyPacketSent(){
        packetsSent++;
    }

    public synchronized static void notifyACKReceived(){
        ackReceived++;
    }

    public static void notifyFileSendStart(long fileLengthByte){
        StatsU.millisFileStart = System.currentTimeMillis();
        StatsU.fileLength = fileLengthByte / 1024;
    }

    public static void notifyFileSendEnd(){
        millisFileEnd = System.currentTimeMillis();
    }

    public static void notifyWindowSizeChange(int newSize){
        windowSize = (windowSize * avgWindowSize + newSize) / (avgWindowSizeCount + 1);
        avgWindowSizeCount++;
    }

    public static Map<String, String> getStats(){
        Map<String, String> map = new LinkedHashMap<>();

        long duration = millisFileEnd - millisFileStart;

        map.put("duration (ms)", Long.toString(duration));
        map.put("file length (kB)", Long.toString(fileLength));
        map.put("bitrate (Kb/s)", Long.toString(fileLength * 1000 / duration));
        map.put("RTT (ms)", Double.toString(estimatedRTT));
        map.put("average window size", Integer.toString(avgWindowSize));

        return map;
    }

}
