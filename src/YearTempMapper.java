import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class MonthTempMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {

    private final static DoubleWritable tempWritable = new DoubleWritable();
    private Text monthKey = new Text();

    // Simple CSV parser
    private ArrayList<String> parseCSV(String line) {
        ArrayList<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                cols.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        cols.add(cur.toString());
        return cols;
    }

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString().trim();
        if (line.isEmpty()) return;

        String low = line.toLowerCase();
        if (low.startsWith("station,") || low.contains("station,date")) return;

        ArrayList<String> cols = parseCSV(line);

        int idxDate = 1; // adjust if your date column is different
        int idxTmp = 13; // adjust to the temperature column

        if (cols.size() <= idxTmp) return;

        String dateStr = cols.get(idxDate).trim(); // e.g., 1916-12-31 09:00:00
        String tmpStr = cols.get(idxTmp).trim();

        // Skip invalid values
        if (tmpStr.isEmpty() || tmpStr.equals("9999") || tmpStr.equals("99999") || tmpStr.equals("999.9")) {
            return;
        }

        double temp;
        try {
            temp = Double.parseDouble(tmpStr.replaceAll("[^0-9\\.-]", "")) / 10.0; // divide by 10 if tenths of degrees
        } catch (NumberFormatException e) {
            return;
        }

        // sanity check
        if (temp < -50 || temp > 60) return;

        // extract month: YYYY-MM
        String month = null;
        if (dateStr.length() >= 7) {
            month = dateStr.substring(0, 7); // "1916-12"
        }
        if (month == null) return;

        monthKey.set(month);
        tempWritable.set(temp);
        context.write(monthKey, tempWritable);
    }
}
