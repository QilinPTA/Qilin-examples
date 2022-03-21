import driver.PTAOption;
import qilin.core.PTA;
import qilin.pta.tools.CollectionsPTA;
import qilin.util.Stopwatch;

public class Main {

    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.newAndStart("Pointer Analysis");
        new PTAOption().parseCommandLine(args);
        driver.Main.setupSoot();
        PTA pta = new CollectionsPTA();
        pta.run();
        stopwatch.stop();
        System.out.println(stopwatch);
    }
}
