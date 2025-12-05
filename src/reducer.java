import java.io.IOException;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class YearStatsReducer extends Reducer<Text, DoubleWritable, Text, Text> {

    @Override
    protected void reduce(Text key, Iterable<DoubleWritable> values, Context context)
            throws IOException, InterruptedException {

        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        double sum = 0.0;
        double sumSq = 0.0;
        long count = 0;

        for (DoubleWritable v : values) {
            double t = v.get();
            if (t > max) max = t;
            if (t < min) min = t;
            sum += t;
            sumSq += t * t;
            count++;
        }

        if (count == 0) return;

        double avg = sum / count;
        double variance = (sumSq / count) - (avg * avg);
        if (variance < 0 && variance > -1e-10) variance = 0; // numerical safety
        double stddev = Math.sqrt(Math.max(0.0, variance));

        // Output as CSV string: MAX,MIN,AVG,STDDEV,COUNT
        String out = String.format("%.4f,%.4f,%.4f,%.4f,%d", max, min, avg, stddev, count);
        context.write(key, new Text(out));
    }
}
