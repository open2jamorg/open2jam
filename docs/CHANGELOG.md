Alpha 7
=======

After more than a year, this release brings in a lot of new features:

* Sound system changed from OpenAL to FMOD Ex. There should be less audio issue in this release.
* Better support for BMS, OJM and OJN, and SM file formats.
* Partial support for background animations in BMS files.
* [Audio and display latency settings and syncing.](autosync.md)
* More accurate calculation of health points, based on the selected difficulty.
* Regul-Speed modifier.
* Local matching — play open2jam with your friends.
* BMS exporter.
* Lot of bug fixes.

Regul-Speed
-----------
This speed modifier is borrowed from beatmaniaIIDX and makes the notes scroll at 150BPM * speed multiplier,
regardless of actual BPM changes present in the chart. This is equivalent to the C-mod in StepMania.

BMS Exporter
------------
You can convert any playable files to BMS, by right-clicking at the song you want to export in the selection list,
and choose __Convert to BMS.__

The song file, along with keysounds and background music will be exported with the converted BMS file.

The converted files will appear in the "converted" directory inside your open2jam folder.