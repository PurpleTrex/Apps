add_executable(pocketboy_room_standalone
    pocketboy_room_standalone.cpp
)

set_target_properties(pocketboy_room_standalone PROPERTIES OUTPUT_NAME "pocketboy-room")

target_link_libraries(pocketboy_room_standalone PRIVATE pocketboy_room)

if(UNIX AND NOT APPLE)
    install(TARGETS pocketboy_room_standalone RUNTIME DESTINATION "${CMAKE_INSTALL_PREFIX}/bin")
endif()
