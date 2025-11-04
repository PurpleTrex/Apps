cmake_minimum_required(VERSION 3.22.1)

project("emulator-jni")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add the JNI library
add_library(emulator-jni SHARED
        emulator_jni.cpp)

# Find required libraries
find_library(log-lib log)
find_library(android-lib android)

# Link libraries
target_link_libraries(emulator-jni
        ${log-lib}
        ${android-lib})

# If you have the LibRetro FCEUmm core, uncomment this:
# find_library(fceumm-lib fceumm PATHS ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})
# target_link_libraries(emulator-jni ${fceumm-lib})
