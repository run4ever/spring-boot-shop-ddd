package fr.training.samples.spring.shop.importjob;

import org.springframework.batch.core.SkipListener;

public class MySkipListener<T, S> implements SkipListener {
    @Override
    public void onSkipInRead(Throwable throwable) {

    }

    @Override
    public void onSkipInWrite(Object o, Throwable throwable) {

    }

    @Override
    public void onSkipInProcess(Object o, Throwable throwable) {

    }
}
