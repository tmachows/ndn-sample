package pl.edu.agh.sm.ndnserver;

import net.named_data.jndn.*;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.util.Blob;

import java.io.IOException;

public class Server {

    public static void main(String[] args) {
        Face face = new Face("localhost");

        Name prefix = new Name("/pl/edu/agh/sm/demo");
        OnInterestCallback onInterestCallback = createOnInterestCallback();
        OnRegisterFailed onRegisterFailed = createOnRegisterFailed();
        try {
            face.registerPrefix(prefix, onInterestCallback, onRegisterFailed);
        } catch (IOException | SecurityException e) {
            e.printStackTrace();
        }
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
