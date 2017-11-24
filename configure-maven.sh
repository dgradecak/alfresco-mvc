#!/bin/bash

sed -i~ "/<servers>/ a\
<server>\
  <id>Gradecak-Repo</id>\
  <username>${GRA_USERNAME}</username>\
  <password>${GRA_PASS}</password>\
</server>" /usr/share/maven/conf/settings.xml