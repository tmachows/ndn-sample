package pl.edu.agh.ndntest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import net.named_data.jndn.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchData();
            }
        });
    }

    void fetchData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Fetcher fetcher = new Fetcher();
                final String data = fetcher.fetchData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, ">>>RESPONSE: " + data, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    private static class Fetcher {
        private String retVal;
        private Face face;
        volatile private boolean shouldStop = false;

        String fetchData() {
            try {
                face = new Face("18.197.199.137");
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
}
