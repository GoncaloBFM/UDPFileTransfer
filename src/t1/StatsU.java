package t1;

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

    public static void addRTTSample(long sampleRTT){
        devRTT = devRTT * BETA_LEFT + Math.abs(sampleRTT - estimatedRTT) * BETA;
        estimatedRTT = estimatedRTT * ALPHA_LEFT + sampleRTT * ALPHA;
    }

    public static int getOptimalTimeout(){

        //System.out.println((int) (estimatedRTT + 4 * devRTT));
        return 100; //(int) (estimatedRTT + 4 * devRTT);
    }

    public static int getOptimalWindowSize(){
        double estimatedValue = (estimatedRTT / transmissionTime);
        if (estimatedValue > windowSize) {
            windowSize ++;
        } else if (estimatedValue < windowSize) {
            windowSize = (int) estimatedValue;
        }
        return windowSize;
    }

    public static void addTransmissionTimeSample(int transmissionTimeSample) {
        transmissionTime = (int) Math.ceil(transmissionTime * THETA_LEFT + transmissionTimeSample * THETA);
    }

}
