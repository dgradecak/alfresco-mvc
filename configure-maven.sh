#!/bin/bash

sed -i~ "/<servers>/ a\
<server>\
  <id>private-repo</id>\
  <username>${GRA_USERNAME}</username>\
  <password>${GRA_PASS}</password>\
</server>" /usr/share/maven/conf/settings.xml