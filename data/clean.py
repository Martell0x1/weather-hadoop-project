import pandas as pd

# -------- CONFIG --------
FILE1 = "file1.csv"
FILE2 = "file2.csv"
OUTPUT = "cleaned_weather.csv"
# ------------------------

def extract_temperature(tmp_value):
    """
    Extract temperature from TMP field.
    TMP format from NOAA ISD: "000000,1,N,9"
    First value = temperature in tenths of °C.
    Missing temperature = '+9999'
    """
    if pd.isna(tmp_value):
        return None

    parts = str(tmp_value).split(",")
    if len(parts) == 0:
        return None

    raw = parts[0]  # First field should be temperature

    # Remove non-digit chars
    raw = raw.replace("+", "").replace("\"", "").strip()

    if raw in ["9999", "99999", ""]:
        return None

    # Convert tenths of °C → standard °C
    try:
        return float(raw) / 10.0
    except:
        return None


def main():
    print("Loading CSV files...")
    df1 = pd.read_csv(FILE1)
    df2 = pd.read_csv(FILE2)

    print("Merging...")
    df = pd.concat([df1, df2], ignore_index=True)

    print("Cleaning DATE column...")
    df["DATE"] = pd.to_datetime(df["DATE"], errors="coerce")

    print("Extracting clean TMP values...")
    df["CLEAN_TMP"] = df["TMP"].apply(extract_temperature)

    print("Extracting clean DEW values...")
    df["CLEAN_DEW"] = df["DEW"].apply(extract_temperature)

    print("Removing rows where DATE or CLEAN_TMP are invalid...")
    df = df.dropna(subset=["DATE", "CLEAN_TMP"])

    print("Saving cleaned CSV...")
    df.to_csv(OUTPUT, index=False)

    print("\n✔ DONE: cleaned file saved as:", OUTPUT)


if __name__ == "__main__":
    main()

