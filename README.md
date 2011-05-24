
`open2jam`
==========
open source emulator of [O2Jam](http://o2jam.wikia.com/wiki/O2Jam).

It is written in a combination of java,OpenGL and OpenAL, with the objective of being able to play in whatever platform you are.

We don't have an official roadmap, but we aim to:

*   Have it working on major platforms (Windows, Linux, Mac OS X).
*   Being able to play any OJN/OJM and BMS files.
*   Skinnable game interface
*   Multiplayer similar to o2jam.
  

License
-------

All the code here is distributed under the terms of the Artistic License 2.0.  
For more details, see the full text of the license in the file LICENSE.


Running from source
-------------------

Once you have cloned/downloaded the code, you can either you use [ant](http://ant.apache.org/) on the root directory to compile the source, or you can use [netbeans](http://netbeans.org/), which is our editor of choice here.


Once compiled, the jar will be in the dist/ directory, to run type:  
`$ java -jar dist/open2jam.jar`, make sure the lib/native directory is in the current working directory, because it's needed by the application.

