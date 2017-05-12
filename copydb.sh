rm ./pilsremote.db
adb -d shell "rm /sdcard/pilsremote.db"
adb -d shell "run-as nl.svia.pilsremote cp /data/data/nl.svia.pilsremote/databases/pilsremote.db /sdcard/pilsremote.db"
adb pull /sdcard/pilsremote.db
echo "Saved to ./pilsremote.db"

