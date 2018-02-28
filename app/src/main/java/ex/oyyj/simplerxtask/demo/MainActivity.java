package ex.oyyj.simplerxtask.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ex.oyyj.simplerxtask.library.SimpleRxTask;

public class MainActivity extends AppCompatActivity {

    private SimpleRxTask<Integer, String> mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doSomething();
    }


    private void doSomething() {

        mTask = new SimpleRxTask<Integer, String>() {
            @Override
            protected String doInBackground() throws Exception {
                long start = System.currentTimeMillis();
                String pi = calculatePi(1000);

                long cost = System.currentTimeMillis() - start;
                Log.i(TAG, "calculate cost:" + cost + " ms");
                return pi;
            }

            @Override
            protected void onSuccess(String aDouble) {
                Log.i(TAG, "result:" + aDouble);
            }
        };

        mTask.start();
    }

    /**
     * 计算z精度下的PI值
     *
     * @param n 给定的位数
     * @return
     */
    private String calculatePi(int n) {
        BigDecimal pi = new BigDecimal(0);
        for (int k = 0; k < n; k++) {
            BigDecimal a0 = new BigDecimal(16).pow(k);
            BigDecimal a1 = new BigDecimal(4).divide(new BigDecimal(8*k+1), 20, RoundingMode.HALF_UP);
            BigDecimal a2 = new BigDecimal(2).divide(new BigDecimal(8*k+4), 20, RoundingMode.HALF_UP);
            BigDecimal a3 = new BigDecimal(1).divide(new BigDecimal(8*k+5), 20, RoundingMode.HALF_UP);
            BigDecimal a4 = new BigDecimal(1).divide(new BigDecimal(8*k+6), 20, RoundingMode.HALF_UP);
            BigDecimal a5 = a1.subtract(a2).subtract(a3).subtract(a4);
            BigDecimal a6 = BigDecimal.ONE.divide(a0, 20, RoundingMode.HALF_UP);
            BigDecimal tmp = a5.multiply(a6);
            pi = tmp.add(pi);
        }

        return pi.toString();
    }
}
