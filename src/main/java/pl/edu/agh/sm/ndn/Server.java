package pl.edu.agh.sm.ndn;

import net.named_data.jndn.*;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.transport.TcpTransport;
import net.named_data.jndn.util.Blob;

import java.util.concurrent.Executors;

public class Server implements AutoCloseable {
    private final Face face;
    private final KeyChain keyChain;
    private long counter = 1;
    private boolean shouldStop = false;

    public static void main(String[] args) {
        Server server = new Server();
    }

    public Server() {
        try {
            this.face = new ThreadPoolFace(Executors.newScheduledThreadPool(1), new TcpTransport(),
                    new TcpTransport.ConnectionInfo("localhost", 6363));
            Name prefixName = new Name("/pl/edu/agh/sm/demo");
            this.keyChain = new KeyChain();

            this.face.setCommandSigningInfo(this.keyChain, this.keyChain.getDefaultCertificateName());
            this.face.registerPrefix(prefixName, new OnInterestCallback() {
                @Override
                public void onInterest(Name prefix, Interest interest, Face face, long interestFilterId, InterestFilter filter) {
                    String content = String.format("Hello from ndn server! Request number: %d", counter);
                    counter++;

                    Data data = new Data(interest.getName());
                    MetaInfo meta = new MetaInfo();
                    meta.setFreshnessPeriod(5000);
                    data.setMetaInfo(meta);

                    data.setContent(new Blob(content.getBytes()));

                    try {
                        keyChain.sign(data, keyChain.getDefaultCertificateName());
                        face.putData(data);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, new OnRegisterFailed() {
                @Override
                public void onRegisterFailed(Name prefix) {
                    System.out.println("jNDN: failed to register prefix");
                    shouldStop = true;
                }
            });
            System.out.println("Prefix registered");

            while (!shouldStop) {
                System.out.println("LOOP");
                face.processEvents();
                Thread.sleep(500);
            }
            close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        System.out.println("Closing face");
        face.shutdown();
    }
}