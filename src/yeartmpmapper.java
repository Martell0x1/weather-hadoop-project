import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class YearTempMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {

    private final static DoubleWritable tempWritable = new DoubleWritable();
    private Text yearKey = new Text();

    // helper: parse CSV line respecting quotes (simple, not full RFC)
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

        // skip possible header lines
        String low = line.toLowerCase();
        if (low.startsWith("station,") || low.startsWith("station\t") || low.contains("station,date")) {
            return;
        }

        ArrayList<String> cols = parseCSV(line);

        // TMP expected index (based on provided sample header): 13 (0-based)
        int idxDate = 1;
        int idxTmp = 13;

        // safety: require at least up to TMP
        if (cols.size() <= idxTmp) {
            // not enough columns -> skip
            return;
        }

        String dateStr = cols.get(idxDate).trim();
        String tmpStr = cols.get(idxTmp).trim();

        // common sentinel values: empty or 99999 or 9999 etc.
        if (tmpStr.isEmpty() || tmpStr.equals("99999") || tmpStr.equals("9999") || tmpStr.equals("999.9")) {
            return; // no valid temperature
        }

        // try parse year from date: expected "YYYY-MM-DD ..." or "YYYY-MM-DD HH:MM:SS"
        String year = null;
        if (dateStr.length() >= 4) {
            String maybeYear = dateStr.substring(0,4);
            if (maybeYear.matches("\\d{4}")) {
                year = maybeYear;
            }
        }
        if (year == null) return;

        // try parse temperature as double (tmp may be integer or decimal)
        double temp;
        try {
            temp = Double.parseDouble(tmpStr);
        } catch (NumberFormatException e) {
            // sometimes TMP may include extra chars -> try remove non-numeric (conservative)
            String cleaned = tmpStr.replaceAll("[^0-9\\.-]", "");
            if (cleaned.isEmpty()) return;
            try {
                temp = Double.parseDouble(cleaned);
            } catch (NumberFormatException e2) {
                return;
            }
        }

        yearKey.set(year);
        tempWritable.set(temp);
        context.write(yearKey, tempWritable);
    }
}
