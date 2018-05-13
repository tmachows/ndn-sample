package pl.edu.agh.sm.ndnserver;

import net.named_data.jndn.*;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;
import net.named_data.jndn.util.Blob;

import java.io.IOException;

public class Server {

    public static void main(String[] args) {
        Face face = new Face("localhost");
        try {
            KeyChain keyChain = buildKeyChain(face);
            face.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        try {
            pingNdnDaemon(face);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Name prefix = new Name("/pl/edu/agh/sm/demo");
        OnInterestCallback onInterestCallback = createOnInterestCallback();
        OnRegisterFailed onRegisterFailed = createOnRegisterFailed();
        try {
            face.registerPrefix(prefix, onInterestCallback, onRegisterFailed);
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                face.processEvents();
                Thread.sleep(100);
            } catch (IOException | EncodingException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static KeyChain buildKeyChain(Face face) throws SecurityException {
        MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
        MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);
        KeyChain keyChain = new KeyChain(identityManager);
        try {
            keyChain.getDefaultCertificateName();
        } catch (SecurityException e) {
            keyChain.createIdentityAndCertificate(new Name("/test/identity"));
            keyChain.getIdentityManager().setDefaultIdentity(new Name("/test/identity"));
        }
        return keyChain;
    }

    private static void pingNdnDaemon(Face face) throws IOException {
        face.expressInterest(new Name("/localhost/nfd/status/general"),
                (interest, data) -> System.out.println("Ping response received: " + data.getContent().toString()),
                interest -> System.out.println("Error: ping timeout"));
    }

    private static OnInterestCallback createOnInterestCallback() {
        return (name, interest, face, l, interestFilter) -> {
            Blob blob = new Blob("Hello world!".getBytes());
            Data data = new Data();
            data.setContent(blob);
            try {
                face.putData(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    private static OnRegisterFailed createOnRegisterFailed() {
        return name -> System.out.println("Registration failed for name: " + name.toString());
    }
}
