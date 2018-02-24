set JXMLTVDIR=C:\Program Files\jxmltv
set HOMEDIR=%CD%
pushd "%JXMLTVDIR%"

java -jar "jxmltv.jar" be.skynetnl -output.filename="%HOMEDIR%\..\xml\xmltv_be_sky.xml"
popd