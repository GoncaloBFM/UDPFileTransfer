package t1;

import org.junit.Test;

/**
 * Created by Andre Pontes on 13/10/2015.
 */
public class FTPUdpServerSRTest {
    @Test
    public void test() throws Exception {
        String filename = "testfile_original";
        String[] clientArgs = new String[]{filename, "localhost", "512", "5000", "20"};
        String[] serverArgs = new String[]{"50"};
        for(int i = 0; i < 20; i++){
            try {
                Thread serverThread = new Thread(){
                    @Override
                    public void run(){
                        try {
                            FTUdpServer.main(serverArgs);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                Thread clientThread = new Thread(){
                    @Override
                    public void run(){
                        try {
                            FTUdpClientSR.main(clientArgs);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                if(!serverThread.isAlive()){
                    System.out.println("server started");
                    serverThread.start();
                    Thread.sleep(3000);
                }

                System.out.println("client started");
                clientThread.start();

                while(clientThread.isAlive())
                    Thread.yield();



            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }
        }
    }
}
