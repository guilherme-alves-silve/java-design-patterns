package com.iluwatar.saga.mychoreography;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class MySaga {

    private int pos;
    private boolean forward;
    private boolean finished;
    private final List<MyChapter> chapters;

    public MySaga() {
        this.pos = 0;
        this.forward = true;
        this.finished = false;
        this.chapters = new ArrayList<>();
    }

    public MyResult getResult() {
        if (finished) {
            return forward
                    ? MyResult.FINISHED
                    : MyResult.ROLLBACK;
        }

        return MyResult.PROGRESS;
    }

    public MySaga chapter(final String name) {
        chapters.add(new MyChapter(name));
        return this;
    }

    public MySaga setInValue(Object value) {
        if (chapters.isEmpty()) {
            return this;
        }

        chapters.get(chapters.size() - 1).setInValue(value);
        return this;
    }

    public Object getCurrentValue() {
        return chapters.get(pos).getInValue();
    }

    public MySaga setCurrentValue(Object value) {
        chapters.get(pos).setInValue(value);
        return this;
    }

    public MySaga setCurrentStatus(MyChapterStatus status) {
        chapters.get(pos).setStatus(status);
        return this;
    }

    public MySaga setFinished(final boolean finished) {
        this.finished = finished;
        return this;
    }

    boolean isFinished() {
        return finished;
    }

    int forward() {
        return ++pos;
    }

    int back() {
        this.forward = false;
        return --pos;
    }

    MyChapter getCurrent() {
        return chapters.get(pos);
    }

    boolean inRange() {
        return pos >= 0 && pos < chapters.size();
    }

    boolean isCurrentSuccess() {
        return chapters.get(pos).isSuccess();
    }

    public class MyChapter {

        private final String name;
        private Object value;
        private MyChapterStatus status;

        public MyChapter(final String name) {
            this.name = name;
        }

        public void setInValue(final Object value) {
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public boolean isSuccess() {
            return MyChapterStatus.SUCCESS == status;
        }

        public Object getInValue() {
            return value;
        }

        public void setStatus(final MyChapterStatus status) {
            this.status = status;
        }
    }

    public static MySaga create() {
        return new MySaga();
    }

    public enum MyChapterStatus {
        INIT, SUCCESS, ROLLBACK
    }

    public enum MyResult {
        PROGRESS, FINISHED, ROLLBACK
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MySaga.class.getSimpleName() + "[", "]")
                .add("pos=" + pos)
                .add("forward=" + forward)
                .add("finished=" + finished)
                .add("chapters=" + chapters)
                .toString();
    }
}
