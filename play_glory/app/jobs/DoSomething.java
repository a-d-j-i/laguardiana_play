package jobs;

import play.jobs.Job;

public class DoSomething extends Job<String> {

    long d;

    public DoSomething(long d) {
        this.d = d;
    }

    @Override
    public String doJobWithResult() throws Exception {
        Thread.sleep(d);
        return "DONE:" + d;
    }

}
