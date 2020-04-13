
cmake_minimum_required(VERSION 3.4.1)

project(FDKaacProject)

#include_directories(${CMAKE_CURRENT_LIST_DIR})
#include_directories(./libSYS/include)
#include_directories(./libFDK/include)
#include_directories(./libAACdec/include)
#include_directories(./libAACenc/include)
#include_directories(./libMpegTPDec/include)
#include_directories(./libMpegTPEnc/include)
#include_directories(./libPCMutils/include)
#include_directories(./libSBRdec/include)
#include_directories(./libSBRenc/include)
set( SOURCE_FILES )
foreach( subdir libAACdec libAACenc
        libFDK libMpegTPDec libMpegTPEnc libPCMutils
        libSBRenc libSBRdec libSYS)
    set( incdir ${CMAKE_CURRENT_LIST_DIR}/${subdir}/include )
    set( srcdir ${CMAKE_CURRENT_LIST_DIR}/${subdir}/src )
    include_directories(${incdir})

    file( GLOB srcfiles
            ${srcdir}/*.cpp
            ${srcdir}/*.h )

    list( APPEND SOURCE_FILES ${srcfiles} )
endforeach()





add_library( # Sets the name of the library.
             libFDKaac

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${SOURCE_FILES}
              )


#target_link_libraries( # Specifies the target library.
#                       libSound)