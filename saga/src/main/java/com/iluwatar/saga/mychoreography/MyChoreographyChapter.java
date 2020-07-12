package com.iluwatar.saga.mychoreography;

public interface MyChoreographyChapter {

    MySaga execute(MySaga saga);

    String getName();

    MySaga process(MySaga saga);

    MySaga rollback(MySaga saga);
}
