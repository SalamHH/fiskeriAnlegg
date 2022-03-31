# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.
message(PROJECT_SOURCE_DIR="${PROJECT_SOURCE_DIR}")
CMAKE_MINIMUM_REQUIRED(VERSION 2.8.12)
# cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
        lib_norkloader

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/NorKyst800FimexDataLoader.cpp)

###########
# LOGGING #
###########
find_library( # Defines the name of the path variable that stores the
        # location of the NDK library.
        lib_log

        # Specifies the name of the NDK library that
        # CMake needs to locate.
        log)

#########
# FIMEX #
#########
# Sets lib_src_DIR to the path of the target CMake project.
set(lib_src_DIR src/main/cpp/fimex-fimex_1_4_1__1)

# Sets lib_build_DIR to the path of the desired output directory.
set(lib_build_DIR src/main/cpp/fimex-fimex_1_4_1__1/out)
file(MAKE_DIRECTORY ${lib_build_DIR})

# Adds the CMakeLists.txt file located in the specified directory
# as a build dependency.
add_subdirectory( # Specifies the directory of the CMakeLists.txt file.
        ${lib_src_DIR}

        # Specifies the directory for the build outputs.
        ${lib_build_DIR})

# Adds the output of the additional CMake build as a prebuilt static
# library and names it lib_gmath.
add_library(libfimex STATIC IMPORTED)
set_target_properties(libfimex PROPERTIES IMPORTED_LOCATION
        ${lib_build_DIR}/${ANDROID_ABI}/libfimex.a)
include_directories(${lib_src_DIR}/include)

# Links the top-level CMake build output against lib_gmath.
target_link_libraries(lib_norkloader libfimex lib_log)


