#!/usr/bin/env python3
from pathlib import Path
import re
from collections import defaultdict
root = Path(__file__).parent / 'src/main/resources/db/migration'
versions = defaultdict(list)
for path in root.glob('V*.sql'):
    m = re.match(r'V(\d+)__.+\.sql$', path.name)
    if m:
        versions[int(m.group(1))].append(path.name)
duplicates = {v:names for v,names in versions.items() if len(names)>1}
if duplicates:
    for v,names in sorted(duplicates.items()):
        print(f'Duplicate Flyway version V{v}: {", ".join(names)}')
    raise SystemExit(1)
print(f'OK: {len(versions)} unique Flyway versions; highest V{max(versions) if versions else 0}')
