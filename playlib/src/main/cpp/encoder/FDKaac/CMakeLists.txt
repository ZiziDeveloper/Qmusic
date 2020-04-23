
cmake_minimum_required(VERSION 3.4.1)

project(FDKaacProject)

#头文件已经在主CMakeLists引入了，这里不需要再引入
set( SOURCE_FILES )
#include_directories( ${CMAKE_CURRENT_LIST_DIR})
foreach( subdir libAACdec libAACenc
        libFDK libMpegTPDec libMpegTPEnc libPCMutils
        libSBRenc libSBRdec libSYS)
    set( incdir ${CMAKE_CURRENT_LIST_DIR}/${subdir}/include )
    set( srcdir ${CMAKE_CURRENT_LIST_DIR}/${subdir}/src )
#    include_directories(${incdir})

    file( GLOB srcfiles
            ${srcdir}/*.cpp
            ${srcdir}/*.h )

    list( APPEND SOURCE_FILES ${srcfiles} )
endforeach()
list( APPEND SOURCE_FILES ${CMAKE_CURRENT_LIST_DIR}/AACEncoder.cpp )
list( APPEND SOURCE_FILES ${CMAKE_CURRENT_LIST_DIR}/aac-enc.c )
list( APPEND SOURCE_FILES ${CMAKE_CURRENT_LIST_DIR}/wavreader.c )





add_library( # Sets the name of the library.
             libFDKaac

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${SOURCE_FILES}
              )


#target_link_libraries( # Specifies the target library.
#                       libSound)