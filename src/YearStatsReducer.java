import java.io.IOException;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class MonthTempReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

    @Override
    protected void reduce(Text key, Iterable<DoubleWritable> values, Context context)
            throws IOException, InterruptedException {

        double monthMax = -Double.MAX_VALUE;

        for (DoubleWritable v : values) {
            double t = v.get();
            if (t > monthMax) monthMax = t;
        }

        context.write(key, new DoubleWritable(monthMax));
    }
}
