set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${PROJECT_SOURCE_DIR}/CMakeModules)

add_library(pocketboy_room STATIC EXCLUDE_FROM_ALL
    precompiled_headers.h
    pocketboy_room.cpp
    pocketboy_room.h
    pocketboy_room.rc
)

create_target_directory_groups(pocketboy_room)

target_link_libraries(pocketboy_room PRIVATE pocketboy_common network)
if (ENABLE_WEB_SERVICE)
    target_link_libraries(pocketboy_room PRIVATE web_service)
endif()

target_link_libraries(pocketboy_room PRIVATE cryptopp)
if (MSVC)
    target_link_libraries(pocketboy_room PRIVATE getopt)
endif()
target_link_libraries(pocketboy_room PRIVATE ${PLATFORM_LIBRARIES} Threads::Threads)

if(UNIX AND NOT APPLE)
    install(TARGETS pocketboy_room RUNTIME DESTINATION "${CMAKE_INSTALL_PREFIX}/bin")
endif()

if (CITRA_USE_PRECOMPILED_HEADERS)
    target_precompile_headers(pocketboy_room PRIVATE precompiled_headers.h)
endif()

# Bundle in-place on MSVC so dependencies can be resolved by builds.
if (MSVC)
    include(BundleTarget)
    bundle_target_in_place(pocketboy_room)
endif()
