# SimpleRxTaskDemo
A SimpleRxTask, base on RxJava2, take plase of AsyncTask of Android.

Improvements: 

1. Safe. Catch exceptions internal Compare with AsyncTask.

2. Rubust. Cancel() in main/worker thread while AsyncTask sometimes has bugs with cancel().

3. Consistent. Don't need to care about different implement of AsyncTask in  Android 2.3 ~ 8.0

4. Simple. like : ***SimpleRxTask<PROGRESS, RESULT>*** instead of ***AsyncTask<Params, Progress, Result>***, because you can 
pass ***Params*** in when onPrepare()


Usage:

```

       mTask = new SimpleRxTask<Integer, String>() {

            @Override
            protected void onPrepare() {
                super.onPrepare();
            }

            @Override
            protected String doInBackground() throws Exception {
                long start = System.currentTimeMillis();
                String pi = calculatePi(1000);

                long cost = System.currentTimeMillis() - start;
                Log.i(TAG, "calculate cost:" + cost + " ms");
                return pi;
            }

            @Override
            protected void onProgress(Integer integer) {
                super.onProgress(integer);
            }

            @Override
            protected void onSuccess(String aDouble) {
                Log.i(TAG, "result:" + aDouble);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }
        };

        mTask.start();


```
