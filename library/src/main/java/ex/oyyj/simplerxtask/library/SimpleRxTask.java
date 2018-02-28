package ex.oyyj.simplerxtask.library;

import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;


/**
 *  @brief :
 *
 * 一个用RxJava2实现的Task，继承AsyncTask主线程、子线程分工明确和免接口回调的优点，并改进AsyncTask以下问题：
 * 1.无法自动捕获和提示异常
 * 2.cancel()有时无效
 * 3.不用关心不同版本AsyncTask的具体串行或并行实现
 *
 *
 *
 * 线程分工：
 * doInBackground()在子线程执行
 *
 * 以下方法会在主线程回调
 * onPrepare()
 * onProgress()
 * onSuccess()
 * onError()
 * onCancelled()
 *
 */
public abstract class SimpleRxTask<PROGRESS, RESULT> {
    private final PublishSubject<PROGRESS> mProgressObservable = PublishSubject.create();
    private final PublishSubject<String> mPrepareObservable = PublishSubject.create();
    private final PublishSubject<String>  mCanceledObservable = PublishSubject.create();
    private volatile Disposable mTaskSubscription;
    private Disposable mProgressSubscription;
    private Disposable mPrepareSubscription;
    private Disposable mCanceledSubscription;

    private volatile boolean mCancelled;
    protected static final String TAG = "SimpleRxTask";

    public void start() {
        if (isRunning())
            throw new IllegalStateException("already started");

        final SimpleRxTask<PROGRESS, RESULT> current = this;

        if (mCanceledSubscription == null) {
            mCanceledSubscription = mCanceledObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) throws Exception {
                        onCancelled();
                    }
                });
        }

        if (mPrepareSubscription == null) {
            mPrepareSubscription = mPrepareObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        onPrepare();
                    }
                });
        }

        if (mProgressSubscription == null)
            mProgressSubscription = mProgressObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<PROGRESS>() {
                    @Override
                    public void accept(PROGRESS progress) throws Exception {
                        // on progress
                        if (!mProgressSubscription.isDisposed()) {
                            current.onProgress(progress);
                        }
                    }
                });

        // on prepare
        mPrepareObservable.onNext("do prepare");
        mTaskSubscription = Single
            .create(new SingleOnSubscribe<RESULT>() {
                @Override
                public void subscribe(SingleEmitter<RESULT> singleEmitter) throws Exception {
                    try {
                        RESULT result = doInBackground(); //不能返回null !
                        if (result == null) {
                            throw new NullPointerException("doInBackground return result is null!");
                        }
                        singleEmitter.onSuccess(result);

                    } catch (Exception e) {
                        if (!mTaskSubscription.isDisposed()) {
                            singleEmitter.onError(e);
                        }
                    }
                }
            })
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .unsubscribeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<RESULT>() {
                @Override
                public void accept(RESULT result) throws Exception {
                    //on success
                    current.onSuccess(result);
                    mTaskSubscription = null;
                    mProgressSubscription = null;
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable error) throws Exception {
                    // on error
                    current.onError(error);
                }
            });
    }

    public boolean isRunning() {
        return mTaskSubscription != null && !mTaskSubscription.isDisposed();
    }

    public void cancel() {
        mCancelled = true;
        if (mTaskSubscription != null)
            mTaskSubscription.dispose();

        if (mProgressSubscription != null)
            mProgressSubscription.dispose();

        mCanceledObservable.onNext("do cancel");
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    @WorkerThread
    public void publishProgress(@NonNull PROGRESS progress) {
        mProgressObservable.onNext(progress);
    }

    @WorkerThread
    protected @NonNull abstract RESULT doInBackground() throws Exception;

    @MainThread
    protected void onPrepare() {
    }

    @MainThread
    protected void onProgress(@NonNull PROGRESS progress) {
    }


    @MainThread
    protected void onSuccess(@NonNull RESULT result) {
    }

    @MainThread
    protected void onError(@NonNull Throwable error) {
    }

    protected void onCancelled() {
    }

}