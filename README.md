
open2jam
========
open source emulator of [O2Jam](http://o2jam.wikia.com/wiki/O2Jam).

It is written in a combination of java, OpenGL and FMOD Ex, with the objective of being able to play in whatever platform you are.

We don't have an official roadmap, but we aim to:

*   Have it working on major platforms (Windows, Linux, Mac OS X).
*   Being able to play any OJN/OJM and BMS files.
*   Skinnable game interface
*   Multiplayer similar to o2jam.

FMOD Sound System, copyright © Firelight Technologies Pty, Ltd., 1994-2012.


Current Features
----------------

* Supports OJN/OJM files and BMS files.
    * Partially supports BGA for BMS files. (Image backgrounds and movie files using VLC)
* Works on Windows, Mac and Linux.
    * Tested on several Windows laptops.
    * Tested on Mac OS X 10.8 with Java 1.6.
    * Tested on Ubuntu 12.04.
* Music directory selection
    * You can put songs in multiple directories. open2jam keeps track of each of them separately.
* Adjustable KEY/BGM volume.
* Auto-play mode.
* Display and audio latency compensation. [Howto](https://github.com/open2jamorg/open2jam/blob/master/docs/autosync.md)
    * Related discussions:
        * [Audio Latency and Autosyncing](https://github.com/open2jamorg/open2jam/pull/20)
        * [Display lag and audio latency - Some information and problems](https://github.com/open2jamorg/open2jam/issues/8)
* Optional, configurable alternative judgment method: "Timed Judgment," which judges notes by milliseconds rather than beats.
* Local matching - play with friends (powered by [partytime](https://github.com/dtinth/partytime)). [Demo Video](http://www.youtube.com/watch?v=UaZu2jVOdS8)
* Speed type: Hi-Speed, xR-Speed, W-Speed, Regul-Speed


License
-------

All the code here is distributed under the terms of the Artistic License 2.0.  
For more details, see the full text of the license in the file LICENSE.


Running from source
-------------------

Once you have cloned/downloaded the code, you can either you use [ant](http://ant.apache.org/) on the root directory to compile the source, or you can use [netbeans](http://netbeans.org/), which is our editor of choice here.

Once compiled, the jar will be in the dist/ directory, to run, `cd` to the `dist` directory, and then run `$ java -jar open2jam.jar`.


