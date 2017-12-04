#!/bin/bash
find 'res' -name 'gl_gocn_*.png' | while read fname
do
  echo "===remove and copy==="${fname}
  cp -f "./res/drawable/justforcn.png" "./res/drawable"/${fname##*/}
  rm -f "$fname";
done

echo "===============change png success "
