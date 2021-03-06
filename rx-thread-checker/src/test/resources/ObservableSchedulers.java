/*
 *  Copyright (c) 2017-2018 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ubercab.rxthreadchecker.testdata;

import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;
import com.ubercab.rxthreadchecker.qual.*;
import org.checkerframework.checker.guieffect.qual.*;


/**
 * Combine UI effect typing with RX Observable thread typing
 */
public class ObservableSchedulers {

    //Correct usage examples; no type-checking errors should be raised.
    private void safeObservableEffects(
            io.reactivex.@UI Observer<Object> uiEffectfulAction,
            io.reactivex.Observer<Object> safeAction)
    {
        Observable<Object> o = Observable.empty(); //Make a new stream
        o.subscribe(safeAction); //subscribe an effect-free callback
        //Equivalent operations without intermediate variables:
        Observable.empty().subscribe(safeAction);

        Observable<Object> o1 = Observable.empty(); //Make a new stream
        @UIThread Observable<Object> o2 = o1.observeOn(MockAndroidSchedulers.mainThread()); // Specify that it should
        // be observed on the UI thread
        o2.subscribe(uiEffectfulAction); //subscribe a callback with UI effect
        //Equivalent operations without intermediate variables:
        Observable.empty().observeOn(MockAndroidSchedulers.mainThread()).subscribe(uiEffectfulAction);

        Observable<Object> o3 = Observable.empty(); //Make a new stream
        @UIThread Observable<Object> o4 = o3.observeOn(MockAndroidSchedulers.mainThread()); // Specify that it should be observed on the UI thread
        @UIThread Observable<Object> o5 = o4.doOnEach(safeAction); //Perform some effect-free operations (e.g. map, filter, etc.)  o5 should have inferred @UIThread annotation
        o5.subscribe(uiEffectfulAction);
        //Equivalent operations without intermediate variables:
        Observable.empty().observeOn(MockAndroidSchedulers.mainThread()).doOnEach(safeAction).subscribe(uiEffectfulAction);
    }

    //Incorrect usage, type-checking errors should be raised
    private void unsafeObservableEffects(io.reactivex.@UI Observer<Object> uiEffectfulAction)
    {
        Observable<Object> o1 = Observable.empty(); //Make a new stream
        @CompThread Observable<Object> o2 = o1.observeOn(Schedulers.computation()); //Specify that it should be observed on a computation thread
        // :: error: (rxjava.thread.violation)
        o2.subscribe(uiEffectfulAction);//subscribe a callback with UI effect

        //Equivalent operations without intermediate variables:
        // :: error: (rxjava.thread.violation)
        Observable.empty().observeOn(Schedulers.computation()).subscribe(uiEffectfulAction);

        Observable<Object> o3 = Observable.empty(); //Make a new stream
        // :: error: (rxjava.thread.violation)
        o3.subscribe(uiEffectfulAction); //subscribe a callback with UI effect

        //Equivalent operations without intermediate variables:
        // :: error: (rxjava.thread.violation)
        Observable.empty().subscribe(uiEffectfulAction);
    }

    private void safeObservableMergeEffects(io.reactivex.@UI Observer<Object> uiEffectfulAction)
    {
        Observable<Object> o1 = Observable.empty();
        @UIThread Observable<Object> o2 = o1.observeOn(MockAndroidSchedulers.mainThread());
        @UIThread Observable<Object> o3 = o1.observeOn(MockAndroidSchedulers.mainThread());
        Observable.merge(o2, o3).subscribe(uiEffectfulAction);
    }

    private void unsafeObservableMergeEffects(io.reactivex.@UI Observer<Object> uiEffectfulAction)
    {
        Observable<Object> o1 = Observable.empty();
        @UIThread Observable<Object> o2 = Observable.empty().observeOn(MockAndroidSchedulers.mainThread());
        // :: error: (rxjava.thread.violation)
        Observable.merge(o1, o2).subscribe(uiEffectfulAction);
    }
}
