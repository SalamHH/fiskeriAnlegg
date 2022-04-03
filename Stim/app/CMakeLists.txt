# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.
message(PROJECT_SOURCE_DIR="${PROJECT_SOURCE_DIR}")
CMAKE_MINIMUM_REQUIRED(VERSION 3.6)
project(stim)

#
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY BOTH)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE BOTH)
set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE BOTH)

#add the subfolders we will use, together with where they will be built
#set(libmipo_src_DIR src/main/cpp/mi-programoptions)
#set(libmict_src_DIR src/main/cpp/mi-cpptest)
set(libdate_src_DIR src/main/cpp/date)
set(libfimex_src_DIR src/main/cpp/fimex)
set(libnorkloader_src_DIR src/main/cpp/norkloader)
#file(MAKE_DIRECTORY ${libmipo_src_DIR}/build)
#file(MAKE_DIRECTORY ${libmict_src_DIR}/build)
file(MAKE_DIRECTORY ${libdate_src_DIR}/build)
file(MAKE_DIRECTORY ${libfimex_src_DIR}/build)
file(MAKE_DIRECTORY ${libnorkloader_src_DIR}/build)
#add the CMakeLists in each directory as build dependencies
#add_subdirectory(${libmipo_src_DIR} ${libmipo_src_DIR}/build)
#add_subdirectory(${libmict_src_DIR} ${libmict_src_DIR}/build)
add_subdirectory(${libdate_src_DIR} ${libdate_src_DIR}/build)
add_subdirectory(${libfimex_src_DIR} ${libfimex_src_DIR}/build)
add_subdirectory(${libnorkloader_src_DIR} ${libnorkloader_src_DIR}/build)

#####################
# mi-programoptions #
#####################
#add_library(libmiprogramoptions SHARED IMPORTED)
#set_target_properties(libmiprogramoptions PROPERTIES IMPORTED_LOCATION
#        src/main/cpp/mi-programoptions/build/${ANDROID_ABI}/libmiprogramoptions.a)
#include_directories(src/main/cpp/mi-programoptions/include)


# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.
add_library( # Specifies the name of the library.
        libnorkloader
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        src/main/cpp/norkloader/src/NorKyst800FimexDataLoader.cpp)

###########
# LOGGING # (prebuilt!)
###########
find_library( # Defines the name of the path variable that stores the
        # location of the NDK library.
        liblog
        # Specifies the name of the NDK library that
        # CMake needs to locate.
        log)

#########
# FIMEX # (has to be built!)
#########

#Set parameters
#OPTION(BUILD_SHARED_LIBS "Select ON (default) for shared lib, OFF for static lib, BOTH for both" ON)
#set(BUILD_SHARED_LIBS OFF FORCE)
#set(date_INC_DIR /home/christian/AndroidStudioProjects/stim/Stim/app/src/main/cpp/date/include)
#set(date_DIR /home/christian/AndroidStudioProjects/stim/Stim/app/src/main/cpp/date)
#set(date_INC_DIR date)

# Adds the output of the additional CMake build as a prebuilt static
# library and names it libfimex.
add_library(libfimex STATIC IMPORTED)
set_target_properties(libfimex PROPERTIES IMPORTED_LOCATION
        ${lib_build_DIR}/${ANDROID_ABI}/libfimex.a)
include_directories(${lib_src_DIR}/include)

# Links the top-level CMake build output against lib_gmath.
target_link_libraries(libnorkloader libfimex liblog)


