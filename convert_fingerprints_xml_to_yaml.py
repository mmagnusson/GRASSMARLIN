import os
import yaml
import xml.etree.ElementTree as ET

SRC = os.path.join(os.path.dirname(__file__), 'GM3/data/fingerprint')
DST = os.path.join(os.path.dirname(__file__), '../pyLot/pylot/fingerprints')
os.makedirs(DST, exist_ok=True)

def xml_to_dict(elem):
    d = {elem.tag.lower(): {}}
    # Add attributes
    for k, v in elem.attrib.items():
        d[elem.tag.lower()][k.lower()] = v
    # Add children
    for child in elem:
        c = xml_to_dict(child)
        for k, v in c.items():
            if k in d[elem.tag.lower()]:
                if not isinstance(d[elem.tag.lower()][k], list):
                    d[elem.tag.lower()][k] = [d[elem.tag.lower()][k]]
                d[elem.tag.lower()][k].append(v)
            else:
                d[elem.tag.lower()][k] = v
    # Add text
    if elem.text and elem.text.strip():
        d[elem.tag.lower()]['_text'] = elem.text.strip()
    return d

def main():
    files = [f for f in os.listdir(SRC) if f.endswith('.xml')]
    count = 0
    for f in files:
        xml_path = os.path.join(SRC, f)
        tree = ET.parse(xml_path)
        root = tree.getroot()
        d = xml_to_dict(root)
        yml_name = os.path.splitext(f)[0].replace(' ', '_') + '.yaml'
        yml_path = os.path.join(DST, yml_name)
        with open(yml_path, 'w', encoding='utf-8') as out:
            yaml.dump(d, out, sort_keys=False, allow_unicode=True)
        count += 1
        print(f'Converted: {f} -> {yml_name}')
    print(f'Conversion complete. {count} files processed.')

if __name__ == '__main__':
    main() 