package t1;

import org.junit.Test;

/**
 * Created by Andre Pontes on 13/10/2015.
 */
public class FTPUdpServerSRTest {
    @Test
    public void test() throws Exception {
        String[] args = new String[]{"testfile_original", "localhost", "512", "5000", "20"};

        for(int i = 0; i < 20; i++){
            try {
                System.out.println("testing "+ i);
                System.out.println("...............................................");
                System.out.println("...............................................");
                System.out.println("...............................................");
                System.out.println("...............................................");
                System.out.println("...............................................");
                FTUdpClientSR.main(args);
                System.out.println("...............................................");
                System.out.println("...............................................");
                System.out.println("...............................................");
                System.out.println("...............................................");
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
}
