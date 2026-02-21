import csv
import os

src = 'oui.csv'
dst = os.path.join('..', 'pyLot', 'pylot', 'parser', 'oui.csv')

with open(src, newline='', encoding='utf-8') as infile, open(dst, 'w', encoding='utf-8') as outfile:
    reader = csv.DictReader(infile)
    count = 0
    for row in reader:
        prefix = row['Assignment'].strip().replace('-', '').upper()
        vendor = row['Organization Name'].strip()
        if len(prefix) == 6 and vendor:
            outfile.write(f"{prefix},{vendor}\n")
            count += 1
    print(f"Converted {count} OUI entries to {dst}") 