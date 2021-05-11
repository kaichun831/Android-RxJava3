package www.bizpro.com.tw.app.rxjava3;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.reactivestreams.Subscription;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableEmitter;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.CompletableOnSubscribe;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeEmitter;
import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.core.MaybeOnSubscribe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Test6Example();
        eventBreakTest();
    }

    //一般使用
    private void Test1() {
        Observer observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Integer integer) {
                Log.d("KAI", "Next" + integer);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.d("KAI", "onComplete");
            }
        };
        Observable observable = new Observable<Integer>() {

            @Override
            protected void subscribeActual(@NonNull Observer<? super Integer> observer) {
                observer.onNext(1);
                observer.onNext(2);
                observer.onComplete();
            }
        };
        observable.subscribe(observer);
    }

    //Flowable/Subscriber上游背壓(也就是一直傳資料給下游)-方式一
    private void Test2() {
        Flowable.range(0, 10)
                .subscribe(new FlowableSubscriber<Integer>() {
                    Subscription sub;

                    @Override
                    public void onSubscribe(@NonNull Subscription s) {
                        Log.d("KAI", "Start");
                        sub = s;
                        sub.request(1);
                        Log.d("KAI", "Done");
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d("KAI", "Next" + integer);
                        sub.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {
                        Log.d("KAI", "onComplete");
                    }
                });
    }

    //Flowable/Subscriber上游背壓-方式二(需指定背壓方式)
    private void Test3() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
                e.onNext(4);
                e.onComplete();
            }
        }, BackpressureStrategy.BUFFER);
        //需要指定背压策略
    }

    //Single/SingleObserver 模式,適用於單次,僅「成功或失敗」
    private void Test4(){
        //被觀察者
        Single<String> single = Single.create(new SingleOnSubscribe<String>() {
            @Override
            public void subscribe(SingleEmitter<String> e) throws Exception {
                e.onSuccess("test");
                e.onSuccess("test2");//onSuccess 只會調用一次,因此這行不會執行
            }
        });
        //訂閱者
        single.subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(String s) {
                //相当于onNext和onComplete
                Log.d("KAI",  s  );
            }

            @Override
            public void onError(Throwable e) {

            }
        });


    }

    //Completable/CompletableObserver 模式,適用於不管過程,僅「完成或發生錯誤」
    private void Test5(){
        Completable.create(new CompletableOnSubscribe() {//被观察者

            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                e.onComplete();//单一onComplete或者onError
            }

        }).subscribe(new CompletableObserver() {//观察者
            @Override
            public void onSubscribe(Disposable d) {
                //中止
            }

            @Override
            public void onComplete() {
                Log.e("KAI", "onComplete: ");
            }

            @Override
            public void onError(Throwable e) {

            }
        });

    }

    //Maybe/MaybeObserver 模式,適用於可能成功可能失敗
    private void Test6(){
        Maybe<String> maybe = Maybe.create(new MaybeOnSubscribe<String>() {
            @Override
            public void subscribe(MaybeEmitter<String> e) throws Exception {
                e.onSuccess("test");
                //如果已經呼叫成功,則e.onComplete() 無作用,反之
                //僅可呼叫e.onError()

            }
        });

        //訂閱觀察者
        maybe.subscribe(new MaybeObserver<String>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(String s) {
                //跑成功,就不跑完成
                Log.i("KAI", s);
            }

            @Override
            public void onComplete() {
                //跑完就不跑成功
                Log.i("KAI", "onComplete");
            }

            @Override
            public void onError(Throwable e) {

            }
        });

    }
    private void Test6Example(){

        //判断是否登陆
        Maybe.just(isLogin())
                //可能涉及到IO操作，放在子线程
                .subscribeOn(Schedulers.newThread())
                //取回结果传到主线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new MaybeObserver<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Boolean value) {
                        if(value){
                            Log.d("KAI","登入成功");
                        }else{
                            Log.d("KAI","登入失敗");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    //事件調度,可以掛載多件事情
    private void   eventBreakTest(){
        CompositeDisposable mRxEvent = new CompositeDisposable();
        Disposable subscribe = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext("俊俊俊很帅");
                e.onNext("你值得拥有");
                e.onNext("取消关注");
                e.onNext("但还是要保持微笑");
                e.onComplete();
            }
        }).subscribe(
                //調度 「允許」
                new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Throwable {
                        Log.d("KAI","允許執行"+s);
                    }
                },
                //調度 「Error」
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {

                    }
                },
                //調度執行
                new Action() {
                    @Override
                    public void run() throws Throwable {

                    }
                });
        Disposable subscribe2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {
                e.onNext("凱鈞很帅");
                e.onNext("你值得拥有");
                e.onNext("取消关注");
                e.onNext("但还是要保持微笑");
                e.onComplete();
            }
        }).subscribe(
                //調度 「允許」
                new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Throwable {
                        Log.d("KAI","允許執行"+s);
                    }
                },
                //調度 「Error」
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Throwable {

                    }
                },
                //調度執行
                new Action() {
                    @Override
                    public void run() throws Throwable {

                    }
                });
        mRxEvent.add(subscribe);
        mRxEvent.add(subscribe2);
        mRxEvent.clear();
    }
    private boolean isLogin(){
        return  false;
    }
}