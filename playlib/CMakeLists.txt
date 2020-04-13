
cmake_minimum_required(VERSION 3.4.1)

#cpp目录下的文件聚合为DIRSRCS变量
aux_source_directory(src/main/cpp/ DIRSRCS)

#ffmpeg头文件引入
include_directories(src/main/cpp/include)
#soundtouch头文件
include_directories(src/main/cpp/soundtouch/include)
#FDKaac头文件
include_directories(src/main/cpp/FDKaac/)

#加入ffmpeg的动态库
     #加入avcodec-57库
add_library( avcodec-57 SHARED IMPORTED)
     #定义avcodec-57库的本地so文件路径
set_target_properties( avcodec-57
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavcodec-57.so)


add_library( avdevice-57 SHARED IMPORTED)
set_target_properties( avdevice-57
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavdevice-57.so)


add_library( avfilter-6 SHARED IMPORTED)
set_target_properties( avfilter-6
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavfilter-6.so)


add_library( avformat-57 SHARED IMPORTED)
set_target_properties( avformat-57
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavformat-57.so)


add_library( avutil-55 SHARED IMPORTED)
set_target_properties( avutil-55
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavutil-55.so)

add_library( postproc-54 SHARED IMPORTED)
set_target_properties( postproc-54
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libpostproc-54.so)

add_library( swresample-2 SHARED IMPORTED)
set_target_properties( swresample-2
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libswresample-2.so)


add_library( swscale-4 SHARED IMPORTED)
set_target_properties( swscale-4
                       PROPERTIES IMPORTED_LOCATION
                       ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libswscale-4.so)

#cpp目录编译成PlayNative动态库
add_library( # Sets the name of the library.
             PlayNative

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${DIRSRCS}

              )

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

#加入soundtouch子目录的CMakeLists.txt工程
add_subdirectory(src/main/cpp/soundtouch)
#加入FDKaac子目录的CMakeLists.txt工程
add_subdirectory(src/main/cpp/FDKaac)

#链接所有库
target_link_libraries( # Specifies the target library.
                       PlayNative
                       libSound
                       libFDKaac
                       avcodec-57
                       avdevice-57
                       avfilter-6
                       avformat-57
                       avutil-55
                       postproc-54
                       swresample-2
                       swscale-4

                       OpenSLES
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
