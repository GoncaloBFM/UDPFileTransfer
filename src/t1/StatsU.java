package t1;

/**
 * Created by Andre Pontes on 13/10/2015.
 */
public abstract class StatsU {

    private static final int START_TIMEOUT = 3000;

    private static final double ALPHA = 0.125;
    private static final double BETA = 0.25;
    private static final double THETA = 0.25;

    private static final double ALPHA_LEFT = 1 - ALPHA;
    private static final double BETA_LEFT = 1 - BETA;
    private static final double THETA_LEFT = 1 - THETA;

    private static double estimatedRTT = START_TIMEOUT;
    private static double devRTT;

    private static int transmissionTime = 10;

    public static void addRTTSample(int sampleRTT){
        devRTT = devRTT * BETA_LEFT + Math.abs(sampleRTT - estimatedRTT) * BETA;
        estimatedRTT = estimatedRTT * ALPHA_LEFT + sampleRTT * ALPHA;
    }

    public static int getOptimalTimeout(){
        return (int) (estimatedRTT + 4 * devRTT);
    }

    public static int getOptimalWindowSize(){
        return getOptimalTimeout() / transmissionTime + 1;
    }

    public static void addTransmissionTimeSample(int transmissionTimeSample) {
        transmissionTime = (int) Math.ceil(transmissionTime * THETA_LEFT + transmissionTimeSample * THETA);
    }
}
