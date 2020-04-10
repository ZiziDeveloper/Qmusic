
cmake_minimum_required(VERSION 3.4.1)

project(soundTouchProject)

include_directories(soundtouch/include)
include_directories(soundtouch/SoundTouch)

file(GLOB_RECURSE soundtouch_native_sources soundtouch/*)


add_library( # Sets the name of the library.
             libSound

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${soundtouch_native_sources}
              )


#target_link_libraries( # Specifies the target library.
#                       libSound)