package pl.edu.agh.sm.ndn;


import net.named_data.jndn.*;


public class Client {

    public static void main(String[] args) {
        Fetcher fetcher = new Fetcher();
        String data = fetcher.fetchData();
        System.out.println(">>>RESPONSE: " + data);
    }
}

class Fetcher {
    private String retVal;
    private Face face;
    private boolean shouldStop = false;

    String fetchData() {
        try {
            face = new Face("localhost");
            face.expressInterest(new Name("/pl/edu/agh/sm/demo"),
                    new OnData() {
                        @Override
                        public void
                        onData(Interest interest, Data data) {
                            retVal = data.getContent().toString();
                            shouldStop = true;
                        }
                    },
                    new OnTimeout() {
                        @Override
                        public void onTimeout(Interest interest) {
                            retVal = "ERROR: Timeout";
                            shouldStop = true;
                        }
                    });

            while (!shouldStop) {
                face.processEvents();
                Thread.sleep(500);
            }
            face.shutdown();
            face = null;
            return retVal;
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}