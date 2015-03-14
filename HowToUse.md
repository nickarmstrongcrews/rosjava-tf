# Introduction #

(assuming you have rosjava already installed)

  * Use svn to checkout read-only copy of source.
  * Import projects into Eclipse
  * Download dependencies (below), add them as referenced jars in Eclipse project
  * change any IP addresses to match your set up (in VizDemo, android\_tf\_tools.demo)
  * build
  * run a local roscore on a PC or laptop (not on android)
  * run VizDemo
  * run android\_tf\_tools.demo with your phone attached

# Dependencies #

Download the following jars:
  * jgrapht.jar ( http://www.jgrapht.org ), and included TGGraphLayout.jar
  * libvecmath.jar ( part of Java 3D, sudo apt-get )