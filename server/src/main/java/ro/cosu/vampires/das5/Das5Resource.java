package ro.cosu.vampires.das5;

import ro.cosu.vampires.server.resources.AbstractResource;

public class Das5Resource extends AbstractResource {

    public Das5Resource() {
        super(Type.DAS5);

    }

    @Override
    public void onStart() throws Exception {

        //submit via sbatch/srun and save job id

        //load job no from squeue
    }

    @Override
    public void onStop() throws Exception {
        //call scancel

    }

    @Override
    public void onFail() throws Exception {

    }
}
