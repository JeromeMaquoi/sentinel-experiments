package be.unamur.snail.core;

import java.util.ArrayList;
import java.util.List;

public class Module {
    protected final List<Stage> preStages = new ArrayList<>();
    protected final List<Stage> stages = new ArrayList<>();
    protected final List<Stage> postStages = new ArrayList<>();
    protected int repetitions = 1;

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public void run(Context context) throws Exception {
        for (Stage stage : preStages) stage.execute(context);
        for (int i = 0; i < repetitions; i++) {
            for (Stage stage : stages) stage.execute(context);
        }
        for (Stage stage : postStages) stage.execute(context);
    }
}
