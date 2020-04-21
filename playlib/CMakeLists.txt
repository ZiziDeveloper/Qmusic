
cmake_minimum_required(VERSION 3.4.1)

#cpp目录下的文件聚合为DIRSRCS变量
aux_source_directory(src/main/cpp/ DIRSRCS)


#ffmpeg头文件引入
include_directories(src/main/cpp/include)
#soundtouch头文件
include_directories(src/main/cpp/soundtouch/include)
#FDKaac头文件
#include_directories(src/main/cpp/encoder/FDKaac/)

#引入FDKaac的所有头文件，目前方法比较丑，需要优化
foreach( subdir src/main/cpp/encoder/FDKaac/libAACdec src/main/cpp/encoder/FDKaac/libAACenc
        src/main/cpp/encoder/FDKaac/libFDK src/main/cpp/encoder/FDKaac/libMpegTPDec
        src/main/cpp/encoder/FDKaac/libMpegTPEnc src/main/cpp/encoder/FDKaac/libPCMutils
        src/main/cpp/encoder/FDKaac/libSBRenc
        src/main/cpp/encoder/FDKaac/libSBRdec
        src/main/cpp/encoder/FDKaac/libSYS)
    set( incdir ${CMAKE_CURRENT_LIST_DIR}/${subdir}/include )
    set( srcdir ${CMAKE_CURRENT_LIST_DIR}/${subdir}/src )
    include_directories(${incdir})
endforeach()

# 5. 头文件
# 5.1. 定义函数，用于递归添加头文件
function(include_sub_directories_recursively root_dir)
    if (IS_DIRECTORY ${root_dir})               # 当前路径是一个目录吗，是的话就加入到包含目录
        message("qiuyayong include dir: " ${root_dir})
        include_directories(${root_dir})
    endif()

    file(GLOB ALL_SUB RELATIVE ${root_dir} ${root_dir}/*) # 获得当前目录下的所有文件，让如ALL_SUB列表中
    foreach(sub ${ALL_SUB})
        if (IS_DIRECTORY ${root_dir}/${sub})
            include_sub_directories_recursively(${root_dir}/${sub}) # 对子目录递归调用，包含
        endif()
    endforeach()
endfunction()
# 5.2. 添加头文件
include_sub_directories_recursively(src/main/cpp/encoder/FDKaac/) # 对子目录递归调用，包含
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
#             src/main/cpp/encoder/FDKaac/AACEncoder.cpp

              )

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

#加入soundtouch子目录的CMakeLists.txt工程
add_subdirectory(src/main/cpp/soundtouch)
#加入FDKaac子目录的CMakeLists.txt工程
add_subdirectory(src/main/cpp/encoder/FDKaac)

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
