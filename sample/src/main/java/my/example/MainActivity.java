package my.example;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {


    // CR Code Review

    // TODO To Do

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //LOGGER.info("MainActivity::onCreate");
        //LOGGER.info("AndroidLibrary.process: {}", AndroidLibrary.process(this));
        //LOGGER.info("JavaLibrary.process: {}", JavaLibrary.process(this));

        new Thread() {
            @Override
            public void run() {
                try {
                    Object object = "";
                } catch (Exception e) {

                }

                "".length();
            }
        }.start();
    }

}
